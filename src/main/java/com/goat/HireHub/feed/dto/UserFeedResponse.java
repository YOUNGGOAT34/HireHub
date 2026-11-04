package com.goat.HireHub.feed.dto;

import java.math.BigDecimal;

public record UserFeedResponse(Long jobId,
                               String jobTitle,
                               BigDecimal jobSalary,
                               Long companyId,
                               String companyName,
                               Long matched,
                               Long required,
                               Double scorePercentage
                               ) {
     public static UserFeedResponse from(UserFeed feed){
         return new UserFeedResponse(feed.getJobId(),
                 feed.getJobTitle(),
                 feed.getJobSalary(),
                 feed.getCompanyId(),
                 feed.getCompanyName(),
                 feed.getMatched(),
                 feed.getRequired(),
                 calculatePercentage(feed.getScore())
                 );
     }

     public static Double calculatePercentage(Double score){
          return score==null?0.0:score*100;
     }
}
