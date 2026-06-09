package com.connectit.core.settings.service;

import com.connectit.core.settings.entity.Setting;
import com.connectit.core.settings.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SettingsService {

    @Autowired
    private SettingRepository settingRepository;

    @Transactional(readOnly = true)
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Setting> getSetting(String key) {
        return settingRepository.findByKey(key);
    }

    @Transactional
    public Setting updateSetting(String key, String value) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
        setting.setValue(value);
        return settingRepository.save(setting);
    }
}
