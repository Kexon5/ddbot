package com.kexon5.ddbot.services.hospitals.actions;

public interface ActionSteps {
    String getMsg();

    default String handle(String text) {
        return null;
    }
}
