package com.example.simple.spring.web.mvc.bind.support;

public class SimpleSessionStatus implements SessionStatus {

    private boolean complete = false;

    @Override
    public void setComplete() {
        this.complete = true;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

}
