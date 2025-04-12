CREATE TABLE IF NOT EXISTS IDEMPOTENCY_ENTRIES (
                                                  key VARCHAR(255) PRIMARY KEY,
                                                  payload TEXT NOT NULL,
                                                  ttl_expiry TIMESTAMP,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);