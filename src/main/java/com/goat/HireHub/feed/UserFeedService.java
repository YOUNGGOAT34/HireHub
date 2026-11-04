package com.goat.HireHub.feed;
import com.goat.HireHub.feed.dto.UserFeedResponse;
import com.goat.HireHub.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
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
        return userFeedRepository.recommendedJobs(pageable,loggedUserId).map(UserFeedResponse::from);
    }
}
