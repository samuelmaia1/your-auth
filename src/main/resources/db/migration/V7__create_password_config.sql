CREATE TABLE password_config (
     id VARCHAR(36) NOT NULL,
     project_id VARCHAR(36) NOT NULL,

     number_required BOOLEAN NOT NULL,
     special_char_required BOOLEAN NOT NULL,
     uppercase_required BOOLEAN NOT NULL,
     lowercase_required BOOLEAN NOT NULL,

     min_size INTEGER NOT NULL,
     max_size INTEGER NOT NULL,

     CONSTRAINT pk_password_config
         PRIMARY KEY (id),

     CONSTRAINT uk_password_config_project
         UNIQUE (project_id),

     CONSTRAINT fk_password_config_project
         FOREIGN KEY (project_id)
             REFERENCES projects (id)
             ON DELETE CASCADE,

     CONSTRAINT ck_password_config_min_size
         CHECK (min_size BETWEEN 1 AND 120),

     CONSTRAINT ck_password_config_max_size
         CHECK (max_size BETWEEN 1 AND 120),

     CONSTRAINT ck_password_config_size_range
         CHECK (min_size <= max_size)
);

INSERT INTO password_config (
    id,
    project_id,
    number_required,
    special_char_required,
    uppercase_required,
    lowercase_required,
    min_size,
    max_size
)
SELECT
    project.id,
    project.id,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    1,
    120
FROM projects AS project;