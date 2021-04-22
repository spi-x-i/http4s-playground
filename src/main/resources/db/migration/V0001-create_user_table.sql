CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "USER" (
  "ID" SERIAL PRIMARY KEY NOT NULL,
  "UUID" UUID NOT NULL UNIQUE DEFAULT uuid_generate_v1(),
  "CF" VARCHAR(255) NOT NULL UNIQUE,
  "NAME" VARCHAR(255) NOT NULL,
  "SURNAME" VARCHAR(255) NULL,
  "AGE" BIGINT NOT NULL,
  "CREATED_AT" TIMESTAMP NOT NULL,
  "CREATED_BY" VARCHAR(1024) NOT NULL,
  "LAST_UPDATED_AT" TIMESTAMP NOT NULL,
  "LAST_UPDATED_BY" VARCHAR(1024) NOT NULL,
  "DELETED_BY" VARCHAR(1024) NULL,
  "DELETED_AT" TIMESTAMP NULL
);

CREATE INDEX "USER_DELETED_AT_NOT_NULL" ON "USER" USING btree ("CF", "DELETED_AT");

CREATE INDEX "USER_DELETED_AT_NULL" ON "USER" USING btree ("SHOW_DEFINITION_UUID", "VIEWER_UUID");