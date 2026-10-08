package com.example.objectverse.mapper;

import com.example.objectverse.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    UserAccount findById(@Param("id") Long id);

    UserAccount findByUsername(@Param("username") String username);

    int insert(UserAccount userAccount);

    int updateProfile(UserAccount userAccount);
}
