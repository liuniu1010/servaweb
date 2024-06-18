create table configs(
id                int            not null primary key,
version           int            not null,
configname        varchar(30)    not null,
configvalue       varchar(100),
comments          varchar(100),
unique key        uniqueno_element (configname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

insert into configs(id, version, configname, configvalue) values(1, 1, 'OpenAiApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue) values(2, 1, 'DateFormat', 'dd/MM/yyyy');
insert into configs(id, version, configname, configvalue) values(3, 1, 'TimeFormat', 'HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(4, 1, 'DateTimeFormat', 'dd/MM/yyyy HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(5, 1, 'GoogleApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue, comments) values(6, 1, 'AIInstance', 'GoogleAIImpl', 'option of GoogleAIImpl/OpenAIImpl');
insert into configs(id, version, configname, configvalue, comments) values(7, 1, 'codeModel', 'gemini-1.5-pro-latest', '');


insert into configs(id, version, configname, configvalue, comments) values(11, 1, 'javamavenlinuxSandBoxUrl', 'http://localhost:8081/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(12, 1, 'javagradlelinuxSandBoxUrl', 'http://localhost:8082/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(13, 1, 'dotnetlinuxSandBoxUrl', 'http://localhost:8083/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(14, 1, 'python3linuxSandBoxUrl', 'http://localhost:8084/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(15, 1, 'nodejslinuxSandBoxUrl', 'http://localhost:8085/api/aisandbox', '');

insert into configs(id, version, configname, configvalue, comments) values(51, 1, 'email.username', '<username>', '');
insert into configs(id, version, configname, configvalue, comments) values(52, 1, 'email.password', '<password>', '');
insert into configs(id, version, configname, configvalue, comments) values(53, 1, 'mail.smtp.auth', 'true', '');
insert into configs(id, version, configname, configvalue, comments) values(54, 1, 'mail.smtp.starttls.enable', 'true', '');
insert into configs(id, version, configname, configvalue, comments) values(55, 1, 'mail.smtp.host', 'smtp.gmail.com', '');
insert into configs(id, version, configname, configvalue, comments) values(56, 1, 'mail.smtp.port', '587', '');

create table chatrecord(
id               int             not null primary key,
version          int             not null,
session          varchar(50)     not null,
isrequest        tinyint(1)      not null,
chattime         datetime        not null,
content          text            not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table coderecord(
id               int             not null primary key,
version          int             not null,
session          varchar(50)     not null,
createtime       datetime        not null,
requirement      text            null,
content          text            null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table useraccount(
id               int             not null primary key,
version          int             not null,
username         varchar(50)     not null unique,
encyptedpassword varchar(64)     not null,
registtime       datetime        not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table loginsession(
id               int             not null primary key,
version          int             not null,
session          varchar(20)     not null,
accountid        int             not null,
expiretime       datetime        not null,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table chasedcredits(
id               int             not null primary key,
version          int             not null,
accountid        int             not null,
credits          int             not null default 0,
expiretime       datetime        not null,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table consumedcredits(
id               int             not null primary key,
version          int             not null,
accountid        int             not null,
credits          int             not null,
consumetime      datetime        not null,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
