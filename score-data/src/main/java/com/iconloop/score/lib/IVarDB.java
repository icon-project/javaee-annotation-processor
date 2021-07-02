package com.iconloop.score.lib;

/**
 * Copy of {@link score.VarDB}, it's not allowed for jar-optimization
 * @param <E> Variable type. It shall be readable and writable class.
 */
public interface IVarDB<E> {
    /**
     * Sets value.
     * @param value new value
     */
    void set(E value);

    /**
     * Returns the current value.
     * @return current value
     */
    E get();

    /**
     * Returns the current value or {@code defaultValue} if the current value
     * is {@code null}.
     * @param defaultValue default value
     * @return the current value or {@code defaultValue} if the current value
     * is {@code null}.
     */
    E getOrDefault(E defaultValue);
}
