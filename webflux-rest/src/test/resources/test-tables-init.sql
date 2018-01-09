DROP TABLE USERS IF EXISTS;
CREATE TABLE USERS (
  user_id INTEGER IDENTITY,
  user_name varchar(45) NOT NULL,
  user_surname varchar(45) NOT NULL,
  user_email varchar(45) NOT NULL
);

DROP TABLE TASKS IF EXISTS;
CREATE TABLE TASKS (
task_id INTEGER IDENTITY,
user_id INTEGER,
task_name varchar(45) NOT NULL,
task_desc varchar(45) NOT NULL,
task_creation_date date NOT NULL,
task_deadline_date date NOT NULL,
CONSTRAINT fk_tasks_1 FOREIGN KEY (user_id) REFERENCES USERS (user_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
