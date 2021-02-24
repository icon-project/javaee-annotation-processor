package com.iconloop.score.example;

import foundation.icon.ee.annotation_processor.JsonObject;
import foundation.icon.ee.annotation_processor.JsonProperty;

import java.util.List;

@JsonObject
public class Pojo {
    private Boolean booleanVal;
    private Byte byteVal;
    private Character charVal;
    private Short shortVal;
    private Integer integerVal;
    private Long longVal;
    private Float floatVal;
    private Double doubleVal;
    private String strVal;
    private String[] strArrVal;
    private List<String> strListVal;

    private PrimitivePojo primitivePojo;
    private PrimitivePojo[] primitiveArrPojo;
    private List<PrimitivePojo> primitiveListPojo;

    @JsonProperty(value = "DirectVal", direct = true)
    protected String directVal;

    public Boolean getBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(Boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public Byte getByteVal() {
        return byteVal;
    }

    public void setByteVal(Byte byteVal) {
        this.byteVal = byteVal;
    }

    public Character getCharVal() {
        return charVal;
    }

    public void setCharVal(Character charVal) {
        this.charVal = charVal;
    }

    public Short getShortVal() {
        return shortVal;
    }

    public void setShortVal(Short shortVal) {
        this.shortVal = shortVal;
    }

    public Integer getIntegerVal() {
        return integerVal;
    }

    public void setIntegerVal(Integer integerVal) {
        this.integerVal = integerVal;
    }

    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    public Float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(Float floatVal) {
        this.floatVal = floatVal;
    }

    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    public String[] getStrArrVal() {
        return strArrVal;
    }

    public void setStrArrVal(String[] strArrVal) {
        this.strArrVal = strArrVal;
    }

    public List<String> getStrListVal() {
        return strListVal;
    }

    public void setStrListVal(List<String> strListVal) {
        this.strListVal = strListVal;
    }

    public PrimitivePojo getPrimitivePojo() {
        return primitivePojo;
    }

    public void setPrimitivePojo(PrimitivePojo primitivePojo) {
        this.primitivePojo = primitivePojo;
    }

    public PrimitivePojo[] getPrimitiveArrPojo() {
        return primitiveArrPojo;
    }

    public void setPrimitiveArrPojo(PrimitivePojo[] primitiveArrPojo) {
        this.primitiveArrPojo = primitiveArrPojo;
    }

    public List<PrimitivePojo> getPrimitiveListPojo() {
        return primitiveListPojo;
    }

    public void setPrimitiveListPojo(List<PrimitivePojo> primitiveListPojo) {
        this.primitiveListPojo = primitiveListPojo;
    }
}
