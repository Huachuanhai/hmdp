package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * 发送短信验证码
     * @param phone 手机号码
     * @param session session
     * @return Result
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 短信登录
     * @param loginForm 登录信息
     * @param session session
     * @return Result
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    /**
     * 用户签到
     * @return Result
     */
    Result sign();

    /**
     * 连续签到统计
     * @return Result
     */
    Result signCount();
}
