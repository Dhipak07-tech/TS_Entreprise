package com.connectit.core.cmdb.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.asset.entity.Asset;
import com.connectit.core.cmdb.entity.CiRelationship;
import com.connectit.core.cmdb.entity.ConfigurationItem;
import com.connectit.core.cmdb.service.CmdbService;
import com.connectit.core.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cmdb")
public class CmdbController {

    private final CmdbService cmdbService;

    public CmdbController(CmdbService cmdbService) {
        this.cmdbService = cmdbService;
    }

    // --- Vendors ---
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        return ResponseEntity.ok(ApiResponse.success("Vendors retrieved successfully", cmdbService.getAllVendors()));
    }

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.ok(ApiResponse.success("Vendor created successfully", cmdbService.createVendor(vendor)));
    }

    // --- Assets ---
    @GetMapping("/assets")
    public ResponseEntity<ApiResponse<Page<Asset>>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Assets retrieved successfully", cmdbService.getAllAssets(pageable)));
    }

    @PostMapping("/assets")
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Asset>> createAsset(@RequestBody Asset asset) {
        return ResponseEntity.ok(ApiResponse.success("Asset created successfully", cmdbService.createAsset(asset)));
    }

    // --- Configuration Items ---
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ConfigurationItem>>> getAllConfigurationItems() {
        return ResponseEntity.ok(ApiResponse.success("Configuration items retrieved successfully", cmdbService.getAllConfigurationItems()));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<ConfigurationItem>> createConfigurationItem(@RequestBody ConfigurationItem ci) {
        return ResponseEntity.ok(ApiResponse.success("Configuration item created successfully", cmdbService.createConfigurationItem(ci)));
    }

    // --- Relationships ---
    @GetMapping("/relationships")
    public ResponseEntity<ApiResponse<List<CiRelationship>>> getAllRelationships() {
        return ResponseEntity.ok(ApiResponse.success("CI relationships retrieved successfully", cmdbService.getAllRelationships()));
    }

    @PostMapping("/relationships")
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<CiRelationship>> createRelationship(@RequestBody CiRelationship relationship) {
        try {
            return ResponseEntity.ok(ApiResponse.success("CI relationship created successfully", cmdbService.createRelationship(relationship)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
