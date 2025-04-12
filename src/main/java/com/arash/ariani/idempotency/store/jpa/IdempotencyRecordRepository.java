package com.arash.ariani.idempotency.store.jpa;

import org.springframework.data.jpa.repository.JpaRepository;


public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
}
