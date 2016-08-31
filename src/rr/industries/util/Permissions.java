package rr.industries.util;

/**
 * Created by Sam on 8/28/2016.
 */
public enum Permissions {
    NORMAL(0, "Normal"), REGULAR(1, "Regular"), MOD(2, "Moderator"), ADMIN(3, "Administrator"), BOTOPERATOR(4, "Bot Operator");
    public final int level;
    public final String title;

    Permissions(int level, String title) {
        this.level = level;
        this.title = title;
    }
}
