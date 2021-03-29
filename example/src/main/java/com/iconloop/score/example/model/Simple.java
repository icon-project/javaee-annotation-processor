package com.iconloop.score.example.model;

import com.iconloop.score.annotation_processor.JsonObject;
import com.iconloop.score.annotation_processor.ScorePropertiesDBObject;

@JsonObject
@ScorePropertiesDBObject
public class Simple {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
