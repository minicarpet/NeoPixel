package com.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minicarpet on 02/03/2017.
 */

public class Functions {
    public List<Function> ITEMS = new ArrayList<Function>();
    public boolean isEdit = false;

    public void addItem(String id, String title, int color, int delay) {
        Function function = new Function(id, title, color, delay);
        ITEMS.add(function);
    }

    public void deleteItem(int index) {
        ITEMS.remove(index);
    }

    public class Function {
        public final String id;
        public final String title;
        public final int color;
        public final int delay;
        public boolean willBeDeleted;

        public Function(String id, String content, int Color, int Delay) {
            this.id = id;
            this.title = content;
            this.color = Color;
            this.delay = Delay;
            this.willBeDeleted = false;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
