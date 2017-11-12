package com.github.adamint.Models;

public class Scope {
    private String name;
    private String route;

    public static enum Routes {
        CONNECTIONS("/users/@me/connections"),
        EMAIL("/users/@me"),
        IDENTIFY("/users/@me"),
        GUILDS("/users/@me/guilds"),
        BOT_INFORMATION("/oauth2/applications/@me");

        private String route;

        Routes(String s) {
            this.route = s;
        }

        public String getRouteURL() {
            return route;
        }

    }
}
