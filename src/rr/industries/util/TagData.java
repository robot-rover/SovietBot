package rr.industries.util;

import java.util.Optional;

/**
 * @author Sam
 */
public class TagData {
    private String guild;
    private String name;
    private String content;
    private boolean permanent;

    public TagData(String guild, String name, String content, boolean permanent) {
        this.guild = guild;
        this.name = name;
        this.content = content;
        this.permanent = permanent;
    }

    public TagData(String name, String content) {
        this.guild = null;
        this.name = name;
        this.content = content;
        this.permanent = false;
    }

    public Optional<String> getGuild() {
        return Optional.ofNullable(guild);
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isGlobal() {
        return guild == null;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }
}
