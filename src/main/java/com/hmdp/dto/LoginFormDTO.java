package com.hmdp.dto;

import lombok.Data;

/**
 * @author 21027
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
