package com.kexon5.ddbot.statemachine;

import java.util.function.Predicate;

public interface Accessable extends Buttonable {

    DialogueFlow.DialogueFlowBuilder getReplyFlowBuilder();

    default Predicate<Long> hasAccess() {
        return userId -> true;
    }
}
