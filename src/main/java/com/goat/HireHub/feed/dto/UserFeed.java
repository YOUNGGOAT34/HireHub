package com.goat.HireHub.feed.dto;

import java.math.BigDecimal;

public interface UserFeed {
    Long getJobId();
    String getJobTitle();
    BigDecimal getJobSalary();
    Long getCompanyId();
    String getCompanyName();
    Long getMatched();
    Long getRequired();
    Double getScore();
}
