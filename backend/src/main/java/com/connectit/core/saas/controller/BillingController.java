package com.connectit.core.saas.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.saas.entity.Subscription;
import com.connectit.core.saas.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saas/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<Subscription>> getSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription details retrieved successfully", billingService.getActiveSubscription()));
    }

    @PostMapping("/subscription/upgrade")
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Subscription>> upgradeSubscription(@RequestParam String tier) {
        return ResponseEntity.ok(ApiResponse.success("Subscription upgraded successfully", billingService.upgradeSubscription(tier)));
    }
}
