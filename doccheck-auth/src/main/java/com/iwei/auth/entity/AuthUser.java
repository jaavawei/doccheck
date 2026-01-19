package com.iwei.auth.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 用户权限实体类
 *
 * @auther: zhaokangwei
 */
@Component
@Data
public class AuthUser {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String password;

    private Integer sex;

    private String avatar;

    private Integer status;

    private String introduce;

    private String extJson;

    private Date createdAt;

    private String createdBy;

    private Date updatedAt;

    private String updatedBy;

    private String delFlg;
}
