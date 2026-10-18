package com.goat.HireHub.user;

import jakarta.persistence.*;

@Entity
@Table(name="skills")
public class Skill {
      @Id
      @GeneratedValue(strategy = GenerationType.SEQUENCE)
      @Column(name="skill_id")
      private Long id;
      @Column(name="skill_name")
      private String name;

      public Skill(){}

    public Skill(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
