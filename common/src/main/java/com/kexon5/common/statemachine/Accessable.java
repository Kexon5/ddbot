package com.kexon5.common.statemachine;

import com.kexon5.common.models.User;

import java.util.function.Predicate;

public interface Accessable extends Buttonable {

    String name();

    default Predicate<Long> hasAccess() {
        return userId -> true;
    }

    default boolean hasAccess(User user) {
        return true;
    }
}
