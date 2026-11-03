package com.goat.HireHub.user.dto;

import java.math.BigDecimal;

public interface UserFeed {
      Long getJobId();
      String getJobTitle();
      BigDecimal getJobSalary();
      Long getCompanyId();
      String getCompanyName();
      Integer getMatched();
      Integer getRequired();
      Double getScore();
}
