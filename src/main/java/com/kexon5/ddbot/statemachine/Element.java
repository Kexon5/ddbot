package com.kexon5.ddbot.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.telegram.abilitybots.api.db.DBContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public abstract class Element<T extends Buttonable> implements Accessable {

    protected static final Map<Buttonable, Element<? extends Buttonable>> registry = new HashMap<>();
    @Setter
    protected static DBContext dbContext;

    protected final T elementState;

    private final Predicate<Long> accessPredicate;

    public Element(T state, @Nullable Predicate<Long> accessPredicate) {
        this.elementState = state;
        this.accessPredicate = accessPredicate;

        registry.put(state, this);
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
        return Optional.ofNullable(accessPredicate)
                       .orElse(Accessable.super.hasAccess());
    }
}
