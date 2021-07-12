package com.iconloop.score.example.model;

import com.iconloop.score.data.ScoreDataObject;

@ScoreDataObject(beginOfOptionalFields = "added")
public class BackwardCompatible extends ParameterAcceptable {
    private String added;

    public BackwardCompatible() {
    }

    public BackwardCompatible(ParameterAcceptable obj) {
        super(obj);
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BackwardCompatible{");
        sb.append("added='").append(added).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
