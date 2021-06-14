/*
 * Copyright 2020 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.example;

import com.iconloop.score.example.model.*;
import com.iconloop.score.lib.LinkedIterableDictDB;
import com.iconloop.score.lib.PropertiesDB;
import score.Address;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.Map;

public class HelloWorldImpl implements HelloWorld {
    private final String name;
    private String greeting = "Hello";
    private String to;
    private final EnumerableDictDB<String, DBAcceptableSdo> enumerableDictDB;
    private final LinkedIterableDictDB<String, ComplexSpo> linkedIterableDictDB;

    /**
     * enum is not allowed as parameter
     */
    public enum DB {Enum, Linked }

    public HelloWorldImpl(String _name) {
        this.name = _name;
        enumerableDictDB = new EnumerableDictDB<>("enumerableDictDB", String.class, DBAcceptableSdo.class);
        linkedIterableDictDB = new LinkedIterableDictDB<>("linkedIterableDictDB", ComplexSpo.class, String.class);
    }

    @External(readonly = true)
    public String name() {
        return name;
    }

    @External(readonly = true)
    public String greeting() {
        String msg = "[" + name + "] " + greeting + " " + to + "!";
        Context.println(msg);
        return msg;
    }

    @External
    public void setGreeting(String _greeting) {
        this.greeting = _greeting;
    }

    @Payable
    @External
    public void setTo(String _to) {
        this.to = _to;
    }

    @Payable
    @External
    public void intercall(Address _address) {
        HelloWorldScoreInterface helloWorldScoreInterface = new HelloWorldScoreInterface(_address);
        String before = helloWorldScoreInterface.greeting();
        helloWorldScoreInterface.setTo(Context.getValue(), name);
        Intercall(_address, Context.getValue(), before, helloWorldScoreInterface.greeting());
    }

    @EventLog(indexed = 1)
    public void Intercall(Address _address, BigInteger icx, String before, String after) {
    }

    @External
    public void put(String _db, String _key, String _value) {
        Context.println("[put]"+ "_db:" + _db + "_key:" + _key + ",_value:" + _value);
        switch (DB.valueOf(_db)) {
            case Enum:
                DBAcceptable acceptable = DBAcceptableJson.parse(_value);
                DBAcceptableSdo sdo = null;
                if (acceptable != null) {
                    sdo = new DBAcceptableSdo(acceptable);
                }
                enumerableDictDB.put(_key, sdo);
                break;
            case Linked:
                Complex complex = ComplexJson.parse(_value);
                ComplexSpo spo = null;
                if (complex != null) {
                    spo = new ComplexSpo();
                    spo.initialize(linkedIterableDictDB.concatID(_key));
                    spo.value(complex);
                }
                linkedIterableDictDB.set(_key, spo);
                linkedIterableDictDB.flushAndClose();
                break;
            default:
                throw new IllegalArgumentException("invalid _db");
        }
    }

    @External
    public void remove(String _db, String _key) {
        Context.println("[remove]"+ "_db:" + _db + "_key:" + _key);
        switch (DB.valueOf(_db)) {
            case Enum:
                enumerableDictDB.remove(_key);
                break;
            case Linked:
                linkedIterableDictDB.remove(_key);
                linkedIterableDictDB.flushAndClose();
            default:
                throw new IllegalArgumentException("invalid _db");
        }
    }

    private <T extends ImplicitParameterAcceptable> ImplicitParameterAcceptable convert(T obj) {
        if (obj instanceof PropertiesDB) {
            @SuppressWarnings("unchecked")
            ImplicitParameterAcceptable value = ((PropertiesDB<? extends ImplicitParameterAcceptable>) obj).value();
            return new ImplicitParameterAcceptable(value);
        } else {
            return new ImplicitParameterAcceptable(obj);
        }
    }

    private Map<String, ImplicitParameterAcceptable> convert(Map<String, ? extends ImplicitParameterAcceptable> map) {
        Map<String, ImplicitParameterAcceptable> retMap = new scorex.util.HashMap<>();
        for(Map.Entry<String, ? extends ImplicitParameterAcceptable> entry : map.entrySet()) {
            retMap.put(entry.getKey(), convert(entry.getValue()));
        }
        return retMap;
    }

    private void close(DB db) {
        switch (db) {
            case Linked:
                linkedIterableDictDB.close();
                break;
            case Enum:
                break;
            default:
                throw new IllegalArgumentException("invalid _db");
        }
    }

    private ImplicitParameterAcceptable get(DB db, String key) {
        ImplicitParameterAcceptable ret;
        switch (db) {
            case Enum:
                ret = enumerableDictDB.get(key);
                break;
            case Linked:
                ret = linkedIterableDictDB.get(key);
                break;
            default:
                throw new IllegalArgumentException("invalid _db");
        }
        return convert(ret);
    }

    private Map<String, ImplicitParameterAcceptable> map(DB db) {
        Map<String, ? extends ImplicitParameterAcceptable> map;
        switch (db) {
            case Enum:
                map = enumerableDictDB.toMap();
                break;
            case Linked:
                map = linkedIterableDictDB.toMap();
                break;
            default:
                throw new IllegalArgumentException("invalid _db");
        }
        return convert(map);
    }

    @External(readonly = true)
    public ParameterAcceptable get(String _db, String _key) {
        Context.println("[get]"+"_db:" + _db + "_key:" + _key);
        DB db = DB.valueOf(_db);
        ImplicitParameterAcceptable ret = get(db, _key);
        close(db);
        return ret;
    }

    @External(readonly = true)
    public Map map(String _db) {
        Context.println("[map]"+"_db:" + _db);
        DB db = DB.valueOf(_db);
        Map<String, ImplicitParameterAcceptable> map = map(db);
        close(db);
        return map;
    }

    @Payable
    public void fallback() {
        // just receive incoming funds
    }

}
