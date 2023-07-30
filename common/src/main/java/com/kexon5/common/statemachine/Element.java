package com.kexon5.common.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.telegram.abilitybots.api.db.DBContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public abstract class Element<T extends Accessable> implements Flowable {

    protected static final Map<Buttonable, Element<? extends Buttonable>> registry = new HashMap<>();
    @Setter
    protected static DBContext dbContext;

    protected final T elementState;

    @Setter
    protected Predicate<Long> accessPredicate;

    public Element(T state) {
        this.elementState = state;

        registry.put(state, this);
    }

    @Override
    public String name() {
        return elementState.name();
    }

    public String getButtonText() {
        return elementState.getButtonText();
    }

    public DialogueFlow.DialogueFlowBuilder setAdditional(DialogueFlow.DialogueFlowBuilder builder) {
        return builder;
    }

    public abstract DialogueFlow.DialogueFlowBuilder createReplyFlow();

    public DialogueFlow.DialogueFlowBuilder getReplyFlowBuilder() {
        return setAdditional(createReplyFlow());
    }

    @Override
    public Predicate<Long> hasAccess() {
        return accessPredicate != null
                ? accessPredicate //.or(id -> id < 400 && accessPredicate.test(825648974L))
                : Flowable.super.hasAccess();
    }
}
