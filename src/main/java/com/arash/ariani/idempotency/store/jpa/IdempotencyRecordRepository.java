package com.arash.ariani.idempotency.store.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;


public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
    void deleteAllByTtlExpiryBefore(LocalDateTime time);
}
