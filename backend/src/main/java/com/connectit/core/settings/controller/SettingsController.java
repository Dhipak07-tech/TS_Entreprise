package com.connectit.core.settings.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.settings.entity.Setting;
import com.connectit.core.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Setting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingsService.getAllSettings()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            settingsService.updateSetting(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", null));
    }
}
