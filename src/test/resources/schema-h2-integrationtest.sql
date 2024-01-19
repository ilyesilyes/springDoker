CREATE SCHEMA IF NOT EXISTS securecapita;

SET SCHEMA securecapita;

DROP TABLE IF EXISTS Users;

CREATE TABLE Users
(
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(50)     NOT NULL,
    last_name    VARCHAR(50)     NOT NULL,
    email        VARCHAR(100)    NOT NULL,
    password     VARCHAR(255)    NOT NULL,
    address      VARCHAR(255)    NULL,
    phone        VARCHAR(30)     NULL,
    title        VARCHAR(50)     NULL,
    bio          VARCHAR(255)    NULL,
    enabled      BOOLEAN      DEFAULT TRUE,
    non_locked   BOOLEAN      DEFAULT TRUE,
    using_mfa    BOOLEAN      DEFAULT FALSE,
    created_date DATETIME     DEFAULT CURRENT_TIMESTAMP,
    image_url    VARCHAR(255) DEFAULT NULL,
    CONSTRAINT UQ_User_Email UNIQUE (email)
);

DROP TABLE IF EXISTS Roles;

CREATE TABLE Roles
(
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)     NOT NULL,
    permission   VARCHAR(255)     NOT NULL,
    CONSTRAINT UQ_Roles_Name UNIQUE (name)
);

DROP TABLE IF EXISTS UserRoles;

CREATE TABLE UserRoles
(
    id           BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT     NOT NULL,
    role_id      BIGINT     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT UQ_UserRoles_User_Id UNIQUE (user_id)
);

DROP TABLE IF EXISTS Events;

CREATE TABLE Events
(
    id           BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type         VARCHAR(50)    NOT NULL CHECK ( type IN ('LOGIN_ATTEMPT', 'LOGIN_ATTEMPT_FAILURE', 'LOGIN_ATTEMPT_SUCCESS', 'PROFILE_UPDATE', 'PROFILE_PICTURE_UPDATE', 'ROLE_UPDATE', 'ACCOUNT_SETTING_UPDATE', 'PASSWORD_UPDATE', 'MFA_UPDATE') ),
    description  VARCHAR(255)   NOT NULL,
    CONSTRAINT UQ_Events_Type UNIQUE (type)
);

DROP TABLE IF EXISTS UserEvents;

CREATE TABLE UserEvents
(
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    event_id     BIGINT NOT NULL,
    device       VARCHAR(100) DEFAULT NULL,
    ip_address   VARCHAR(100) DEFAULT NULL,
    create_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (event_id) REFERENCES Events (id) ON DELETE RESTRICT ON UPDATE CASCADE
);

DROP TABLE IF EXISTS AccountVerifications;

CREATE TABLE AccountVerifications
(
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    url          VARCHAR(255) NOT NULL,
    -- date         DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_AccountVerifications_User_ID UNIQUE (user_id),
    CONSTRAINT UQ_AccountVerifications_Url UNIQUE (url)
);


DROP TABLE IF EXISTS ResetPasswordVerifications;

CREATE TABLE ResetPasswordVerifications
(
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    url                 VARCHAR(255) NOT NULL,
    expiration_date     DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_ResetPasswordVerifications_User_ID UNIQUE (user_id),
    CONSTRAINT UQ_ResetPasswordVerifications_Url UNIQUE (url)
);

DROP TABLE IF EXISTS TwoFactorVerifications;

CREATE TABLE TwoFactorVerifications
(
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    code                VARCHAR(10) NOT NULL,
    expiration_date     DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_TwoFactorVerifications_User_ID UNIQUE (user_id),
    CONSTRAINT UQ_TwoFactorVerifications_Code UNIQUE (code)
);

insert into Roles (name, permission)
values ('ROLE_USER', 'READ:USER,READ:CUSTOMER'),
       ('ROLE_MANAGER', 'READ:USER,READ:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER'),
       ('ROLE_ADMIN', 'READ:USER,READ:CUSTOMER,CREATE:USER,CREATE:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER'),
       ('ROLE_SYSADMIN', 'READ:USER,READ:CUSTOMER,CREATE:USER,CREATE:CUSTOMER,UPDATE:USER,UPDATE:CUSTOMER,DELETE:USER,DELETE:CUSTOMER');



