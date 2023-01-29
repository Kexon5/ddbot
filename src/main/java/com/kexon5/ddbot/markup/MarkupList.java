package com.kexon5.ddbot.markup;

import java.util.ArrayList;
import java.util.Collection;

public class MarkupList<T> extends ArrayList<T> {

    public MarkupList(Collection<T> data) {
        super(data);
    }

    public MarkupList() {
        super();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.size(); i++) {
            int number = i + 1;
            sb.append(new BoldString(number + "\\) "))
                    .append(this.get(i).toString())
                    .append("\n");
        }
        return sb.toString();
    }
}
