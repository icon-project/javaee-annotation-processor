package com.iconloop.score.lib;

import score.Context;
import score.VarDB;

public class ProxyVarDB<E> implements ProxyDB {
    protected final String id;
    protected final Class<E> valueClass;
    protected final VarDB<E> varDB;
    private E origin = null;
    private E update = null;
    private Boolean modified = null;

    public ProxyVarDB(String id, Class<E> valueClass) {
        this.id = id;
        this.valueClass = valueClass;
        varDB = Context.newVarDB(id, valueClass);
    }

    public void set(E value) {
        update = value;
        modified = true;
    }

    public E get() {
        if (isModified()) {
            return update;
        } else {
            if (modified == null) {
                origin = varDB.get();
                modified = false;
            }
            return origin;
        }
    }

    public E getOrDefault(E defaultValue) {
        E value = get();
        return value == null ? defaultValue : value;
    }

    public boolean isModified() {
        return (modified != null && modified);
    }

    public boolean isLoaded() {
        return (modified != null && !modified);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void close() {
        origin = null;
        update = null;
        modified = null;
        println("close");
    }

    @Override
    public void flush() {
        varDB.set(update);
        println("flush value:", Util.toString(update));
    }
}
