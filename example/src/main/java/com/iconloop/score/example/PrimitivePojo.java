package com.iconloop.score.example;

import foundation.icon.ee.annotation_processor.JsonObject;
import foundation.icon.ee.annotation_processor.JsonProperty;

@JsonObject
public class PrimitivePojo {
    @JsonProperty(getter = "isBooleanVal", setter = "booleanVal")
    private boolean booleanVal;
    private byte byteVal;
    private char charVal;
    private short shortVal;
    private int intVal;
    private long longVal;
    private float floatVal;
    private double doubleVal;
    private int[] intArrVal;

    public boolean isBooleanVal() {
        return booleanVal;
    }

    public void booleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public byte getByteVal() {
        return byteVal;
    }

    public void setByteVal(byte byteVal) {
        this.byteVal = byteVal;
    }

    public char getCharVal() {
        return charVal;
    }

    public void setCharVal(char charVal) {
        this.charVal = charVal;
    }

    public short getShortVal() {
        return shortVal;
    }

    public void setShortVal(short shortVal) {
        this.shortVal = shortVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public int[] getIntArrVal() {
        return intArrVal;
    }

    public void setIntArrVal(int[] intArrVal) {
        this.intArrVal = intArrVal;
    }
}
