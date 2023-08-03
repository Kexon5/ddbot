package com.kexon5.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class WrapUtils {

    @FunctionalInterface
    private interface MyCallable {

        void call() throws Exception;
    }

    public static <T> T wrapExceptionableCall(Callable<T> callable) {
        return wrapExceptionableCall(callable, "Error:");
    }

    public static <T> T wrapExceptionableCall(Callable<T> callable, String errorMsg) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.error(errorMsg, e);
            return null;
        }
    }

    public static void wrapExceptionableCall(MyCallable callable) {
        wrapExceptionableCall(callable, "Request error:");
    }

    public static void wrapExceptionableCall(MyCallable callable, String errorMsg) {
        try {
            callable.call();
        } catch (Exception e) {
            log.error(errorMsg, e);
        }
    }
}
