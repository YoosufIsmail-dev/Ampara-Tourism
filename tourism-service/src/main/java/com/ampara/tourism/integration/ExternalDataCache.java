package com.ampara.tourism.integration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bounded per-instance cache. Keeps external integrations fast without allowing
 * an unbounded ConcurrentHashMap to grow forever under many unique requests.
 * For multi-instance deployments, an external Redis cache can be introduced
 * later without changing the service contract.
 */
@Component
public class ExternalDataCache {
    private final Cache<String, Entry> cache;

    public ExternalDataCache(
            @Value("${app.cache.max-entries:2000}") long maxEntries,
            @Value("${app.cache.default-ttl-seconds:600}") long defaultTtlSeconds) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(100, maxEntries))
                .expireAfterWrite(Math.max(30, defaultTtlSeconds), TimeUnit.SECONDS)
                .recordStats()
                .build();
    }

    public Object get(String key) {
        Entry entry = cache.getIfPresent(key);
        if (entry == null || entry.expiresAtNanos < System.nanoTime()) {
            cache.invalidate(key);
            return null;
        }
        return entry.value;
    }

    public void put(String key, Object value, Duration ttl) {
        long ttlNanos = Math.max(1, ttl.toNanos());
        cache.put(key, new Entry(value, System.nanoTime() + ttlNanos));
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    private static final class Entry {
        private final Object value;
        private final long expiresAtNanos;

        private Entry(Object value, long expiresAtNanos) {
            this.value = value;
            this.expiresAtNanos = expiresAtNanos;
        }
    }
}
