package com.iconloop.score.lib;

/**
 * Copy of {@link score.DictDB}, it's not allowed for jar-optimization
 * @param <K> Key type. It shall be String, byte array, Address,
 *           Byte, Short, Integer, Long, Character or BigInteger.
 * @param <V> Value type. It shall be readable and writable class.
 */
public interface IDictDB<K, V> {
    /**
     * Sets a value for a key
     * @param key key
     * @param value value for the key
     */
    void set(K key, V value);

    /**
     * Returns the value for a key
     * @param key key
     * @return the value for a key
     */
    V get(K key);

    /**
     * Returns the value for a key or {@code defaultValue} if the value is
     * {@code null}.
     * @param key key
     * @param defaultValue default value
     * @return the value for a key or {@code defaultValue} if the value is
     * {@code null}.
     */
    V getOrDefault(K key, V defaultValue);
}
