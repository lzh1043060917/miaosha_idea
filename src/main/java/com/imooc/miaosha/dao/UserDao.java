package com.imooc.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.imooc.miaosha.domain.User;

// 这个注解是把dao标记为bean，这样autowired不会报红
@Repository
@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    User getById(@Param("id")int id);

    @Insert("insert into user(id, name)values(#{id}, #{name})")
    public int insert(User user);
}
