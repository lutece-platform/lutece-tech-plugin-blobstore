-- liquibase formatted sql
-- changeset blobstore:create_db_blobstore.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Structure for table blobstore_blobstore
--
DROP TABLE IF EXISTS blobstore_blobstore;
CREATE TABLE blobstore_blobstore (
	id_blob VARCHAR(255) DEFAULT '' NOT NULL,
	blob_value LONG VARBINARY,
	PRIMARY KEY (id_blob) 
);
