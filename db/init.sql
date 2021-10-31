CREATE TABLE credentials
(
    id            BIGSERIAL NOT NULL,
    client_id     VARCHAR(255),
    client_secret VARCHAR(255),
    CONSTRAINT pk_credentials PRIMARY KEY (id)
);

CREATE TABLE applications
(
    id             BIGSERIAL NOT NULL,
    name           VARCHAR(255),
    credentials_id BIGINT,
    CONSTRAINT pk_applications PRIMARY KEY (id)
);

ALTER TABLE applications
    ADD CONSTRAINT FK_APPLICATIONS_ON_CREDENTIALS FOREIGN KEY (credentials_id) REFERENCES credentials (id);

ALTER TABLE credentials REPLICA IDENTITY FULL;
ALTER TABLE applications REPLICA IDENTITY FULL;

CREATE ROLE cdc REPLICATION LOGIN PASSWORD 'cdc_pwd';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO cdc;

CREATE PUBLICATION "cdc-publication" FOR TABLE applications, credentials
