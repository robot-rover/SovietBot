package rr.industries.util.sql;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.MissingPermsException;
import rr.industries.util.Permissions;
import rr.industries.util.TagData;
import sx.blah.discord.handle.obj.IGuild;

import javax.naming.ConfigurationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public TagTable(Statement executor) {
        localTags = new Table("tags", executor,
                new Column("guildid", "text", false),
                new Column("tagname", "text", false),
                new Column("tagcontent", "text", false),
                new Column("ispermanent", "integer", false, false)
        ).createIndex("tagindex", "guildid, tagname", true);
        globalTags = new Table("globaltags", executor,
                new Column("tagname", "text", false),
                new Column("tagcontent", "text", false)
        ).createIndex("globaltagindex", "tagname", true);
    }

    /**
     * @return the old tag, if it could be found
     */
    public Optional<TagData> setGlobal(IGuild guild, String name, boolean global, Permissions perm) throws MissingPermsException {
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
    public Optional<TagData> setPermanent(@Nullable IGuild guild, String name, boolean permanent, Permissions perm) throws MissingPermsException {
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
    public Optional<TagData> makeTag(@Nullable IGuild guild, String name, String content, boolean permanent, Permissions perm) throws MissingPermsException {
        Optional<TagData> previous = getTag(guild, name);
        if (previous.isPresent()) {
            checkTag(previous.get(), perm);
        }
        if (permanent && perm.level < permanentPerm.level)
            throw new MissingPermsException("Edit a Permanent Tag", permanentPerm);
        if (guild != null) {
            localTags.insertValue(Value.of(guild.getID(), true), Value.of(name, true), Value.of(content, false), Value.of(permanent, false));
        } else {
            globalTags.insertValue(Value.of(name, true), Value.of(content, false));
        }
        return previous;
    }

    public Optional<TagData> deleteTag(@Nullable IGuild guild, String name, Permissions perm) throws MissingPermsException {
        Optional<TagData> tag = getTag(guild, name);
        if (tag.isPresent()) {
            checkTag(tag.get(), perm);
            if (tag.get().isGlobal())
                globalTags.removeEntry(Value.of(name, true), Value.empty());
            else
                //noinspection OptionalGetWithoutIsPresent
                localTags.removeEntry(Value.of(tag.get().getGuild().get(), true), Value.of(name, true), Value.empty(), Value.empty());
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


    public Optional<TagData> getTag(@Nullable IGuild guild, String name) {
        try {
            if (guild != null) {
                ResultSet result = localTags.queryValue(Value.of(guild.getID(), true), Value.of(name, true), Value.empty(), Value.empty());
                if (result.next()) {
                    return Optional.of(getTagDataFromSQL(result));
                }
            }
            ResultSet result2 = globalTags.queryValue(Value.of(name, true), Value.empty());
            if (result2.next()) {
                return Optional.of(getTagDataFromSQL(result2));
            }
        } catch (SQLException | ConfigurationException e) {
            LOG.error(SQLException.class.getName(), e);
        }
        return Optional.empty();
    }

    /**
     * .next() is NOT called, make sure the cursor is where you want it
     */
    private TagData getTagDataFromSQL(ResultSet result) throws SQLException, ConfigurationException {
        String table = result.getMetaData().getTableName(1);
        switch (table) {
            case "tags":
                return new TagData(result.getString("guildid"), result.getString("tagname"),
                        result.getString("tagcontent"), result.getBoolean("ispermanent"));
            case "globaltags":
                return new TagData(result.getString("tagname"), result.getString("tagcontent"));
            default:
                throw new ConfigurationException("ResultSet did not contain a global or local tag table");
        }
    }

    public List<TagData> getGlobalTags() {
        try {
            ResultSet result = globalTags.queryValue(Value.empty(), Value.empty());
            List<TagData> tags = new ArrayList<>();
            while (result.next()) {
                tags.add(getTagDataFromSQL(result));
            }
            return tags;
        } catch (SQLException | ConfigurationException ex) {
            LOG.error(SQLException.class.getName(), ex);
        }
        return new ArrayList<>();
    }

    public List<TagData> getAllTags(IGuild guild) {
        try {
            ResultSet result = localTags.queryValue(Value.of(guild.getID(), true), Value.empty(), Value.empty(), Value.empty());
            List<TagData> tags = new ArrayList<>();
            while (result.next()) {
                tags.add(getTagDataFromSQL(result));
            }
            return tags;
        } catch (SQLException | ConfigurationException ex) {
            LOG.error(SQLException.class.getName(), ex);
        }
        return new ArrayList<>();
    }
}
