package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAccount extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String role;

    private String status;

    private Integer age;

    private String occupation;

    private String bio;
}
