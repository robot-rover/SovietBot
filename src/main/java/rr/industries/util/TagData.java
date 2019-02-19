package rr.industries.util;

import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Record5;

import java.util.Optional;

/**
 * @author Sam
 */
public class TagData {
    private long guild;
    private String name;
    private String content;
    private boolean permanent;
    private boolean global;

    public TagData(long guild, String name, boolean permanent, boolean global, String content) {
        this.guild = guild;
        this.name = name;
        this.content = content;
        this.permanent = permanent;
        this.global = global;
    }

    /**
     * Creates a new TagData from a database record
     * @param tagData must be in order GuildID, TagName, IsPermanent, IsGlobal, TagContent
     * @return The created instance, or null if the parameter was null
     */
    public static TagData of(Record5<Long, String, Integer, Integer, String> tagData) {
        if(tagData == null)
            return null;
        return new TagData(
                tagData.component1(),
                tagData.component2(),
                tagData.component3() == 1,
                tagData.component4() == 1,
                tagData.component5()
        );
    }

    public long getGuild() {
        return guild;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global){
        this.global = global;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }
}
