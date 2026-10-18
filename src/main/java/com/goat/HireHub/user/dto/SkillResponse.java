package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.Skill;

public record SkillResponse(Long id, String name) {
    public static SkillResponse from(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName());
    }
}
