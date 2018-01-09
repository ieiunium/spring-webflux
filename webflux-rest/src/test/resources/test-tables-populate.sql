INSERT INTO users (user_id,
                   user_name,
                   user_surname,
                   user_email)
VALUES (
  1,
  'USER NAME 1',
  'USER SURNAME 1',
  'test1@mail.com'
);

INSERT INTO users (user_id,
                   user_name,
                   user_surname,
                   user_email)
VALUES (
  2,
  'USER NAME 1',
  'USER SURNAME 1',
  'test1@mail.com'
);


INSERT INTO tasks (user_id,
                   task_name,
                   task_desc,
                   task_creation_date,
                   task_deadline_date)
VALUES (
  1,
  'TEST_TASK1',
  'TEST TASK DESC1',
  CURRENT_TIMESTAMP,
  TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)
);

INSERT INTO tasks (user_id,
                   task_name,
                   task_desc,
                   task_creation_date,
                   task_deadline_date)
VALUES (
  1,
  'TEST_TASK2',
  'TEST TASK DESC2',
  CURRENT_TIMESTAMP,
  TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)
);

INSERT INTO tasks (user_id,
                   task_name,
                   task_desc,
                   task_creation_date,
                   task_deadline_date)
VALUES (
  1,
  'TEST_TASK3',
  'TEST TASK DESC3',
  CURRENT_TIMESTAMP,
  TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)
);

INSERT INTO tasks (user_id,
                   task_name,
                   task_desc,
                   task_creation_date,
                   task_deadline_date)
VALUES (
  2,
  'TEST_TASK4',
  'TEST TASK DESC4',
  CURRENT_TIMESTAMP,
  TIMESTAMPADD(SQL_TSI_DAY, 1, CURRENT_TIMESTAMP)
);