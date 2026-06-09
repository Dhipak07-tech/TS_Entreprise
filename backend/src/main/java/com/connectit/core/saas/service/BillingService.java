package com.connectit.core.saas.service;

import com.connectit.core.saas.entity.Subscription;
import com.connectit.core.saas.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BillingService {

    private final SubscriptionRepository subscriptionRepository;

    public BillingService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription getActiveSubscription() {
        List<Subscription> subs = subscriptionRepository.findAll();
        if (subs.isEmpty()) {
            Subscription defaultSub = Subscription.builder()
                    .planTier("FREE")
                    .status("ACTIVE")
                    .billingCycle("MONTHLY")
                    .currentPeriodStart(LocalDateTime.now())
                    .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                    .build();
            return subscriptionRepository.save(defaultSub);
        }
        return subs.get(0);
    }

    public Subscription upgradeSubscription(String tier) {
        Subscription active = getActiveSubscription();
        active.setPlanTier(tier.toUpperCase());
        active.setUpdatedAt(LocalDateTime.now());
        active.setCurrentPeriodStart(LocalDateTime.now());
        active.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        return subscriptionRepository.save(active);
    }
}
