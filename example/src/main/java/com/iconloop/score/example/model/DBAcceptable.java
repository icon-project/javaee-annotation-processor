package com.iconloop.score.example.model;

import com.iconloop.score.annotation_processor.JsonObject;
import com.iconloop.score.annotation_processor.JsonProperty;
import com.iconloop.score.annotation_processor.ScoreDataObject;
import com.iconloop.score.annotation_processor.ScoreDataProperty;
import com.iconloop.score.lib.Util;
import score.VarDB;

import java.util.List;

@JsonObject
@ScoreDataObject(beginOfOptionalFields = "custom")
public class DBAcceptable extends ImplicitParameterAcceptable {
    //not allowed types for parameter or return type of external method
    private float floatVal;
    private double doubleVal;
    private Float floatWrap;
    private Double doubleWrap;

    @ScoreDataProperty(nullable = false)
    private String[] stringNotNullArr;
    @ScoreDataProperty(nullable = false)
    private List<String> stringNotNullList;

    //this field will be ignored when using at parameter or return type, because it doesn't have getter and setter
    @ScoreDataProperty(direct = true)
    @JsonProperty(direct = true)
    protected String directVal;

    @ScoreDataProperty(wrapped = true)
    private Struct bytesWrappedStruct;

    @ScoreDataProperty(wrapped = true)
    private Struct[] bytesWrappedArr;

    @ScoreDataProperty(
            readObject = "CustomConverter.readStruct",
            writeObject = "CustomConverter.writeObject"
    )
    @JsonProperty(
            parser = "CustomConverter.parseStruct",
            toJson = "CustomConverter.toJson"
    )
    private Struct custom;

    @ScoreDataProperty(
            readObject = "CustomConverter.readStruct",
            writeObject = "CustomConverter.writeObject"
    )
    @JsonProperty(
            parser = "CustomConverter.parseStruct",
            toJson = "CustomConverter.toJson"
    )
    private Struct[] customArr;

    @ScoreDataProperty(ignore = true)
    private VarDB<String> varDB;

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

    public Float getFloatWrap() {
        return floatWrap;
    }

    public void setFloatWrap(Float floatWrap) {
        this.floatWrap = floatWrap;
    }

    public Double getDoubleWrap() {
        return doubleWrap;
    }

    public void setDoubleWrap(Double doubleWrap) {
        this.doubleWrap = doubleWrap;
    }

    public String[] getStringNotNullArr() {
        return stringNotNullArr;
    }

    public void setStringNotNullArr(String[] stringNotNullArr) {
        this.stringNotNullArr = stringNotNullArr;
    }

    public List<String> getStringNotNullList() {
        return stringNotNullList;
    }

    public void setStringNotNullList(List<String> stringNotNullList) {
        this.stringNotNullList = stringNotNullList;
    }

    public Struct getBytesWrappedStruct() {
        return bytesWrappedStruct;
    }

    public void setBytesWrappedStruct(Struct bytesWrappedStruct) {
        this.bytesWrappedStruct = bytesWrappedStruct;
    }

    public Struct[] getBytesWrappedArr() {
        return bytesWrappedArr;
    }

    public void setBytesWrappedArr(Struct[] bytesWrappedArr) {
        this.bytesWrappedArr = bytesWrappedArr;
    }

    public Struct getCustom() {
        return custom;
    }

    public void setCustom(Struct custom) {
        this.custom = custom;
    }

    public Struct[] getCustomArr() {
        return customArr;
    }

    public void setCustomArr(Struct[] customArr) {
        this.customArr = customArr;
    }

    public VarDB<String> getVarDB() {
        return varDB;
    }

    public void setVarDB(VarDB<String> varDB) {
        this.varDB = varDB;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBAcceptable{");
        sb.append("floatVal=").append(floatVal);
        sb.append(", doubleVal=").append(doubleVal);
        sb.append(", floatWrap=").append(floatWrap);
        sb.append(", doubleWrap=").append(doubleWrap);
        sb.append(", directVal='").append(directVal).append('\'');
        sb.append(", custom=").append(bytesWrappedStruct);
        sb.append(", customArr=").append(Util.toString(bytesWrappedArr));
        sb.append(", varDB=").append(varDB);
        sb.append('}');
        sb.append(super.toString());
        return sb.toString();
    }
}
