package com.guangke.forum.mapper;

import com.guangke.forum.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByUsername(String username);

    User selectByEmail(String email);

    int updatePassword(int id,String password);

    int updateHeader(int id,String headerUrl);

    int updateStatus(int id,int status);

    int insertUser(User user);

}
