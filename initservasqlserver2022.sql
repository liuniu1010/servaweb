-- Create database with UTF-8 collation
CREATE DATABASE testenv COLLATE Latin1_General_100_CS_AS_SC_UTF8;
GO

-- Switch to the database
USE testenv;
GO

-- Create login and user
CREATE LOGIN testenvlogin WITH PASSWORD = 'abcd1234';
GO

CREATE USER testenvuser FOR LOGIN testenvlogin;
GO

-- Grant permissions
GRANT CONTROL ON DATABASE::testenv TO testenvuser;
GO

-- Create configs table
CREATE TABLE configs(
id                CHAR(36)       NOT NULL PRIMARY KEY,
version           INT            NOT NULL,
configname        VARCHAR(50)    NOT NULL,
configvalue       VARCHAR(100),
comments          VARCHAR(100),
CONSTRAINT uniqueno_element UNIQUE (configname)
);
GO

-- Insert config data
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'OpenAiApiKey', '<apiKey>');
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'DateFormat', 'dd/MM/yyyy');
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'TimeFormat', 'HH:mm:ss');
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'DateTimeFormat', 'dd/MM/yyyy HH:mm:ss');
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'GoogleApiKey', '<apiKey>');
INSERT INTO configs(id, version, configname, configvalue) VALUES(NEWID(), 1, 'X-RapidAPI-Proxy-Secret', '<secretKey>');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'AIInstance', 'OpenAIImpl', 'option of GoogleAIImpl/OpenAIImpl');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'codeModel', 'gpt-4.1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'adminModel', 'gpt-4.1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'assistantModel', 'gpt-4.1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'taskModel', 'gpt-4.1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'utilityModel', 'gpt-4.1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'speechToTextModel', 'whisper-1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'textToSpeechModel', 'tts-1-hd', '');

INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'javamavenlinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'javagradlelinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'dotnetlinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'python3linuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'nodejslinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'bashlinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'cmakegcclinuxSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'taskSandBoxUrl', 'http://<sandboxip>:9090/api/aisandbox', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'gameFactoryUrl', 'http://<gamefactoryip>:18080/api/v1/aigamefactory', '');

INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'email.username', '<username>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'email.password', '<password>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'mail.smtp.auth', 'true', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'mail.smtp.starttls.enable', 'true', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'mail.smtp.host', 'smtp.gmail.com', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'mail.smtp.port', '587', '');

INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'sessionExpireMinutes', '60', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'jobExpireMinutes', '30', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'creditsExpireMonths', '6', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'topupOnRegister', '300', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'retryTimesOnLLMException', '4', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'firstWaitSecondsOnLLMException', '10', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'codeIterationRounds', '2', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'codeInterationDeep', '30', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'consumedCreditsOnCoderBot', '20', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'consumedCreditsOnSpeechSplit', '10', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'consumedCreditsOnUtilityBot', '5', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'consumedCreditsOnChatWithAssistant', '0', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'consumedCreditsOnSpeechToText', '0', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'interfaceThreadPoolSize', '32', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyMaintenance', '0', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyUsername', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyIP', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyRegion', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyMaxRegisterNumber', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'verifyMaxOnlineNumber', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'maxRegisterNumber', '1000', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'maxOnlineNumber', '100', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'maxFileSizeForUpload', '10', 'unit M');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'blockExprTest', '1', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'paymentLinkOnStripe', '<paymentLinkOnStripe>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'paymentSigningSecretOnStripe', '<paymentSigningSecretOnStripe>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleUrl', '<OAuthGoogleUrl>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleClientID', '<OAuthGoogleClientID>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleClientSecret', '<OAuthGoogleClientSecret>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleRedirectUri', '<OAuthGoogleRedirectUri>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleTokenEndpoint', '<OAuthGoogleTokenEndpoint>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthGoogleUserInfoEndpoint', '<OAuthGoogleUserInfoEndpoint>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftUrl', '<OAuthMicrosoftUrl>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftClientID', '<OAuthMicrosoftClientID>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftClientSecret', '<OAuthMicrosoftClientSecret>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftRedirectUri', '<OAuthMicrosoftRedirectUri>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftTokenEndpoint', '<OAuthMicrosoftTokenEndpoint>', '');
INSERT INTO configs(id, version, configname, configvalue, comments) VALUES(NEWID(), 1, 'OAuthMicrosoftUserInfoEndpoint', '<OAuthMicrosoftUserInfoEndpoint>', '');
GO

-- Create chatrecord table
CREATE TABLE chatrecord(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
session          VARCHAR(50)     NOT NULL,
isrequest        BIT             NOT NULL,
chattime         DATETIME2       NOT NULL,
content          VARCHAR(MAX)    NOT NULL
);
GO

-- Create coderecord table
CREATE TABLE coderecord(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
session          VARCHAR(50)     NOT NULL,
createtime       DATETIME2       NOT NULL,
requirement      VARCHAR(MAX)    NULL,
content          VARCHAR(MAX)    NULL
);
GO

-- Create useraccount table
CREATE TABLE useraccount(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
username         VARCHAR(50)     NOT NULL UNIQUE,
encyptedpassword VARCHAR(64)     NOT NULL,
registtime       DATETIME2       NOT NULL,
ip               VARCHAR(20)
);
GO

-- Create loginsession table
CREATE TABLE loginsession(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
session          VARCHAR(20)     NOT NULL UNIQUE,
accountid        CHAR(36)        NOT NULL,
expiretime       DATETIME2       NOT NULL,
ip               VARCHAR(20),
isdeleted        BIT             NOT NULL DEFAULT 0,
FOREIGN KEY (accountid) REFERENCES useraccount(id)
);
GO

-- Create chasedcredits table
CREATE TABLE chasedcredits(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
accountid        CHAR(36)        NOT NULL,
credits          INT             NOT NULL DEFAULT 0,
createtime       DATETIME2       NOT NULL,
expiretime       DATETIME2       NOT NULL,
chasedsource     VARCHAR(20),
transactionid    VARCHAR(100)    UNIQUE,
FOREIGN KEY (accountid) REFERENCES useraccount(id)
);
GO

-- Create consumedcredits table
CREATE TABLE consumedcredits(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
accountid        CHAR(36)        NOT NULL,
credits          INT             NOT NULL,
consumetime      DATETIME2       NOT NULL,
consumefunction  VARCHAR(20),
FOREIGN KEY (accountid) REFERENCES useraccount(id)
);
GO

-- Create neojob table
CREATE TABLE neojob(
id               CHAR(36)        NOT NULL PRIMARY KEY,
version          INT             NOT NULL,
jobid            VARCHAR(10)     NOT NULL,
jobtype          VARCHAR(20)     NOT NULL,
jobstatus        VARCHAR(10)     NOT NULL,
jobparams        VARCHAR(MAX)    NOT NULL,
joboutcome       VARCHAR(MAX),
message          VARCHAR(MAX),
createtime       DATETIME2       NOT NULL,
expiretime       DATETIME2       NOT NULL,
CONSTRAINT uq_jobid UNIQUE (jobid)
);
GO
