package com.iconloop.score.example.model;

import com.iconloop.score.annotation_processor.JsonObject;
import com.iconloop.score.annotation_processor.JsonProperty;
import com.iconloop.score.annotation_processor.ScoreDataObject;
import com.iconloop.score.annotation_processor.ScoreDataProperty;

import java.util.List;

@JsonObject
@ScoreDataObject
public class DBAcceptable extends ParameterAcceptable {
    //not allowed types for parameter or return type of external method
    private float floatVal;
    private double doubleVal;
    private Float floatWrap;
    private Double doubleWrap;
    private List<String> stringList;

    //customize with @ScoreDataProperty
    @ScoreDataProperty(direct = true)
    @JsonProperty(direct = true)
    protected String directVal;

    @ScoreDataProperty(
            readObject = "CustomStruct.customReadObject",
            writeObject = "CustomStruct.customWriteObject")
    @JsonProperty(
            parser = "CustomStruct.customParse",
            toJson = "CustomStruct.customToJson"
    )
    private CustomStruct custom;

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

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public CustomStruct getCustom() {
        return custom;
    }

    public void setCustom(CustomStruct custom) {
        this.custom = custom;
    }
}
