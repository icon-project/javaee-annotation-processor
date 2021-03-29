package com.iconloop.score.example.model;

import score.Address;

import java.math.BigInteger;

public class ParameterAcceptable {
    //primitive
    private boolean booleanVal;
    private byte byteVal;
    private char charVal;
    private short shortVal;
    private int intVal;
    private long longVal;
    //array of primitive
    private byte[] byteArr;
    private int[] intArr;

    //wrap of primitive
    private Boolean booleanWrap;
    private Byte byteWrap;
    private Character charWrap;
    private Short shortWrap;
    private Integer integerWrap;
    private Long longWrap;

    //nullable
    private String stringVal;
    private BigInteger bigIntegerVal;
    private Address addressVal;
    //array of nullable
    private String[] stringArr;

    //struct
    private Struct struct;

    public ParameterAcceptable() {
        super();
    }

    public ParameterAcceptable(ParameterAcceptable obj) {
        super();
        this.setBooleanVal(obj.isBooleanVal());
        this.setByteVal(obj.getByteVal());
        this.setCharVal(obj.getCharVal());
        this.setShortVal(obj.getShortVal());
        this.setIntVal(obj.getIntVal());
        this.setLongVal(obj.getLongVal());
        this.setByteArr(obj.getByteArr());
        this.setIntArr(obj.getIntArr());
        this.setBooleanWrap(obj.getBooleanWrap());
        this.setByteWrap(obj.getByteWrap());
        this.setCharWrap(obj.getCharWrap());
        this.setShortWrap(obj.getShortWrap());
        this.setIntegerWrap(obj.getIntegerWrap());
        this.setLongWrap(obj.getLongWrap());
        this.setStringVal(obj.getStringVal());
        this.setBigIntegerVal(obj.getBigIntegerVal());
        this.setAddressVal(obj.getAddressVal());
        this.setStringArr(obj.getStringArr());
        this.setStruct(obj.getStruct());
    }

    public boolean isBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(boolean booleanVal) {
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

    public byte[] getByteArr() {
        return byteArr;
    }

    public void setByteArr(byte[] byteArr) {
        this.byteArr = byteArr;
    }

    public int[] getIntArr() {
        return intArr;
    }

    public void setIntArr(int[] intArr) {
        this.intArr = intArr;
    }

    public Boolean getBooleanWrap() {
        return booleanWrap;
    }

    public void setBooleanWrap(Boolean booleanWrap) {
        this.booleanWrap = booleanWrap;
    }

    public Byte getByteWrap() {
        return byteWrap;
    }

    public void setByteWrap(Byte byteWrap) {
        this.byteWrap = byteWrap;
    }

    public Character getCharWrap() {
        return charWrap;
    }

    public void setCharWrap(Character charWrap) {
        this.charWrap = charWrap;
    }

    public Short getShortWrap() {
        return shortWrap;
    }

    public void setShortWrap(Short shortWrap) {
        this.shortWrap = shortWrap;
    }

    public Integer getIntegerWrap() {
        return integerWrap;
    }

    public void setIntegerWrap(Integer integerWrap) {
        this.integerWrap = integerWrap;
    }

    public Long getLongWrap() {
        return longWrap;
    }

    public void setLongWrap(Long longWrap) {
        this.longWrap = longWrap;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public BigInteger getBigIntegerVal() {
        return bigIntegerVal;
    }

    public void setBigIntegerVal(BigInteger bigIntegerVal) {
        this.bigIntegerVal = bigIntegerVal;
    }

    public Address getAddressVal() {
        return addressVal;
    }

    public void setAddressVal(Address addressVal) {
        this.addressVal = addressVal;
    }

    public String[] getStringArr() {
        return stringArr;
    }

    public void setStringArr(String[] stringArr) {
        this.stringArr = stringArr;
    }

    public Struct getStruct() {
        return struct;
    }

    public void setStruct(Struct struct) {
        this.struct = struct;
    }
}
