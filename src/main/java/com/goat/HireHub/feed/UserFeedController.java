package com.goat.HireHub.feed;

import com.goat.HireHub.feed.dto.UserFeedResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserFeedController {
    private final UserFeedService userFeedService;
    public UserFeedController(UserFeedService userFeedService){
         this.userFeedService=userFeedService;
    }
    @GetMapping("/me/feed")
    public ResponseEntity<Page<UserFeedResponse>> getRecommendedJobs(@RequestParam(defaultValue = "0") @PositiveOrZero(message = "page number must be zero or greater")int page,
                                                                     @RequestParam(defaultValue = "10") @Max(value =50) @Positive(message = "size must be greater than zero")int size){
        Pageable pageable= PageRequest.of(page,size);
        return ResponseEntity.ok(userFeedService.getRecommendedJobs(pageable));
    }
}
