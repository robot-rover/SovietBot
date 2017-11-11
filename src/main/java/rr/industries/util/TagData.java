package rr.industries.util;

import org.jooq.Record2;
import org.jooq.Record4;

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

    /**
     *
     * @param tagData must be in order GuildID, TagName, IsPermanent, TagContent
     */
    public TagData(Record4<String, String, Integer, String> tagData){
        this.guild = tagData.component1();
        this.name = tagData.component2();
        this.permanent = tagData.component3() == 1;
        this.content = tagData.component4();
    }

    /**
     *
     * @param tagData must be in order TagName, TagContent
     */
    public TagData(Record2<String, String> tagData){
        this.guild = null;
        this.name = tagData.component1();
        this.content = tagData.component2();
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
