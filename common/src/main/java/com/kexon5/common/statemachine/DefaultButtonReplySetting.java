package com.kexon5.common.statemachine;

import lombok.RequiredArgsConstructor;

public class DefaultButtonReplySetting implements ButtonReplySetting {

    @RequiredArgsConstructor
    enum State {
        FALSE("❌"),
        TRUE("✅");


        private final String stateString;
    }

    private State state;

    public DefaultButtonReplySetting(boolean state) {
        this.state = state ? State.TRUE : State.FALSE;
    }

    @Override
    public void next() {
        state = state == State.TRUE
                ? State.FALSE
                : State.TRUE;
    }

    @Override
    public String getSettingState() {
        return state.stateString;
    }
}
