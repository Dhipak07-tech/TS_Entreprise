package com.connectit.core.cmdb.service;

import com.connectit.core.asset.entity.Asset;
import com.connectit.core.asset.repository.AssetRepository;
import com.connectit.core.cmdb.entity.CiRelationship;
import com.connectit.core.cmdb.entity.ConfigurationItem;
import com.connectit.core.cmdb.repository.CiRelationshipRepository;
import com.connectit.core.cmdb.repository.ConfigurationItemRepository;
import com.connectit.core.vendor.entity.Vendor;
import com.connectit.core.vendor.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CmdbService {

    private final VendorRepository vendorRepository;
    private final AssetRepository assetRepository;
    private final ConfigurationItemRepository configurationItemRepository;
    private final CiRelationshipRepository ciRelationshipRepository;

    public CmdbService(VendorRepository vendorRepository,
                       AssetRepository assetRepository,
                       ConfigurationItemRepository configurationItemRepository,
                       CiRelationshipRepository ciRelationshipRepository) {
        this.vendorRepository = vendorRepository;
        this.assetRepository = assetRepository;
        this.configurationItemRepository = configurationItemRepository;
        this.ciRelationshipRepository = ciRelationshipRepository;
    }

    // --- Vendors ---
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendor createVendor(Vendor vendor) {
        if (vendor.getStatus() == null) {
            vendor.setStatus("ACTIVE");
        }
        return vendorRepository.save(vendor);
    }

    // --- Assets ---
    public Page<Asset> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    public Asset createAsset(Asset asset) {
        if (assetRepository.existsByAssetTag(asset.getAssetTag())) {
            throw new IllegalArgumentException("Asset tag already exists: " + asset.getAssetTag());
        }
        return assetRepository.save(asset);
    }

    // --- Configuration Items ---
    public List<ConfigurationItem> getAllConfigurationItems() {
        return configurationItemRepository.findAll();
    }

    public ConfigurationItem createConfigurationItem(ConfigurationItem ci) {
        return configurationItemRepository.save(ci);
    }

    // --- Relationships ---
    public List<CiRelationship> getAllRelationships() {
        return ciRelationshipRepository.findAll();
    }

    public CiRelationship createRelationship(CiRelationship relationship) {
        Long parentId = relationship.getParentCi().getId();
        Long childId = relationship.getChildCi().getId();

        if (parentId.equals(childId)) {
            throw new IllegalArgumentException("A CI cannot have a relationship with itself.");
        }

        // Circular Dependency Check: Does a path exist from childId to parentId?
        if (pathExists(childId, parentId)) {
            throw new IllegalArgumentException("Circular dependency detected! Cannot add relationship: child CI depends on parent CI.");
        }

        // Fetch managed entities
        ConfigurationItem parent = configurationItemRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent CI not found: " + parentId));
        ConfigurationItem child = configurationItemRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child CI not found: " + childId));

        relationship.setParentCi(parent);
        relationship.setChildCi(child);

        return ciRelationshipRepository.save(relationship);
    }

    private boolean pathExists(Long startId, Long targetId) {
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();

        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(targetId)) {
                return true;
            }

            // Find all children of the current CI
            List<CiRelationship> relations = ciRelationshipRepository.findByParentCiId(current);
            for (CiRelationship relation : relations) {
                Long childId = relation.getChildCi().getId();
                if (!visited.contains(childId)) {
                    visited.add(childId);
                    queue.add(childId);
                }
            }
        }

        return false;
    }
}
