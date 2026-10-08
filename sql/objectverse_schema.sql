CREATE DATABASE IF NOT EXISTS objectverse
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE objectverse;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    email VARCHAR(128),
    role VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    age INT,
    occupation VARCHAR(128),
    bio TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_account_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    domain_type VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project_user_id (user_id),
    INDEX idx_project_visibility (visibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS requirement_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    raw_requirement TEXT NOT NULL,
    analysis_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    summary TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_requirement_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oop_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    class_name VARCHAR(128) NOT NULL,
    package_name VARCHAR(255),
    description TEXT,
    is_abstract TINYINT DEFAULT 0,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_oop_class_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oop_interface (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    interface_name VARCHAR(128) NOT NULL,
    package_name VARCHAR(255),
    description TEXT,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_oop_interface_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oop_attribute (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    attribute_name VARCHAR(128) NOT NULL,
    attribute_type VARCHAR(128) NOT NULL,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
    default_value VARCHAR(255),
    description TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_oop_attribute_project_id (project_id),
    INDEX idx_oop_attribute_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oop_method (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    method_name VARCHAR(128) NOT NULL,
    return_type VARCHAR(128),
    `parameters` TEXT,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    description TEXT,
    method_body LONGTEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_oop_method_project_id (project_id),
    INDEX idx_oop_method_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oop_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    relation_type VARCHAR(32) NOT NULL,
    description TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_oop_relation_project_id (project_id),
    INDEX idx_oop_relation_source (source_type, source_id),
    INDEX idx_oop_relation_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS design_pattern_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    pattern_name VARCHAR(128) NOT NULL,
    pattern_type VARCHAR(64) NOT NULL,
    reason TEXT,
    implementation_advice TEXT,
    related_class_ids VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_design_pattern_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    task_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    user_input TEXT,
    result_summary TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_ai_task_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    task_id BIGINT,
    agent_name VARCHAR(128) NOT NULL,
    prompt_content LONGTEXT,
    response_content LONGTEXT,
    model_name VARCHAR(128),
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_ai_conversation_project_id (project_id),
    INDEX idx_ai_conversation_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS code_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    package_name VARCHAR(255),
    file_type VARCHAR(64) NOT NULL,
    code_content LONGTEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code_file_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS uml_diagram (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    diagram_type VARCHAR(64) NOT NULL,
    diagram_data LONGTEXT,
    description TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_uml_diagram_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS review_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    review_type VARCHAR(64) NOT NULL,
    score DECIMAL(5,2),
    problem_summary TEXT,
    suggestion TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_review_result_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS simulation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    simulation_name VARCHAR(128) NOT NULL,
    scenario_description TEXT,
    execution_steps LONGTEXT,
    result_summary TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_simulation_record_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS knowledge_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    category VARCHAR(64),
    description TEXT,
    content TEXT NOT NULL,
    source_type VARCHAR(64) NOT NULL DEFAULT 'CODE_REVIEW',
    image_url VARCHAR(512),
    tags VARCHAR(255),
    visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
    like_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_knowledge_note_user_id (user_id),
    INDEX idx_knowledge_note_visibility (visibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS knowledge_note_reaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type VARCHAR(32) NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_note_reaction_note (note_id),
    INDEX idx_note_reaction_user (user_id),
    INDEX idx_note_reaction_active (note_id, user_id, reaction_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS practice_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    knowledge_note_id BIGINT,
    question_type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    answer TEXT,
    create_time DATETIME,
    update_time DATETIME,
    deleted TINYINT DEFAULT 0,
    INDEX idx_practice_question_user_id (user_id),
    INDEX idx_practice_question_note_id (knowledge_note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
