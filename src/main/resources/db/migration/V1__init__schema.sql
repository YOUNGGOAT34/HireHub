
CREATE SEQUENCE IF NOT EXISTS companies_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS jobs_seq START WITH 1 INCREMENT BY 50;

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