package com.goat.HireHub.job.gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {
    private final Client client;
    private final String model;
    private final RequirementExtractor requirementExtractor;
    private static final String SYSTEM_PROMPT = """
    You extract skills from job requirements.
    Rules:
    - Use ONLY information present in the text. Do not invent skills.
    - Return short, normalized names (e.g. "Spring Boot", not "experience with Spring Boot").
    - Include technical skills, tools, languages, and key soft skills.
    - Remove duplicates.
    - Return ONLY valid JSON in exactly this shape:
    { "skills": [string] }
    """;

    public GeminiService(@Value("${gemini.model}") String model,Client client,RequirementExtractor requirementExtractor){
         this.client=client;
         this.model=model;
         this.requirementExtractor =requirementExtractor;
    }

    public String getSkills(Long id){
        String requirements= requirementExtractor.extractJobRequirements(id);
        GenerateContentConfig config= GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_PROMPT)))
                .responseMimeType("application/json")
                .temperature(0.0f)
                .build();
        try{
            GenerateContentResponse response=client.models.generateContent(model,requirements,config);
            return response.text();
        }catch (Exception e){
          throw new RuntimeException("Skill extraction failed");
        }

    }
}
