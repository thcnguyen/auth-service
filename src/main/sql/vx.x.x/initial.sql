  create table userSession (
      id bigint(20) not null auto_increment,
      clientType varchar(15) not null,
      expireDate bigint(20),
      token varchar(200) unique,
      userId varchar(255) not null,
      primary key (id)
  ) ENGINE=InnoDB;

  create table passwordToken (
      id bigint(20) not null auto_increment,
      email varchar(255) not null unique,
      expireDate bigint(20),
      token varchar(200) unique,
      tokenType varchar(20) not null,
      userId varchar(255) not null,
      primary key (id)
  ) ENGINE=InnoDB;