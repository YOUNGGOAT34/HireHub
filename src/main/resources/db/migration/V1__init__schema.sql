
CREATE SEQUENCE IF NOT EXISTS companies_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jobs_seq START WITH 1 INCREMENT BY 50;
CREATE SECOND IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;
CREATE SECOND IF NOT EXISTS skills_seq START WITH 1 INCREMENT BY 50;
CREATE SECOND IF NOT EXISTS company_memberships_seq START WITH 1 INCREMENT BY 50

--create tables
-- companies
CREATE TABLE companies(
    company_id BIGINT primary key,
    company_name VARCHAR(100) NOT NULL,
    company_description VARCHAR(200) NOT NULL
);

-- jobs
CREATE TABLE jobs(
    job_id BIGINT primary key,
    job_title VARCHAR(150) NOT NULL,
    job_description VARCHAR(500) NOT NULL,
    salary NUMERIC(12,2) CHECK(salary>=0),
    company_id BIGINT NOT NULL REFERENCES companies(company_id)
);

-- users

  CREATE TABLE users(
       user_id BIGINT primary key,
       email VARCHAR(50) NOT NULL UNIQUE,
       first_name VARCHAR(50) NOT NULL,
       last_name VARCHAR(50) NOT NULL,
       username VARCHAR(50) UNIQUE,
       password_hash VARCHAR(72) NOT NULL
  );

-- company_memberships
CREATE TABLE company_memberships(
    membership_id BIGINT primary key,
    user_id BIGINT NOT NULL REFERENCES users(user_id)
    company_id BIGINT NOT NULL REFERENCES companies(company_id),
    role VARCHAR(50) NOT NULL

);

-- candidate_profiles
CREATE TABLE candidate_profiles(
     profile_id BIGINT primary key REFERENCES users(user_id),
     headline VARCHAR (100),
     summary TEXT,
     search_status VARCHAR(50)
 );

-- skills
CREATE TABLE skills(
     skill_id BIGINT primary key,
     skill_name VARCHAR(50) NOT NULL UNIQUE
 );

 -- profile_skills

CREATE TABLE profile_skills(
    profile_id BIGINT REFERENCES candidate_profiles(profile_id),
    skill_id BIGINT REFERENCES skills(skill_id),
    primary key(profile_id,skill_id)
);







