package com.arash.ariani.idempotency.store.jpa;

import com.arash.ariani.idempotency.store.IdempotencyStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


public class JpaIdempotencyStore implements IdempotencyStore {

    private final ObjectMapper mapper;
    private final IdempotencyRecordRepository repository;

    public JpaIdempotencyStore(IdempotencyRecordRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public boolean exists(String key) {
        return repository.findById(key)
                .filter(record -> record.getTtlExpiry().isAfter(Instant.now()))
                .isPresent();
    }


    @Override
    public void save(String key, Object response, Duration ttl) {
        try {
            String json = mapper.writeValueAsString(response);
            IdempotencyRecord record = new IdempotencyRecord();
            record.setKey(key);
            record.setPayload(json);
            record.setTtlExpiry(Instant.now().plus(ttl));
            repository.save(record);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    @Override
    public Optional<Object> get(String key) {
        return repository.findById(key)
                .filter(record -> {
                    Instant now = Instant.now();
                    return record.getTtlExpiry().isAfter(now);
                })
                .map(record -> {
                    try {
                        return mapper.readValue(record.getPayload(), Object.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to deserialize cached payload", e);
                    }
                });
    }

}
