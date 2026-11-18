package com.goat.HireHub.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.List;

@org.springframework.context.annotation.Configuration
@EnableCaching
@EnableScheduling
public class CacheConfiguration {
     @Bean
    public CacheManager cacheManager(){
         SimpleCacheManager manager=new SimpleCacheManager();
         manager.setCaches(
                 List.of(
                         builder("jobSearch",1000,Duration.ofSeconds(60)),
                         builder("companySearch",500,Duration.ofSeconds(60)),
                         builder("companyMembers",1000,Duration.ofMinutes(15)),
                         builder("companies",500,Duration.ofMinutes(10)),
                         builder("jobs",1000,Duration.ofMinutes(10)),
                         builder("job",1000,Duration.ofMinutes(10)),
                         builder("authUsers",1000,Duration.ofMinutes(5))
                 )
         );
         return manager;
     }


     private CaffeineCache builder(String name, int maxSize, Duration ttl){
          return new CaffeineCache(name, Caffeine.newBuilder()
                  .maximumSize(maxSize)
                  .expireAfterWrite(ttl)
                  .build()
          );
     }
}
