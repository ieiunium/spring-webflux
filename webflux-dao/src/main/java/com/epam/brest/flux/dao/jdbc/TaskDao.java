package com.epam.brest.flux.dao.jdbc;

import com.epam.brest.flux.dao.ITaskDao;
import com.epam.brest.flux.dao.jdbc.mapper.TaskMapper;
import com.epam.brest.flux.model.Task;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Repository
@Profile("jdbc")
public class TaskDao implements ITaskDao {
    private static final String GET_TASK_BY_ID = "SELECT * FROM tasks JOIN users u ON tasks.user_id = u.user_id WHERE tasks.task_id = :id";
    private static final String GET_ALL_TASKS = "SELECT * FROM tasks JOIN users u ON tasks.user_id = u.user_id";
    private static final String GET_ALL_TASKS_BY_OWNER_ID = "SELECT * FROM tasks JOIN users u ON tasks.user_id = u.user_id WHERE tasks.user_id = :ownerId";
    private static final String CREATE_TASK = "INSERT INTO tasks(user_id, task_name, task_desc, task_creation_date, task_deadline_date) VALUES (:ownerId, :title, :description, :created, :deadLine)";
    private static final String UPDATE_TASK = "UPDATE tasks SET user_id = :ownerId, task_name = :title, task_desc = :description, task_creation_date = :created, task_deadline_date = :deadLine WHERE task_id = :taskId";
    private static final String DELETE_TASK = "DELETE FROM tasks WHERE task_id = :taskId";

    private final NamedParameterJdbcOperations jdbcOperations;
    private final TaskMapper taskMapper = new TaskMapper();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");

    public TaskDao(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;

    }

    @Override
    public Mono<Task> getTaskById(int id) {
        Map<String, Object> paramSource = new HashMap<>();
        paramSource.put("id", id);
        return Mono.create(taskMonoSink -> jdbcOperations.query(GET_TASK_BY_ID, paramSource, taskExtractor(taskMonoSink)));
    }

    @Override
    public Mono<Task> createTask(Task task, Integer ownerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("ownerId", ownerId);
        params.put("title", task.getTitle());
        params.put("description", task.getDescription());
        params.put("created", task.getCreated().format(dateTimeFormatter));
        params.put("deadLine", task.getDeadLine().format(dateTimeFormatter));

        return Mono.create(sink -> {
            SqlParameterSource taskToCreate = new MapSqlParameterSource(params);
            KeyHolder key = new GeneratedKeyHolder();
            int i = jdbcOperations.update(CREATE_TASK, taskToCreate, key);
            task.setTaskId(key.getKey().intValue());
            if (i == 1) {
                sink.success(task);
            } else {
                sink.error(new RuntimeException("Could not create task"));
            }
        });
    }

    @Override
    public Mono<Task> updateTask(Task task, Integer ownerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("ownerId", ownerId);
        params.put("title", task.getTitle());
        params.put("description", task.getDescription());
        params.put("created", task.getCreated().format(dateTimeFormatter));
        params.put("deadLine", task.getDeadLine().format(dateTimeFormatter));
        params.put("taskId", task.getTaskId());

        SqlParameterSource taskToUpdate = new MapSqlParameterSource(params);
        KeyHolder key = new GeneratedKeyHolder();

        return Mono.create(sink -> {
            int i = jdbcOperations.update(UPDATE_TASK, taskToUpdate, key);
            task.setTaskId(key.getKey().intValue());
            if (i == 1) {
                sink.success(task);
            } else {
                sink.error(new RuntimeException("Could not create task"));
            }
        });
    }

    @Override
    public Flux<Task> getAllTasks() {
        return Flux.create(taskFluxSink -> {
            jdbcOperations.query(GET_ALL_TASKS, taskExtractor(taskFluxSink));
            taskFluxSink.complete();
        });
    }

    @Override
    public Flux<Task> getAllTasksOfAUser(int userId) {
        return Flux.create(taskFluxSink -> {
            Map<String, Object> params = new HashMap<>();
            params.put("ownerId", userId);
            jdbcOperations.query(GET_ALL_TASKS_BY_OWNER_ID, params, taskExtractor(taskFluxSink));
            taskFluxSink.complete();
        });
    }

    @Override
    public Mono<Void> deleteTaskById(int id) {
        return Mono.create(voidMonoSink -> {
            int i = jdbcOperations.update(DELETE_TASK, new MapSqlParameterSource("taskId", id));
            if (i == 1) {
                voidMonoSink.success();
            } else {
                voidMonoSink.error(new RuntimeException("Could not delete a task " + id));
            }
        });
    }

    private RowCallbackHandler taskExtractor(MonoSink<Task> sink) {
        return rs -> {
            try {
                Task task = taskMapper.mapRow(rs, 0);
                sink.success(task);
            } catch (SQLException e) {
                sink.error(e);
            }
        };
    }

    private RowCallbackHandler taskExtractor(FluxSink<Task> sink) {
        return rs -> {
            try {
                Task task = taskMapper.mapRow(rs, 0);
                sink.next(task);
            } catch (SQLException e) {
                sink.error(e);
            }
        };
    }
}
