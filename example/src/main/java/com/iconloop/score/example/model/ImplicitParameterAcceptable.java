package com.iconloop.score.example.model;

import score.Address;

import java.math.BigInteger;
import java.util.List;

public class ImplicitParameterAcceptable extends ParameterAcceptable {

    public ImplicitParameterAcceptable() {
        super();
    }

    public ImplicitParameterAcceptable(ImplicitParameterAcceptable obj) {
        super(obj);
        this.setStringList(obj.getStringList());
    }

    //in case of parameter or return type of external method, not allowed List or Map type field by ABICompiler on OptimizeJar phase.
    //but if child class has List or Map type field and use in runtime, then it is allowed.
    private List<String> stringList;
//    private Map<String, String> stringToStringMap;


    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    @Override
    public boolean isBooleanVal() {
        return super.isBooleanVal();
    }

    @Override
    public void setBooleanVal(boolean booleanVal) {
        super.setBooleanVal(booleanVal);
    }

    @Override
    public byte getByteVal() {
        return super.getByteVal();
    }

    @Override
    public void setByteVal(byte byteVal) {
        super.setByteVal(byteVal);
    }

    @Override
    public char getCharVal() {
        return super.getCharVal();
    }

    @Override
    public void setCharVal(char charVal) {
        super.setCharVal(charVal);
    }

    @Override
    public short getShortVal() {
        return super.getShortVal();
    }

    @Override
    public void setShortVal(short shortVal) {
        super.setShortVal(shortVal);
    }

    @Override
    public int getIntVal() {
        return super.getIntVal();
    }

    @Override
    public void setIntVal(int intVal) {
        super.setIntVal(intVal);
    }

    @Override
    public long getLongVal() {
        return super.getLongVal();
    }

    @Override
    public void setLongVal(long longVal) {
        super.setLongVal(longVal);
    }

    @Override
    public byte[] getByteArr() {
        return super.getByteArr();
    }

    @Override
    public void setByteArr(byte[] byteArr) {
        super.setByteArr(byteArr);
    }

    @Override
    public int[] getIntArr() {
        return super.getIntArr();
    }

    @Override
    public void setIntArr(int[] intArr) {
        super.setIntArr(intArr);
    }

    @Override
    public Boolean getBooleanWrap() {
        return super.getBooleanWrap();
    }

    @Override
    public void setBooleanWrap(Boolean booleanWrap) {
        super.setBooleanWrap(booleanWrap);
    }

    @Override
    public Byte getByteWrap() {
        return super.getByteWrap();
    }

    @Override
    public void setByteWrap(Byte byteWrap) {
        super.setByteWrap(byteWrap);
    }

    @Override
    public Character getCharWrap() {
        return super.getCharWrap();
    }

    @Override
    public void setCharWrap(Character charWrap) {
        super.setCharWrap(charWrap);
    }

    @Override
    public Short getShortWrap() {
        return super.getShortWrap();
    }

    @Override
    public void setShortWrap(Short shortWrap) {
        super.setShortWrap(shortWrap);
    }

    @Override
    public Integer getIntegerWrap() {
        return super.getIntegerWrap();
    }

    @Override
    public void setIntegerWrap(Integer integerWrap) {
        super.setIntegerWrap(integerWrap);
    }

    @Override
    public Long getLongWrap() {
        return super.getLongWrap();
    }

    @Override
    public void setLongWrap(Long longWrap) {
        super.setLongWrap(longWrap);
    }

    @Override
    public String getStringVal() {
        return super.getStringVal();
    }

    @Override
    public void setStringVal(String stringVal) {
        super.setStringVal(stringVal);
    }

    @Override
    public BigInteger getBigIntegerVal() {
        return super.getBigIntegerVal();
    }

    @Override
    public void setBigIntegerVal(BigInteger bigIntegerVal) {
        super.setBigIntegerVal(bigIntegerVal);
    }

    @Override
    public Address getAddressVal() {
        return super.getAddressVal();
    }

    @Override
    public void setAddressVal(Address addressVal) {
        super.setAddressVal(addressVal);
    }

    @Override
    public String[] getStringArr() {
        return super.getStringArr();
    }

    @Override
    public void setStringArr(String[] stringArr) {
        super.setStringArr(stringArr);
    }

    @Override
    public Struct getStruct() {
        return super.getStruct();
    }

    @Override
    public void setStruct(Struct struct) {
        super.setStruct(struct);
    }

    @Override
    public byte[][] getByteArrArr() {
        return super.getByteArrArr();
    }

    @Override
    public void setByteArrArr(byte[][] byteArrArr) {
        super.setByteArrArr(byteArrArr);
    }

}