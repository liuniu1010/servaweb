create database testenv;
grant all privileges on testenv.* to 'testenv'@'%' identified by 'abcd1234';
grant all privileges on testenv.* to 'testenv'@'localhost' identified by 'abcd1234';
flush privileges;

use testenv;

create table configs(
id                char(36)       not null primary key,
version           int            not null,
configname        varchar(50)    not null,
configvalue       varchar(100),
comments          varchar(100),
unique key        uniqueno_element (configname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

insert into configs(id, version, configname, configvalue) values(uuid(), 1, 'OpenAiApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue) values(uuid(), 1, 'DateFormat', 'dd/MM/yyyy');
insert into configs(id, version, configname, configvalue) values(uuid(), 1, 'TimeFormat', 'HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(uuid(), 1, 'DateTimeFormat', 'dd/MM/yyyy HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(uuid(), 1, 'GoogleApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'AIInstance', 'OpenAIImpl', 'option of GoogleAIImpl/OpenAIImpl');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'codeModel', 'gpt-4o', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'adminModel', 'gpt-4o', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'assistantModel', 'gpt-4o', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'taskModel', 'gpt-4o', '');

insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'javamavenlinuxSandBoxUrl', 'http://localhost:8081/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'javagradlelinuxSandBoxUrl', 'http://localhost:8082/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'dotnetlinuxSandBoxUrl', 'http://localhost:8083/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'python3linuxSandBoxUrl', 'http://localhost:8084/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'nodejslinuxSandBoxUrl', 'http://localhost:8085/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'bashlinuxSandBoxUrl', 'http://localhost:8086/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'cmakegcclinuxSandBoxUrl', 'http://localhost:8087/api/aisandbox', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'taskSandBoxUrl', 'http://localhost:9011/api/aisandbox', '');

insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'email.username', '<username>', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'email.password', '<password>', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'mail.smtp.auth', 'true', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'mail.smtp.starttls.enable', 'true', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'mail.smtp.host', 'smtp.gmail.com', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'mail.smtp.port', '587', '');

insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'sessionExpireMinutes', '60', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'creditsExpireMonths', '6', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'topupOnRegister', '200', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'retryTimesOnLLMException', '4', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'firstWaitSecondsOnLLMException', '10', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'codeIterationRounds', '2', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'codeInterationDeep', '20', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'consumedCreditsOnEach', '20', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyMaintenance', '0', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyUsername', '0', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyIP', '0', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyRegion', '0', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyMaxRegisterNumber', '1', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'verifyMaxOnlineNumber', '1', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'maxRegisterNumber', '1000', '');
insert into configs(id, version, configname, configvalue, comments) values(uuid(), 1, 'maxOnlineNumber', '100', '');


create table chatrecord(
id               char(36)        not null primary key,
version          int             not null,
session          varchar(50)     not null,
isrequest        tinyint(1)      not null,
chattime         datetime        not null,
content          text            not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table coderecord(
id               char(36)        not null primary key,
version          int             not null,
session          varchar(50)     not null,
createtime       datetime        not null,
requirement      text            null,
content          text            null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table useraccount(
id               char(36)        not null primary key,
version          int             not null,
username         varchar(50)     not null unique,
encyptedpassword varchar(64)     not null,
registtime       datetime        not null,
ip               varchar(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table loginsession(
id               char(36)        not null primary key,
version          int             not null,
session          varchar(20)     not null unique,
accountid        char(36)        not null,
expiretime       datetime        not null,
ip               varchar(20),
isdeleted        tinyint(1)      not null default 0,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table chasedcredits(
id               char(36)        not null primary key,
version          int             not null,
accountid        char(36)        not null,
credits          int             not null default 0,
expiretime       datetime        not null,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table consumedcredits(
id               char(36)        not null primary key,
version          int             not null,
accountid        char(36)        not null,
credits          int             not null,
consumetime      datetime        not null,
foreign key (accountid) references useraccount(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
