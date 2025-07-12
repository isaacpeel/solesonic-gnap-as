--liquibase formatted sql

--changeset gnap-as:000
--comment: Create UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--changeset gnap-as:001
--comment: Create client table
CREATE TABLE IF NOT EXISTS client (
    id uuid default uuid_generate_v4() not null primary key
    instance_id VARCHAR(255),
    display_name VARCHAR(255),
    key_id VARCHAR(255),
    key_jwk TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

--changeset gnap-as:002
--comment: Create grant_request table
CREATE TABLE IF NOT EXISTS grant_request (
    id uuid default uuid_generate_v4() not null primary key
    client_id VARCHAR(36),
    status VARCHAR(20) NOT NULL,
    redirect_uri VARCHAR(2048),
    state VARCHAR(255),
    user_id VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_grant_client FOREIGN KEY (client_id) REFERENCES client(id)
);

--changeset gnap-as:003
--comment: Create access_token table
CREATE TABLE IF NOT EXISTS access_token (
    id uuid default uuid_generate_v4() not null primary key
    grant_id VARCHAR(36) NOT NULL,
    token_value VARCHAR(2048) NOT NULL,
    access_type VARCHAR(50) NOT NULL,
    resource_server VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_token_grant FOREIGN KEY (grant_id) REFERENCES grant_request(id)
);

--changeset gnap-as:004
--comment: Create interaction table
CREATE TABLE IF NOT EXISTS interaction (
    id uuid default uuid_generate_v4() not null primary key
    grant_id VARCHAR(36) NOT NULL,
    interaction_type VARCHAR(50) NOT NULL,
    interaction_url VARCHAR(2048),
    nonce VARCHAR(255),
    hash_method VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_interaction_grant FOREIGN KEY (grant_id) REFERENCES grant_request(id)
);

--changeset gnap-as:005
--comment: Create resource table
CREATE TABLE IF NOT EXISTS resource (
    id uuid default uuid_generate_v4() not null primary key
    grant_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    resource_server VARCHAR(255),
    actions VARCHAR(1024),
    locations VARCHAR(1024),
    data_types VARCHAR(1024),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_resource_grant FOREIGN KEY (grant_id) REFERENCES grant_request(id)
);
