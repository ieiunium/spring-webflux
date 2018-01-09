package com.epam.brest.flux.dao.jdbc;

import com.epam.brest.flux.dao.IUserDao;
import com.epam.brest.flux.dao.jdbc.mapper.UserMapper;
import com.epam.brest.flux.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
@Profile("jdbc")
public class UserDao implements IUserDao {
    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE users.user_id = :userId";
    private static final String GET_ALL_USERS = "SELECT * FROM users";
    private static final String CREATE_USER = "INSERT INTO users(user_name, user_surname, user_email) VALUES (:userName, '', '')";
    private static final String UPDATE_USER = "UPDATE users SET user_name = :user_name WHERE user_id = :user_id";
    private static final String DELETE_USER = "DELETE from users WHERE user_id = :userId";

    private final NamedParameterJdbcOperations jdbcOperations;
    private final UserMapper userMapper = new UserMapper();

    public UserDao(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public Mono<User> getUserById(int userId) {
        Map<String, Object> userParam = new HashMap<>();
        userParam.put("userId", userId);
        return Mono.create(userMonoSink -> jdbcOperations.query(GET_USER_BY_ID, userParam, extractUser(userMonoSink)));
    }

    @Override
    public Mono<User> createUser(User user) {
        if (user.getUserId() != null) {
            return Mono.error(new IllegalArgumentException("Cannot create already existing user!"));
        }
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);
        return Mono.create(userMonoSink -> {
            try {
                KeyHolder key = new GeneratedKeyHolder();
                int result = jdbcOperations.update(CREATE_USER, parameterSource, key);
                if (result == 1) {
                    user.setUserId(key.getKey().intValue());
                    userMonoSink.success(user);
                } else {
                    userMonoSink.error(new RuntimeException("Could not create a User"));
                }
            } catch (DataAccessException e) {
                userMonoSink.error(e);
            }
        });
    }

    @Override
    public Mono<User> updateUser(User user) {
        SqlParameterSource userToUpdate = new BeanPropertySqlParameterSource(user);
        return Mono.create(userMonoSink -> {
            int i = jdbcOperations.update(UPDATE_USER, userToUpdate);
            if (i == 1) {
                userMonoSink.success(user);
            } else {
                userMonoSink.error(new RuntimeException("Could not update a User!"));
            }
        });
    }

    @Override
    public Flux<User> getAllUsers() {
        return Flux.create(userFluxSink -> {
            jdbcOperations.query(GET_ALL_USERS, extractUser(userFluxSink));
            userFluxSink.complete();
        });
    }

    @Override
    public Mono<Void> deleteUserById(int id) {
        Map<String, Integer> deleteParam = new HashMap<>();
        deleteParam.put("userId", id);

        return Mono.create(voidMonoSink -> {
            int result = jdbcOperations.update(DELETE_USER, deleteParam);
            if (result == 1) {
                voidMonoSink.success();
            } else  {
                voidMonoSink.error(new RuntimeException(String.format("Could not delete a user %d", id)));
            }
        });
    }

    private RowCallbackHandler extractUser(MonoSink<User> userMonoSink) {
        return rs -> {
            try {
                User user = userMapper.mapRow(rs, 0);
                userMonoSink.success(user);
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                userMonoSink.error(e);
            }
        };
    }

    private RowCallbackHandler extractUser(FluxSink<User> userFluxSink) {
        return rs -> {
            try {
                User user = userMapper.mapRow(rs, 0);
                userFluxSink.next(user);
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                userFluxSink.error(e);
            }
        };
    }
}
