package com.khang.batterysaver.model;

/** Một mẹo tiết kiệm pin. */
public class Tip {
    public final String id;
    public final String title;
    public final String description;
    public final int iconRes;
    /** Action của Intent để mở màn hình cài đặt tương ứng. */
    public final String action;
    public boolean done;

    public Tip(String id, String title, String description, int iconRes, String action) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.action = action;
    }
}
