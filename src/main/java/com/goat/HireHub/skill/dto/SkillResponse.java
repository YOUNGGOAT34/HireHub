package com.goat.HireHub.skill.dto;

import com.goat.HireHub.skill.Skill;

public record SkillResponse(Long id, String name) {
    public static SkillResponse from(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName());
    }
}
