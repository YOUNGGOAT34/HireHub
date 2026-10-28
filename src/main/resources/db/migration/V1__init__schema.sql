
CREATE SEQUENCE IF NOT EXISTS companies_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jobs_seq START WITH 1 INCREMENT BY 50;

--create tables
-- companies
CREATE TABLE companies(
    company_id BIGSERIAL primary key,
    company_name VARCHAR(100) NOT NULL,
    company_description VARCHAR(200) NOT NULL
);

-- jobs
CREATE TABLE jobs(
    job_id BIGSERIAL primary key,
    job_title VARCHAR(150) NOT NULL,
    job_description VARCHAR(500) NOT NULL,
    salary NUMERIC(12,2) CHECK(salary>=0),
    company_id BIGSERIAL NOT NULL REFERENCES companies(company_id)
);

-- company_memberships
CREATE TABLE company_memberships(
    membership_id BIGSERIAL primary key,
    user_id BIGSERIAL NOT NULL REFERENCES users(user_id)
    company_id BIGSERIAL NOT NULL REFERENCES companies(company_id),
    role VARCHAR(50) NOT NULL

);