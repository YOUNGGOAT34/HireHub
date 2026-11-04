package com.goat.HireHub.feed.dto;

import java.math.BigDecimal;

public record UserFeedResponse(Long jobId,
                               String jobTitle,
                               BigDecimal jobSalary,
                               Long companyId,
                               String companyName,
                               String matching,
                               String missing,
                               Double scorePercentage
                               ) {
     public static UserFeedResponse from(UserFeed feed,SkillBreakDown breakdown){
         return new UserFeedResponse(feed.getJobId(),
                 feed.getJobTitle(),
                 feed.getJobSalary(),
                 feed.getCompanyId(),
                 feed.getCompanyName(),
                 breakdown.getMatching(),
                 breakdown.getMissing(),
                 calculatePercentage(feed.getScore())
                 );
     }

     public static Double calculatePercentage(Double score){
          return score==null?0.0:score*100;
     }
}
