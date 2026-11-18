package com.goat.HireHub.config;

import com.goat.HireHub.company.membership.MemberAddedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

@Service
public class CacheEviction {
     private final CacheManager cacheManager;

     public CacheEviction(CacheManager cacheManager){
         this.cacheManager=cacheManager;
     }

     public void evictCompanyMembers(Long companyId) {

         if (cacheManager.getCache("companyMembers") instanceof CaffeineCache cache) {
             cache.getNativeCache().asMap().keySet()
                     .removeIf(k -> k instanceof List<?> list &&!list.isEmpty()&& Objects.equals(list.get(0), companyId));
         }
     }
     @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true)
     public void handleMemberAddedEvent(MemberAddedEvent event){
         evictCompanyMembers(event.id());
     }
}
