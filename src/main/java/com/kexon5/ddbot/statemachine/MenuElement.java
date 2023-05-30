package com.kexon5.ddbot.statemachine;

import lombok.Getter;
import lombok.Setter;
import org.telegram.abilitybots.api.db.DBContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public abstract class MenuElement implements Accessable {

    protected static final Map<Buttonable, MenuElement> registry = new HashMap<>();

    @Setter
    protected static DBContext dbContext;

    private final String buttonText;
    private final Predicate<Long> accessPredicate;

    public MenuElement(Buttonable button, @Nullable Predicate<Long> accessPredicate) {
        this.buttonText = button.getButtonText();
        this.accessPredicate = accessPredicate;

        registry.put(button, this);
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
                ? accessPredicate
                : Accessable.super.hasAccess();
    }
}
