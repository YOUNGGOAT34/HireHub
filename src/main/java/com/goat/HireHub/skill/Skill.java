package com.goat.HireHub.skill;

import jakarta.persistence.*;

@Entity
@Table(name="skills")
public class Skill {
      @Id
      @GeneratedValue(strategy = GenerationType.SEQUENCE)
      @Column(name="skill_id")
      private Long id;
      @Column(name="skill_name",nullable = false,unique = true)
      private String name;

      public Skill(){}

    public Skill(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Skill(String name){
           this.name=name;
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Skill s && name != null && name.equalsIgnoreCase(s.name);
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.toLowerCase().hashCode();
    }
}
