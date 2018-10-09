DROP TABLE tags_dag;
DROP TABLE categories;
DROP TABLE tags;
DROP TABLE participants;
DROP TABLE sessions;
DROP TABLE users;
DROP TABLE reset_codes;
DROP TABLE inactive_users;

CREATE TABLE users (
	id serial PRIMARY KEY,
  name varchar(20) NOT NULL,
  surname varchar(40) NOT NULL,
	email varchar(100) NOT NULL,
	password char(64) NOT NULL,
  start_date timestamp NOT NULL
);

CREATE TABLE inactive_users (
  name varchar(20) NOT NULL,
  surname varchar(40) NOT NULL,
  email varchar(100) PRIMARY KEY,
  password char(64) NOT NULL,
  start_date timestamp NOT NULL,
  hash char(64) NOT NULL
);

CREATE INDEX inactive_users_time_pkey ON inactive_users (start_date ASC);

CREATE TABLE sessions (
	id serial PRIMARY KEY,
	title varchar(140) NOT NULL,
	due_date date NOT NULL,
	due_time time NOT NULL,
	duration integer NOT NULL,
	people_limit integer NOT NULL,
	place varchar(140) NOT NULL,
	description varchar(280) NOT NULL,
	author integer REFERENCES users NOT NULL
);

CREATE TABLE participants (
	user_id integer REFERENCES users NOT NULL,
	session_id integer REFERENCES sessions NOT NULL,
	PRIMARY KEY (user_id, session_id)
);

CREATE TABLE tags (
	id serial PRIMARY KEY,
	name varchar(70) NOT NULL
);

CREATE TABLE categories (
	tag_id integer REFERENCES tags NOT NULL,
	session_id integer REFERENCES sessions NOT NULL,
	PRIMARY KEY (tag_id, session_id)
);

CREATE TABLE tags_dag (
  parent integer REFERENCES tags NOT NULL,
  child integer REFERENCES tags NOT NULL,
  PRIMARY KEY (parent, child)
);

CREATE TABLE reset_codes (
  email varchar(100) PRIMARY KEY,
  code char(64) NOT NULL,
  sent_date timestamp NOT NULL
);

CREATE INDEX reset_codes_time_pkey ON reset_codes (sent_date ASC);

CREATE TABLE ignored_tags (
	user_id integer REFERENCES users NOT NULL,
	tag_id integer REFERENCES tags NOT NULL,
	PRIMARY KEY (user_id, tag_id)
);

\i triggers.sql
