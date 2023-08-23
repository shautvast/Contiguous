package com.github.shautvast.contiguous;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boundless cache something
 */
class BufferCache {
    private static final ConcurrentHashMap<Integer, ByteBuffer> cache = new ConcurrentHashMap<>();

    static ByteBuffer get(int size) {
        ByteBuffer byteBuffer = Optional.ofNullable(cache.get(size)).orElseGet(() -> ByteBuffer.allocate(size));
        byteBuffer.position(0);
        return byteBuffer;
    }

    static void release(ByteBuffer byteBuffer) {
        cache.put(byteBuffer.capacity(), byteBuffer);
    }
}
