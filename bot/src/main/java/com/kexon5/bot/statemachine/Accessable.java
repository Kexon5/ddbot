package com.kexon5.bot.statemachine;

import java.util.function.Predicate;

public interface Accessable extends Buttonable {

    DialogueFlow.DialogueFlowBuilder getReplyFlowBuilder();

    default Predicate<Long> hasAccess() {
        return userId -> true;
    }
}
