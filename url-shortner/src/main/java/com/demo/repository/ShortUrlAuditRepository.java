package com.demo.repository;

import com.demo.entity.ShortUrlAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlAuditRepository extends JpaRepository<ShortUrlAudit, Long> {
}