package rr.industries.util;

public enum Permissions {
    NORMAL(0, "Normal"), REGULAR(1, "Regular"), MOD(2, "Moderator"), ADMIN(3, "Administrator"), BOTOPERATOR(4, "Bot Operator");
    public final int level;
    public final String title;

    Permissions(int level, String title) {
        this.level = level;
        this.title = title;
    }
}
