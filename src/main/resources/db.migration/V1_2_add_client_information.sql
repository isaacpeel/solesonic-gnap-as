--liquibase formatted sql

--changeset gnap-as:006
--comment: Create client_information table
CREATE TABLE IF NOT EXISTS client_information (
    id uuid PRIMARY KEY,
    name VARCHAR(255),
    uri VARCHAR(2048),
    logo_uri VARCHAR(2048),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

--changeset gnap-as:007
--comment: Add client_information_id column to client table
ALTER TABLE client ADD COLUMN client_information_id uuid;
ALTER TABLE client ADD CONSTRAINT fk_client_information FOREIGN KEY (client_information_id) REFERENCES client_information(id);
