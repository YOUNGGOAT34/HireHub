package com.goat.HireHub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final int COMPANIES = 3_000;
    private static final int USERS = 50_000;
    private static final int JOBS = 50_000;
    private static final int BATCH = 1_000;

    private static final String[] FIRST = {"Amina","Brian","Cynthia","David","Esther","Felix","Grace","Hassan","Irene","James","Kevin","Lucy","Mark","Nancy","Omar","Peter"};
    private static final String[] LAST = {"Mwangi","Otieno","Kamau","Wanjiru","Ochieng","Njoroge","Achieng","Kiptoo","Mutua","Hussein","Naliaka","Kariuki"};
    private static final String[] TITLES = {"Software Engineer","Backend Developer","Frontend Developer","Data Analyst","Data Scientist","DevOps Engineer","Product Manager","QA Engineer","UI/UX Designer","Mobile Developer","Accountant","Sales Executive","Marketing Manager","HR Officer","Project Manager"};
    private static final String[] LEVELS = {"Junior","Mid-level","Senior","Lead","Intern"};
    private static final String[] INDUSTRIES = {"fintech","healthcare","logistics","e-commerce","education","agritech","telecom","energy"};
    private static final String[] LOCATIONS = {"Nairobi","Kampala","Arusha","Lagos","Cairo","Washington","LA","San francisco"};
    private static final int CANDIDATES = 10_000;
    private static final String[] SEARCH_STATUS = {"ACTIVELY_LOOKING", "OPEN_TO_OFFERS", "NOT_LOOKING"};
    private static final String[] EMPLOYMENT_TYPE = {"PART_TIME","CONTRACT","INTERNSHIP","FULL_TIME","VOLUNTEER"};
    private static final String[] Experience_LEVEL = {"ENTRY_LEVEL","SENIOR","MANAGER","DIRECTOR","EXECUTIVE"};
    private static final String[] WORKPLACE_TYPE = {"HYBRID","ON_SITE","REMOTE"};

    private static final String RESPONSIBILITY = "RESPONSIBILITY";
    private static final String QUALIFICATION  = "QUALIFICATION";
    private static final String NICE_TO_HAVE   = "NICE_TO_HAVE";

    private static final String[] RESPONSIBILITIES = {
            "Deliver high-quality work and meet agreed deadlines",
            "Collaborate with colleagues across teams to solve problems",
            "Take ownership of tasks from planning through delivery",
            "Document your work and share knowledge with the team",
            "Take part in reviews and help improve how the team works",
            "Communicate progress and risks clearly to stakeholders"
    };

    // Plain requirement sentences per role. Keys must match TITLES exactly.
    // These are only text for the job_requirements table; nothing here is inserted
    // into skills or job_skills.
    private static final Map<String, List<String>> QUALIFICATIONS_BY_ROLE = Map.ofEntries(
            Map.entry("Software Engineer", List.of("3+ years building backend services in Java and Spring Boot", "Strong SQL skills with PostgreSQL", "Experience designing and consuming REST APIs", "Comfortable working with Docker and Git")),
            Map.entry("Backend Developer", List.of("Experience building microservices with Java and Spring Boot", "Solid knowledge of PostgreSQL and Redis", "Experience with REST API design", "Familiarity with message brokers such as Kafka")),
            Map.entry("Frontend Developer", List.of("Strong JavaScript and TypeScript skills", "Production experience with React", "Solid HTML and CSS fundamentals", "Experience using Git in a team")),
            Map.entry("Data Analyst", List.of("Advanced SQL for querying and reporting", "Experience building dashboards in Tableau or Power BI", "Strong Excel skills", "Working knowledge of Python for data analysis")),
            Map.entry("Data Scientist", List.of("Strong Python skills with Pandas and NumPy", "Experience training machine learning models", "Solid grounding in statistics", "Good SQL skills")),
            Map.entry("DevOps Engineer", List.of("Experience with Docker and Kubernetes", "Hands-on experience with AWS", "Infrastructure as code using Terraform", "Experience building CI/CD pipelines")),
            Map.entry("Product Manager", List.of("Experience owning a product roadmap", "Comfortable working in Agile teams", "Strong stakeholder management", "Ability to analyse product data and run A/B tests")),
            Map.entry("QA Engineer", List.of("Experience writing automated tests with Selenium", "Knowledge of API testing with Postman", "Experience with JUnit", "Comfortable using Jira to track defects")),
            Map.entry("UI/UX Designer", List.of("Strong portfolio built with Figma", "Experience creating wireframes and prototypes", "Experience running user research", "Knowledge of design systems")),
            Map.entry("Mobile Developer", List.of("Experience building Android apps in Kotlin", "Experience with Flutter", "Experience integrating REST APIs", "Familiarity with Firebase")),
            Map.entry("Accountant", List.of("Solid bookkeeping experience", "Proficiency in QuickBooks", "Experience preparing financial reports", "Knowledge of tax compliance")),
            Map.entry("Sales Executive", List.of("Proven record in lead generation", "Experience managing accounts in a CRM", "Strong negotiation skills", "Confident presenting to clients")),
            Map.entry("Marketing Manager", List.of("Experience running SEO and content marketing", "Proficiency in Google Analytics", "Experience managing social media campaigns", "Strong brand strategy skills")),
            Map.entry("HR Officer", List.of("Experience with recruitment and onboarding", "Knowledge of payroll processing", "Understanding of labor law", "Strong employee relations skills")),
            Map.entry("Project Manager", List.of("Experience delivering projects with Scrum", "Strong risk management skills", "Experience managing budgets", "Proficiency in Jira"))
    );

    private static final Map<String, List<String>> NICE_TO_HAVE_BY_ROLE = Map.ofEntries(
            Map.entry("Software Engineer", List.of("Experience with Kubernetes", "Exposure to cloud platforms such as AWS")),
            Map.entry("Backend Developer", List.of("Experience with AWS", "Knowledge of Kubernetes")),
            Map.entry("Frontend Developer", List.of("Experience with Tailwind CSS", "Knowledge of accessibility standards")),
            Map.entry("Data Analyst", List.of("Familiarity with R", "Exposure to data warehousing")),
            Map.entry("Data Scientist", List.of("Experience with TensorFlow", "Knowledge of big data tools such as Spark")),
            Map.entry("DevOps Engineer", List.of("Experience with Jenkins", "Knowledge of Ansible")),
            Map.entry("Product Manager", List.of("Experience with Figma", "Background in fintech")),
            Map.entry("QA Engineer", List.of("Experience with Cypress", "Knowledge of performance testing")),
            Map.entry("UI/UX Designer", List.of("Experience with Adobe XD", "Basic knowledge of HTML and CSS")),
            Map.entry("Mobile Developer", List.of("Experience with Swift", "Knowledge of CI/CD for mobile apps")),
            Map.entry("Accountant", List.of("Experience with SAP", "Knowledge of IFRS")),
            Map.entry("Sales Executive", List.of("Experience with HubSpot", "Fluency in Swahili")),
            Map.entry("Marketing Manager", List.of("Experience with email automation tools", "Knowledge of paid advertising")),
            Map.entry("HR Officer", List.of("Experience with HR software", "Knowledge of performance management frameworks")),
            Map.entry("Project Manager", List.of("PMP certification", "Experience with Confluence"))
    );

    private final JdbcTemplate jdbc;
    private final Random rnd = new Random(42);
    private final String PASSWORD_HASH;

    public DataSeeder(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.PASSWORD_HASH = passwordEncoder.encode("password");
    }

    @Override
    @Transactional
    public void run(String... args) {
        Long existing = jdbc.queryForObject("SELECT count(*) FROM companies", Long.class);
        if (existing != null && existing > 0) {
            System.out.println("Seed skipped: data already exists.");
            return;
        }
        long start = System.currentTimeMillis();

        seedCompanies();
        seedUsers();
        seedMemberships();
        seedJobs();                // also seeds requirements; never touches skills or job_skills
        jdbc.execute("ANALYZE");   // refresh planner statistics
        System.out.println("Seeding done in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void seedCompanies() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 1; i <= COMPANIES; i++) {
            String industry = INDUSTRIES[rnd.nextInt(INDUSTRIES.length)];
            rows.add(new Object[]{
                    "Company " + i,
                    "A " + industry + " company based in Nairobi, number " + i
            });
        }
        batch("INSERT INTO companies(company_name, company_description) VALUES (?, ?)", rows);
    }

    private void seedUsers() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 1; i <= USERS; i++) {
            rows.add(new Object[]{
                    "user" + i + "@example.com",        // unique
                    FIRST[rnd.nextInt(FIRST.length)],
                    LAST[rnd.nextInt(LAST.length)],
                    "user" + i,                         // unique
                    PASSWORD_HASH
            });
        }
        batch("INSERT INTO users(email, first_name, last_name, username, password_hash) VALUES (?, ?, ?, ?, ?)", rows);
    }

    private void seedJobs() {
        List<Long> companyIds = jdbc.queryForList("SELECT company_id FROM companies", Long.class);
        List<Object[]> jobRows = new ArrayList<>();
        List<String> rolesPerJob = new ArrayList<>();   // same order as the jobs

        for (int i = 1; i <= JOBS; i++) {
            String role = TITLES[rnd.nextInt(TITLES.length)];
            rolesPerJob.add(role);
            String title = LEVELS[rnd.nextInt(LEVELS.length)] + " " + role;
            String experience = Experience_LEVEL[rnd.nextInt(Experience_LEVEL.length)];
            String employmentType = EMPLOYMENT_TYPE[rnd.nextInt(EMPLOYMENT_TYPE.length)];
            String workPlaceType = WORKPLACE_TYPE[rnd.nextInt(WORKPLACE_TYPE.length)];
            String url = "https://example.com/jobs/" + i;
            String location = LOCATIONS[rnd.nextInt(LOCATIONS.length)];
            BigDecimal salary = rnd.nextInt(10) == 0
                    ? null
                    : BigDecimal.valueOf(30_000 + rnd.nextInt(220_000));

            jobRows.add(new Object[]{
                    title,
                    "We are hiring a " + title + ". Join our team and help us deliver great work. Ref " + i,
                    salary,
                    companyIds.get(rnd.nextInt(companyIds.size())),
                    location,
                    experience,
                    employmentType,
                    workPlaceType,
                    url
            });
        }
        batch("INSERT INTO jobs(job_title, job_description, salary, company_id, location, experience_level, employment_type, workplace_type, job_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", jobRows);

        List<Long> jobIds = jdbc.queryForList("SELECT job_id FROM jobs ORDER BY job_id", Long.class);
        seedRequirements(jobIds, rolesPerJob);
    }

    private void batch(String sql, List<Object[]> rows) {
        jdbc.batchUpdate(sql, rows, BATCH, (ps, row) -> {
            for (int i = 0; i < row.length; i++) {
                ps.setObject(i + 1, row[i]);
            }
        });
    }

    private void seedMemberships() {
        List<Long> companyIds = jdbc.queryForList(
                "SELECT company_id FROM companies ORDER BY company_id", Long.class);
        List<Long> userIds = jdbc.queryForList(
                "SELECT user_id FROM users ORDER BY user_id OFFSET ?", Long.class, CANDIDATES);

        List<Object[]> rows = new ArrayList<>();
        for (int c = 0; c < companyIds.size(); c++) {
            int base = c * 3;
            if (base + 2 >= userIds.size()) break;      // ran out of users
            rows.add(new Object[]{userIds.get(base),     companyIds.get(c), "ADMIN"});
            rows.add(new Object[]{userIds.get(base + 1), companyIds.get(c), "RECRUITER"});
            rows.add(new Object[]{userIds.get(base + 2), companyIds.get(c), "RECRUITER"});
        }
        batch("INSERT INTO company_memberships(user_id, company_id, role) VALUES (?, ?, ?)", rows);
    }

    private void seedRequirements(List<Long> jobIds, List<String> rolesPerJob) {
        List<Object[]> rows = new ArrayList<>();

        for (int k = 0; k < jobIds.size(); k++) {
            Long jobId = jobIds.get(k);
            String role = rolesPerJob.get(k);
            int order = 1;                                   // one counter per job, all types

            // responsibilities: 1 role line + 2 random generic ones
            rows.add(new Object[]{jobId, order++, RESPONSIBILITY,
                    "Own the day-to-day responsibilities of the " + role + " role"});
            List<String> general = new ArrayList<>(Arrays.asList(RESPONSIBILITIES));
            Collections.shuffle(general, rnd);
            for (String r : general.subList(0, 2)) {
                rows.add(new Object[]{jobId, order++, RESPONSIBILITY, r});
            }

            // qualifications: 3 of the 4 sentences written for this role
            List<String> quals = new ArrayList<>(QUALIFICATIONS_BY_ROLE.get(role));
            Collections.shuffle(quals, rnd);
            for (String q : quals.subList(0, 3)) {
                rows.add(new Object[]{jobId, order++, QUALIFICATION, q});
            }

            // nice to have: 1 or 2 sentences written for this role
            List<String> nice = new ArrayList<>(NICE_TO_HAVE_BY_ROLE.get(role));
            Collections.shuffle(nice, rnd);
            for (String n : nice.subList(0, 1 + rnd.nextInt(2))) {
                rows.add(new Object[]{jobId, order++, NICE_TO_HAVE, n});
            }
        }

        batch("INSERT INTO job_requirements(job_id, display_order, requirement_type, content) VALUES (?, ?, ?, ?)",
                rows);
    }
}