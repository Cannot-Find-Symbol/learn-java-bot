CREATE SEQUENCE IF NOT EXISTS role_group_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS spam_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS warn_seq
    START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS info (
    topic VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    message VARCHAR(2000),
    PRIMARY KEY (topic)
);

CREATE TABLE IF NOT EXISTS member_info (
    id BIGINT NOT NULL,
    month_thank_count INT,
    total_thank_count INT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS role_group (
    id BIGINT NOT NULL,
    channel_id BIGINT,
    guild_id BIGINT,
    is_unique BIT(1),
    message VARCHAR(255),
    message_id BIGINT,
    name VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member_role (
    id BIGINT NOT NULL,
    description VARCHAR(255),
    ordinal INT NOT NULL,
    group_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_member_role_group_id (group_id),
    CONSTRAINT fk_member_role_group FOREIGN KEY (group_id) REFERENCES role_group (id)
);

CREATE TABLE IF NOT EXISTS spam (
    id BIGINT NOT NULL,
    message VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS warn (
    id BIGINT NOT NULL,
    date DATE,
    reason VARCHAR(255),
    username VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS trivia_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aliases VARCHAR(500) NOT NULL,
    answer VARCHAR(100) NOT NULL,
    prompt VARCHAR(1750) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_trivia_question_prompt (prompt) USING HASH
);

CREATE TABLE IF NOT EXISTS trivia_score (
    id VARCHAR(255) NOT NULL,
    guild_id VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_trivia_score_guild_user (guild_id, user_id)
);
