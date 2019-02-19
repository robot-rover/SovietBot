package rr.industries.util.sql;

import discord4j.core.object.util.Snowflake;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.MissingPermsException;
import rr.industries.util.Permissions;
import rr.industries.util.TagData;

import java.util.List;
import java.util.stream.Collectors;

import static rr.industries.jooq.Tables.TAGS;

//todo: add check for permanent in remove tag

/**
 * @author Sam
 */
public class TagTable extends Table {
    private static Permissions globalPerm = Permissions.BOTOPERATOR;
    private static Permissions permanentPerm = Permissions.ADMIN;
    private static Permissions overwritePerm = Permissions.MOD;
    private static Logger LOG = LoggerFactory.getLogger(TagTable.class);
    Table localTags;
    Table globalTags;

    public TagTable(DSLContext connection) throws BotException {
        super(TAGS, connection);
    }

    /**
     * Changes if an existing tag is permanent
     * @param guild The id of the guild
     * @param name The name of the tag
     * @param global Should the tag be global
     * @param perm The permission level of the user
     * @return if the tag was found
     * @throws MissingPermsException if the user isn't authorized to modify an existing tag
     */
    public boolean setGlobal(Snowflake guild, String name, boolean global, Permissions perm) throws BotException {
        TagData tag = getTag(guild, name);
        if(tag == null)
            return false;
        tag.setGlobal(true);
        checkTagOverwritePerms(tag, perm);
        database.update(TAGS).set(TAGS.ISGLOBAL, global ? 1 : 0).where(TAGS.GUILDID.eq(guild.asLong())).and(TAGS.TAGNAME.eq(name)).execute();
        return true;
    }

    /**
     * Changes if an existing tag is permanent
     * @param guild The id of the guild
     * @param name The name of the tag
     * @param permanent Should the tag be permanent
     * @param perm The permission level of the user
     * @return if the tag was found
     * @throws MissingPermsException if the user isn't authorized to modify an existing tag
     */
    public boolean setPermanent(Snowflake guild, String name, boolean permanent, Permissions perm) throws MissingPermsException {
        TagData tag = getTag(guild, name);
        if(tag == null)
            return false;
        tag.setPermanent(true);
        checkTagOverwritePerms(tag, perm);
        database.update(TAGS).set(TAGS.ISPERMANENT, permanent ? 1 : 0).where(TAGS.GUILDID.eq(guild.asLong())).and(TAGS.TAGNAME.eq(name)).execute();
        return true;
    }

    /**
     * Creates a new tag
     * @param guild The id of the guild
     * @param name The name of the tag
     * @param content The content of the tag
     * @param perm The permission level of the user
     * @return The previous tag that was deleted, if any
     * @throws MissingPermsException if the user isn't authorized to modify an existing tag
     */
    public TagData makeTag(Snowflake guild, String name, String content, Permissions perm) throws MissingPermsException {
        TagData previous = deleteTag(guild, name, perm);
        database.insertInto(TAGS).columns(TAGS.GUILDID, TAGS.TAGNAME, TAGS.TAGCONTENT).values(guild.asLong(), name, content).execute();
        return previous;
    }

    /**
     * Deletes a tag
     * @param guild The id of the guild
     * @param name The name of the tag
     * @param perm The permission level of the user
     * @return The tag that was deleted, if any
     * @throws MissingPermsException if the user isn't authorized to modify the given tag
     */
    public TagData deleteTag(Snowflake guild, String name, Permissions perm) throws MissingPermsException {
        TagData tag = getTag(guild, name);
        if (tag != null) {
            checkTagOverwritePerms(tag, perm);
            database.deleteFrom(TAGS).where(TAGS.GUILDID.eq(guild.asLong()).and(TAGS.TAGNAME.eq(name))).execute();
        }
        return tag;
    }

    /**
     * Asserts that a user can modify a tag
     * @param tag The tag to be modified
     * @param perm The permission level of the user
     * @throws MissingPermsException If the user is not authorized to modify the given tag
     */
    private void checkTagOverwritePerms(TagData tag, Permissions perm) throws MissingPermsException {
        if (tag.isGlobal() && perm.level < globalPerm.level)
            throw new MissingPermsException("Edit Global Tags", globalPerm);
        else if (tag.isPermanent() && perm.level < permanentPerm.level)
            throw new MissingPermsException("Edit Permanent Tags", permanentPerm);
        else if (perm.level < overwritePerm.level)
            throw new MissingPermsException("Overwrite Tags", overwritePerm);
    }


    /**
     * Gets an tag by name in the context of a guild, preferring global tags
     * @param guild The id of the guild
     * @param name The name of the tag
     * @return The tag, or null if none were found
     */
    public TagData getTag(Snowflake guild, String name) {
        Record5<Long, String, Integer, Integer, String> matchingGlobal = database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.ISGLOBAL, TAGS.TAGCONTENT).from(TAGS).where(TAGS.ISGLOBAL.eq(1)).and(TAGS.TAGNAME.eq(name)).fetchAny();
        if(matchingGlobal != null) {
            return TagData.of(matchingGlobal);
        }
        Record5<Long, String, Integer, Integer, String> matchingLocal = database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.ISGLOBAL, TAGS.TAGCONTENT).from(TAGS).where(TAGS.GUILDID.eq(guild.asLong())).and(TAGS.TAGNAME.eq(name)).fetchAny();
        if(matchingLocal != null)
            return TagData.of(matchingLocal);
        return null;
    }

    /**
     * Gets all global tags
     * @return List of all global tags
     */
    public List<TagData> getGlobalTags() {
        return database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.ISGLOBAL, TAGS.TAGCONTENT)
                .from(TAGS)
                .where(TAGS.ISGLOBAL.eq(1))
                .fetch()
                .stream()
                .map(TagData::of)
                .collect(Collectors.toList());
    }

    /**
     * Gets every tag applicable to the context of the guild
     * Does not include global tags!
     * @param guild The id of the guild
     * @return A list of every applicable tag
     */
    public List<TagData> getAllTags(Snowflake guild) {
        return database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.ISGLOBAL, TAGS.TAGCONTENT)
                .from(TAGS)
                .where(TAGS.ISGLOBAL.eq(0))
                .and(TAGS.GUILDID.eq(guild.asLong()))
                .fetch()
                .stream()
                .map(TagData::of)
                .collect(Collectors.toList());
    }
}
