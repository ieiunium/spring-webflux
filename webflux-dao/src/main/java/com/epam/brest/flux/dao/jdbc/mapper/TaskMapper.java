package com.epam.brest.flux.dao.jdbc.mapper;

import com.epam.brest.flux.model.Task;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserMapper userMapper = new UserMapper();

        return Task.builder()
                .taskId(rs.getInt("task_id"))
                .deadLine(rs.getDate("task_deadline_date").toLocalDate())
                .created(rs.getDate("task_creation_date").toLocalDate())
                .description(rs.getString("task_desc"))
                .title(rs.getString("task_name"))
                .owner(userMapper.mapRow(rs, rowNum))
                .build();
    }
}
