-- initial db
DROP TABLE IF EXISTS userSession;
create table userSession (
    id varchar(255) not null,
    clientType varchar(15) not null,
    expireDate bigint(20),
    token varchar(200) unique,
    userId varchar(255) not null,
    userAgent varchar(512),
    primary key (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS passwordToken;
create table passwordToken (
    id varchar(255) not null,
    email varchar(255) not null unique,
    timestamp bigint(20),
    token varchar(200) unique,
    tokenType varchar(20) not null,
    primary key (id)
) ENGINE=InnoDB;