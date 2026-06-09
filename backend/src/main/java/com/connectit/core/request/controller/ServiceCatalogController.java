package com.connectit.core.request.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.request.entity.ServiceCatalogItem;
import com.connectit.core.request.entity.ServiceRequest;
import com.connectit.core.request.service.ServiceCatalogService;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class ServiceCatalogController {

    @Autowired
    private ServiceCatalogService serviceCatalogService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceCatalogItem>>> getActiveCatalog() {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getActiveCatalog()));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ServiceRequest>> raiseRequest(
            @RequestBody RequestSubmission submission,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User requester = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        ServiceRequest request = serviceCatalogService.raiseServiceRequest(
                submission.getItemId(),
                submission.getQuantity(),
                requester
        );
        return ResponseEntity.ok(ApiResponse.success("Service request raised successfully", request));
    }

    @Data
    public static class RequestSubmission {
        private Long itemId;
        private Integer quantity;
    }
}
