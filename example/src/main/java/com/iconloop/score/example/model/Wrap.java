package com.iconloop.score.example.model;

import com.iconloop.score.data.ScoreDataObject;
import com.iconloop.score.data.ScoreDataProperty;

@ScoreDataObject
public class Wrap {
    @ScoreDataProperty(wrapped = true)
    private Simple simple;

    @ScoreDataProperty(wrapped = true, nullable = false)
    private Simple notNullSimple;

    @ScoreDataProperty(wrapped = true)
    private Simple[] simpleArr;

    @ScoreDataProperty(wrapped = true, nullableComponent = false)
    private Simple[] notNullSimpleArr;

    public Simple getSimple() {
        return simple;
    }

    public void setSimple(Simple simple) {
        this.simple = simple;
    }

    public Simple getNotNullSimple() {
        return notNullSimple;
    }

    public void setNotNullSimple(Simple notNullSimple) {
        this.notNullSimple = notNullSimple;
    }

    public Simple[] getSimpleArr() {
        return simpleArr;
    }

    public void setSimpleArr(Simple[] simpleArr) {
        this.simpleArr = simpleArr;
    }

    public Simple[] getNotNullSimpleArr() {
        return notNullSimpleArr;
    }

    public void setNotNullSimpleArr(Simple[] notNullSimpleArr) {
        this.notNullSimpleArr = notNullSimpleArr;
    }

}
