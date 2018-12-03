  create table user_session (
      id bigint(20) not null auto_increment,
      client_type varchar(15) not null,
      expire_date bigint(20),
      token varchar(255) unique,
      uid varchar(255) not null,
      primary key (id)
  ) ENGINE=InnoDB;