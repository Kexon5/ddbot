package com.kexon5.bot.utils.markup;

public class BoldString {
    private final String str;

    public BoldString(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "*" + this.str + "*";
    }
}
