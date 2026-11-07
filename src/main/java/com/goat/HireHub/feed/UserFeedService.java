package com.goat.HireHub.feed;
import com.goat.HireHub.feed.dto.SkillBreakDown;
import com.goat.HireHub.feed.dto.UserFeed;
import com.goat.HireHub.feed.dto.UserFeedResponse;
import com.goat.HireHub.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserFeedService {
    private final SecurityUtils securityUtils;
    private final UserFeedRepository userFeedRepository;

    public UserFeedService(SecurityUtils securityUtils,UserFeedRepository userFeedRepository){
        this.securityUtils=securityUtils;
        this.userFeedRepository=userFeedRepository;
    }
    //feed
    public Page<UserFeedResponse> getRecommendedJobs(Pageable pageable){
        Long loggedUserId=securityUtils.getCurrentUser().getId();
        Page<UserFeed> recommendedJobsPage=userFeedRepository.getRecommendedJobs(pageable,loggedUserId);

        if(recommendedJobsPage.isEmpty()){
            return Page.empty(pageable);
        }
        List<Long> jobIds=recommendedJobsPage.getContent().stream()
                .map(UserFeed::getJobId)
                .toList();
        List<SkillBreakDown> breakdown=userFeedRepository.getSkillBreakdown(jobIds,loggedUserId);

        Map<Long,SkillBreakDown> breakDownMap=breakdown.stream().collect(
                 Collectors.toMap(SkillBreakDown::getJobId,breakDown->breakDown));


        return recommendedJobsPage.map(
                 feed->{
                     SkillBreakDown skillBreakDown=breakDownMap.get(feed.getJobId());
                     return UserFeedResponse.from(feed,skillBreakDown);
                 }
        );
    }
}
