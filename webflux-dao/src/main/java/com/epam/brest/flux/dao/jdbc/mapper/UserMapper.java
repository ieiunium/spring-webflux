package com.epam.brest.flux.dao.jdbc.mapper;

import com.epam.brest.flux.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .userId(rs.getInt("user_id"))
                .userName(rs.getString("user_name"))
                .build();
    }
}
