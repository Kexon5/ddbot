package com.kexon5.bot.utils.markup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class MarkupList<T> extends ArrayList<T> {

    public MarkupList(Collection<T> data) {
        super(data);
    }

    public MarkupList() {
        super();
    }

    @Override
    public String toString() {
        return toCustomList(number -> new BoldString(number + ") "));
    }

    public String toSimpleString() {
        StringBuilder sb = new StringBuilder();
        this.forEach(el -> sb.append(el.toString()).append("\n"));
        return sb.toString();
    }

    public String toSimpleList() {
       return toCustomList(number -> new BoldString("- "));
    }

    public String toCustomList(Function<Integer, BoldString> function) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.size(); i++) {
            int number = i + 1;
            sb.append(function.apply(number))
                    .append(get(i).toString())
                    .append("\n");
        }
        return sb.toString();
    }
}
