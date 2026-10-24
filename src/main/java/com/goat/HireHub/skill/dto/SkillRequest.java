package com.goat.HireHub.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank(message = "Skill name cannot be empty") @Size(max=100,message = "skill name cannot exceed 100 characters") String name
) {}
