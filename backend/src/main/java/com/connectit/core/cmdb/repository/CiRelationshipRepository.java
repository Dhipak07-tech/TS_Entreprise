package com.connectit.core.cmdb.repository;

import com.connectit.core.cmdb.entity.CiRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CiRelationshipRepository extends JpaRepository<CiRelationship, Long> {
    List<CiRelationship> findByParentCiId(Long parentCiId);
    List<CiRelationship> findByChildCiId(Long childCiId);
}
