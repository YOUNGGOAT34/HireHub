
CREATE SEQUENCE IF NOT EXISTS companies_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jobs_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS skills_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS company_memberships_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS educations_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS experiences_seq START WITH 1 INCREMENT BY 50;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

--create tables
-- companies
CREATE TABLE companies(
    company_id BIGINT primary key DEFAULT nextval('companies_seq'),
    company_name VARCHAR(100) NOT NULL,
    company_description VARCHAR(200) NOT NULL
);

-- jobs
CREATE TABLE jobs(
    job_id BIGINT primary key DEFAULT nextval('jobs_seq'),
    job_title VARCHAR(150) NOT NULL,
    job_description VARCHAR(500) NOT NULL,
    salary NUMERIC(12,2) CHECK(salary>=0),
    company_id BIGINT NOT NULL REFERENCES companies(company_id)
);

-- users
  CREATE TABLE users(
       user_id BIGINT primary key DEFAULT nextval('users_seq'),
       email VARCHAR(50) NOT NULL UNIQUE,
       first_name VARCHAR(50) NOT NULL,
       last_name VARCHAR(50) NOT NULL,
       username VARCHAR(50) UNIQUE,
       password_hash VARCHAR(72) NOT NULL
  );

-- company_memberships
CREATE TABLE company_memberships(
    membership_id BIGINT primary key DEFAULT nextval('company_memberships_seq'),
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
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
     skill_id BIGINT primary key DEFAULT nextval('skills_seq'),
     skill_name VARCHAR(50) NOT NULL UNIQUE
 );

 -- profile_skills

CREATE TABLE profile_skills(
    profile_id BIGINT REFERENCES candidate_profiles(profile_id) ON DELETE CASCADE,
    skill_id BIGINT REFERENCES skills(skill_id) ON DELETE CASCADE,
    primary key(profile_id,skill_id)
);

-- educations
CREATE TABLE educations(
    education_id BIGINT primary key DEFAULT nextval('educations_seq'),
    profile_id BIGINT NOT NULL references candidate_profiles(profile_id) ON DELETE CASCADE,
    institution_name VARCHAR(50) NOT NULL,
    degree_type VARCHAR(50) NOT NULL,
    field_of_study VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    activities_and_societies TEXT
);

-- experiences
CREATE TABLE experiences(
   experience_id BIGINT primary key DEFAULT nextval('experiences_seq'),
   profile_id BIGINT NOT NULL REFERENCES candidate_profiles(profile_id) ON DELETE CASCADE,
   job_title VARCHAR(50) NOT NULL,
   company_id BIGINT REFERENCES companies(company_id),
   company_name_raw VARCHAR(100),
   start_date DATE NOT NULL,
   end_date DATE,
   is_current BOOLEAN NOT NULL DEFAULT FALSE,
   description TEXT NOT NULL
);


-- indexes
-- jobs
CREATE index idx_jobs_salary ON jobs (salary DESC NULLS last);
CREATE index idx_companies_id ON jobs (company_id);
CREATE INDEX idx_jobs_title_trgm ON jobs USING gin (job_title gin_trgm_ops);

-- companies
create index company_name_idx on companies (company_name);
CREATE INDEX idx_company_name_trgm ON companies USING gin (company_name gin_trgm_ops);

-- users
CREATE INDEX idx_users_first_name_id ON users (first_name, user_id);
CREATE INDEX idx_users_last_name_id ON users (last_name, user_id);
CREATE INDEX idx_users_username_trgm   ON users USING gin (lower(username)   gin_trgm_ops);
CREATE INDEX idx_users_first_name_trgm ON users USING gin (lower(first_name) gin_trgm_ops);
CREATE INDEX idx_users_last_name_trgm  ON users USING gin (lower(last_name)  gin_trgm_ops);
