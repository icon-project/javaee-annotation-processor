package com.iconloop.score.example.model;

import com.iconloop.score.json.JsonObject;
import com.iconloop.score.data.ScoreDataObject;
import com.iconloop.score.data.ScorePropertiesDBObject;

@JsonObject
@ScorePropertiesDBObject
@ScoreDataObject
public class Simple {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
