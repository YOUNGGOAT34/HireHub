package com.goat.HireHub.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goat.HireHub.company.Company;
import jakarta.persistence.*;


@Entity
@Table(name="users")
public class User {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;
    @Column(name="email",nullable = false,unique = true)
    private String email;
    @Column(name="first_name",nullable = false)
    private String firstName;
    @Column(name="last_name",nullable = false)
    private String lastName;
    @Column(name="username",unique = true)
    private String username;
    @Column(name="password",nullable = false)
    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;
    public  User(){}

    public User(String email, String firstName, String lastName,String passwordHash,String username) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username=username;
        this.passwordHash=passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword(){
         return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash){
         this.passwordHash=passwordHash;
    }
}
