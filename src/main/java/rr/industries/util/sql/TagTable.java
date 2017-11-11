package rr.industries.util.sql;

import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.MissingPermsException;
import rr.industries.util.Permissions;
import rr.industries.util.TagData;
import sx.blah.discord.handle.obj.IGuild;

import javax.naming.ConfigurationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static rr.industries.jooq.Tables.*;

//todo: add check for permanent in remove tag

/**
 * @author Sam
 */
public class TagTable implements ITable {
    private static Permissions globalPerm = Permissions.BOTOPERATOR;
    private static Permissions permanentPerm = Permissions.ADMIN;
    private static Permissions overwritePerm = Permissions.MOD;
    private static Logger LOG = LoggerFactory.getLogger(TagTable.class);
    Table localTags;
    Table globalTags;
    private DSLContext database;

    public TagTable(DSLContext connection) throws BotException {
        this.database = connection;
        localTags = new Table(TAGS, database,
                new Field[]{TAGS.GUILDID, TAGS.TAGCONTENT, TAGS.TAGNAME, TAGS.ISPERMANENT},
                new Constraint[]{});
/*        database.alterTable(localTags.table).alterColumn(TAGS.GUILDID).setNotNull().execute();
        database.alterTable(localTags.table).alterColumn(TAGS.TAGNAME).setNotNull().execute();
        database.alterTable(localTags.table).alterColumn(TAGS.ISPERMANENT).setNotNull().execute();*/
        globalTags = new Table(GLOBALTAGS, database,
                new Field[]{GLOBALTAGS.TAGNAME, GLOBALTAGS.TAGCONTENT},
                new Constraint[]{DSL.constraint("TAGNAME_UK").unique(GLOBALTAGS.TAGNAME)});
        /*database.alterTable(globalTags.table).alterColumn(GLOBALTAGS.TAGNAME).setNotNull().execute();*/
    }

    /**
     * @return the old tag, if it could be found
     */
    public Optional<TagData> setGlobal(IGuild guild, String name, boolean global, Permissions perm) throws BotException {
        Optional<TagData> tag = getTag(guild, name);
        if (tag.isPresent()) {
            deleteTag(guild, name, perm);
            makeTag((global ? null : guild), name, tag.get().getContent(), tag.get().isPermanent(), perm);
        }
        return tag;
    }

    /**
     * @return the tag changed, if it was found
     */
    public Optional<TagData> setPermanent(@Nullable IGuild guild, String name, boolean permanent, Permissions perm) throws BotException {
        Optional<TagData> tag = getTag(guild, name);
        if (tag.isPresent()) {
            deleteTag(guild, name, perm);
            makeTag(guild, name, tag.get().getContent(), permanent, perm);
        }
        return tag;
    }


    /**
     * @return the previous tag, if it existed
     */
    public Optional<TagData> makeTag(@Nullable IGuild guild, String name, String content, boolean permanent, Permissions perm) throws BotException {
        Optional<TagData> previous = getTag(guild, name);
        if (previous.isPresent()) {
            checkTag(previous.get(), perm);
        }
        if (permanent && perm.level < permanentPerm.level)
            throw new MissingPermsException("Edit a Permanent Tag", permanentPerm);
        if (guild != null) {
            if(previous.isPresent())
                database.update(localTags.table).set(TAGS.TAGCONTENT, content).set(TAGS.ISPERMANENT, permanent).where(TAGS.GUILDID.eq(guild.getStringID()).and(TAGS.TAGNAME.eq(name))).execute();
            else
                database.insertInto(localTags.table).columns(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.TAGCONTENT).values(guild.getStringID(), name, permanent, content).execute();
        } else {
            if(previous.isPresent())
                database.update(globalTags.table).set(GLOBALTAGS.TAGCONTENT, content).where(TAGS.TAGNAME.eq(name)).execute();
            else
                database.insertInto(globalTags.table).columns(TAGS.TAGNAME, TAGS.TAGCONTENT).values(name, content).execute();
        }
        return previous;
    }

    public Optional<TagData> deleteTag(@Nullable IGuild guild, String name, Permissions perm) throws BotException {
        Optional<TagData> tag = getTag(guild, name);
        if (tag.isPresent()) {
            checkTag(tag.get(), perm);
            if (tag.get().isGlobal() || guild == null)
                database.deleteFrom(globalTags.table).where(GLOBALTAGS.TAGNAME.eq(name)).execute();
            else
                database.deleteFrom(localTags.table).where(TAGS.GUILDID.eq(guild.getStringID()).and(TAGS.TAGNAME.eq(name))).execute();
        }
        return tag;
    }

    private void checkTag(TagData tag, Permissions perm) throws MissingPermsException {
        if (tag.isGlobal() && perm.level < globalPerm.level)
            throw new MissingPermsException("Edit Global Tags", globalPerm);
        else if (tag.isPermanent() && perm.level < permanentPerm.level)
            throw new MissingPermsException("Edit Permanent Tags", permanentPerm);
        else if (perm.level < overwritePerm.level)
            throw new MissingPermsException("Overwrite Tags", overwritePerm);
    }


    public Optional<TagData> getTag(@Nullable IGuild guild, String name) throws BotException {
        if (guild != null){
            Record4<String, String, Integer, String> record = database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.TAGCONTENT).from(localTags.table).where(TAGS.GUILDID.eq(guild.getStringID()).and(TAGS.TAGNAME.eq(name))).fetchAny();
            if (record != null)
                return Optional.of(new TagData(record));
        }
        Record2<String, String> record = database.select(GLOBALTAGS.TAGNAME, GLOBALTAGS.TAGCONTENT).from(globalTags.table).where(GLOBALTAGS.TAGNAME.eq(name)).fetchAny();
        if (record != null)
            return Optional.of(new TagData(record.component1(), record.component2()));
        return Optional.empty();
    }

    public List<TagData> getGlobalTags() throws BotException {
        return database.select(GLOBALTAGS.TAGNAME, GLOBALTAGS.TAGCONTENT).from(globalTags.table).fetch().stream().map(TagData::new).collect(Collectors.toList());
    }

    public List<TagData> getAllTags(IGuild guild) throws BotException {
        return database.select(TAGS.GUILDID, TAGS.TAGNAME, TAGS.ISPERMANENT, TAGS.TAGCONTENT).from(localTags.table).where(TAGS.GUILDID.eq(guild.getStringID())).fetch().stream().map(TagData::new).collect(Collectors.toList());
    }
}
