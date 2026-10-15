package com.goat.HireHub.company;

import com.goat.HireHub.user.User;
import com.goat.HireHub.user.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "company_memberships",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "company_id"})})

public class CompanyMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public CompanyMembership() {}

    public CompanyMembership(User user, Company company, Role role) {
        this.user = user;
        this.company = company;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
