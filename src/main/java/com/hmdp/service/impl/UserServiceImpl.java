package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.resource.StringResource;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.nio.file.CopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }

        // 3.符合，生产验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5.发送验证码
        log.info("发送短信验证码成功，验证码：{}", code);

        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }

        //2.从redis获取验证码并校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //3.不一致，报错
            return Result.fail("验证码错误");
        }

        //4.一致，根据手机号查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.lambda()
                .eq(User::getPhone, loginForm.getPhone());
        User user = userMapper.selectOne(userQueryWrapper);

        //5.判断用户是否存在
        if (null == user) {
            //6.不存在，创建新用户并保存
            user = createUserWithPhone(loginForm.getPhone());
        }

        //7.保存用户信息到session中
        //7.1 随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);

        //7.2 将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        //7.3 存储
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        //7.4 设置token的有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);

        //8 返回token
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        //1. 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        //2. 获取日期
        LocalDateTime now = LocalDateTime.now();
        //3. 拼接key
        String keySuffix = LocalDateTimeUtil.format(now, DatePattern.SIMPLE_MONTH_PATTERN);
        String key = RedisConstants.USER_SIGN_KEY + userId + keySuffix;

        //4. 获取今天是本月中的第几天
        int dayOfMonth = now.getDayOfMonth();

        //5. 写入redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        return Result.ok();
    }

    @Override
    public Result signCount() {
        //1. 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        //2. 获取日期
        LocalDateTime now = LocalDateTime.now();
        //3. 拼接key
        String keySuffix = LocalDateTimeUtil.format(now, DatePattern.SIMPLE_MONTH_PATTERN);
        String key = RedisConstants.USER_SIGN_KEY + userId + keySuffix;

        //4. 获取今日是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        //5. 获取本月截至到今日的所有签到记录，返回的是一个十进制的数字
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.signed((dayOfMonth))).valueAt(0));

        if (CollectionUtil.isEmpty(result)) {
            return Result.ok(0);
        }

        Long num = result.get(0);
        if (null == num || 0 == num) {
            return Result.ok(0);
        }

        int count = 0;
        //6. 循环遍历
        while (true) {
            //6.1 让这个数字与1做与运算，得到数字的最后一个bit位
            //6.2 判断这个bit位是否为0
            if ((num & 1) == 0) {
                //6.3 如果为0，说明未签到，结束
                break;
            } else {
                //6.4 如果不为0.说明已经签到，计数器+1
                count++;
                //6.5 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
                num >>>= 1;
            }
        }

        //7. 返回连续签到天数
        return Result.ok(count);
    }

    /**
     * 根据手机号创建用户
     *
     * @param phone 手机号码
     * @return User
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
