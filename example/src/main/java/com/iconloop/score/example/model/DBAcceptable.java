package com.iconloop.score.example.model;

import com.iconloop.score.annotation_processor.JsonObject;
import com.iconloop.score.annotation_processor.JsonProperty;
import com.iconloop.score.annotation_processor.ScoreDataObject;
import com.iconloop.score.annotation_processor.ScoreDataProperty;

@JsonObject
@ScoreDataObject
public class DBAcceptable extends ImplicitParameterAcceptable {
    //not allowed types for parameter or return type of external method
    private float floatVal;
    private double doubleVal;
    private Float floatWrap;
    private Double doubleWrap;

    //this field will be ignored when using at parameter or return type, because it doesn't have getter and setter
    @ScoreDataProperty(direct = true)
    @JsonProperty(direct = true)
    protected String directVal;

    @ScoreDataProperty(
            readObject = "CustomConverter.readStruct",
            writeObject = "CustomConverter.writeObject")
    @JsonProperty(
            parser = "CustomConverter.parseStruct",
            toJson = "CustomConverter.toJson"
    )
    private Struct custom;

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

    public Struct getCustom() {
        return custom;
    }

    public void setCustom(Struct custom) {
        this.custom = custom;
    }
}
