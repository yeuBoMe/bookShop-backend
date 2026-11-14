package com.fullstack.bookShop.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cache co han su dung, dung cho cac so lieu doc nhieu ghi it (man hinh thong ke).
 *
 * <p>Cach lam theo dashboard cua FaceX nhung doi cong nghe: FaceX dung Redis vi la he
 * nhieu service, BookShop chi mot tien trinh nen cache ngay trong bo nho la du va khong
 * them mot thanh phan ha tang phai van hanh.
 *
 * <p>Hai tinh chat giu nguyen tu FaceX:
 * <ul>
 *   <li>Co co bat/tat de tat han khi can doc so lieu tuoi.</li>
 *   <li>Cache hong KHONG bao gio lam hong request: moi su co deu quy ve "truot cache" roi tinh lai.</li>
 * </ul>
 */
@Component
public class TtlCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TtlCache.class);

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    @Value("${bookshop.cache.enabled}")
    private boolean enabled;

    @Value("${bookshop.cache.max-entries}")
    private int maxEntries;

    /**
     * Tra gia tri con han, neu khong thi goi {@code loader} roi nho lai.
     * Khong dung {@code computeIfAbsent} vi loader co truy van DB: giu lock cua bucket
     * trong suot mot cau query se chan cac key khac cua cung bucket.
     */
    public <T> T getOrLoad(String key, long ttlSeconds, Supplier<T> loader) {
        if (!enabled || ttlSeconds <= 0) {
            return loader.get();
        }

        CacheEntry entry = store.get(key);
        if (entry != null && !entry.isExpired()) {
            return cast(entry);
        }

        T value = loader.get();
        put(key, value, ttlSeconds);
        return value;
    }

    public void put(String key, Object value, long ttlSeconds) {
        if (!enabled || value == null) {
            return;
        }

        // Chan cache phinh vo han khi key sinh tu tham so nguoi dung tu do nhap
        if (store.size() >= maxEntries) {
            evictExpired();
        }
        if (store.size() >= maxEntries) {
            LOGGER.warn("TtlCache day ({} muc), bo qua key moi: {}", store.size(), key);
            return;
        }

        store.put(key, new CacheEntry(value, Instant.now().plusSeconds(ttlSeconds)));
    }

    public void evictByPrefix(String keyPrefix) {
        store.keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    public void clear() {
        store.clear();
    }

    private void evictExpired() {
        store.values().removeIf(CacheEntry::isExpired);
    }

    /**
     * Khong the kiem tra kieu o day vi generic bi xoa luc bien dich - cast nay chi no o cho goi.
     * An toan den tu quy uoc dat key: moi widget dung mot tien to hang so rieng
     * (xem CacheKeyConstant) nen hai kieu khac nhau khong the dung chung mot key.
     */
    @SuppressWarnings("unchecked")
    private <T> T cast(CacheEntry entry) {
        return (T) entry.value();
    }

    private record CacheEntry(Object value, Instant expiresAt) {

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
