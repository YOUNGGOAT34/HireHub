package com.goat.HireHub.skill;

import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService){
         this.skillService=skillService;
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<SkillResponse>> getUserSkills(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        return ResponseEntity.ok(skillService.getSkillsByProfileId(id));
    }

    @PostMapping("/me/skills")
    public  ResponseEntity<SkillResponse> addSkillToProfile(@RequestBody @Valid SkillRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.addSkillToProfile(request));
    }

    @DeleteMapping("/me/skills/{id}")
    public ResponseEntity<Void> removeSkillFromProfile(@PathVariable @Positive(message = "id must be greater than zero")Long id){
        skillService.removeSkillFromProfile(id);
        return ResponseEntity.noContent().build();
    }
}

