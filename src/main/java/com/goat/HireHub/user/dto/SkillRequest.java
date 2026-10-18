package com.goat.HireHub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank(message = "Skill name cannot be empty") String name
) {}
