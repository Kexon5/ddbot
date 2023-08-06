package com.kexon5.common.utils;

import javax.annotation.Nullable;

public class StringUtils {
    private StringUtils() {}

    @Nullable
    public static String escape(@Nullable String text) {
        return text != null
                ? text.replace("_", "\\_")
                      .replace("*", "\\*")
                      .replace("[", "\\[")
                      .replace("`", "\\`")
                : null;
    }
}
