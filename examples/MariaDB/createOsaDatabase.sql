CREATE DATABASE osa default charset utf8;
GRANT ALL ON osa.* TO osaAdmin@localhost IDENTIFIED BY 'osaAdmin';
GRANT ALL ON osa.* TO osaAdmin@'%' IDENTIFIED BY 'osaAdmin';
FLUSH PRIVILEGES;

CREATE TABLE osa.contexttime (organization VARCHAR(50) NOT NULL, contexttime BIGINT(20), PRIMARY KEY(organization));
CREATE TABLE osa.currentusers (user VARCHAR(100) NOT NULL, sessionid VARCHAR(100), PRIMARY KEY (user));
CREATE TABLE osa.idGen (pid VARCHAR(64) NOT NULL, highestID BIGINT (20), PRIMARY KEY(pid));

-- Set the initial value of automatically generated id, if needed - otherwise 0
-- INSERT INTO osa.idGen VALUES('OSA:root',10000);
