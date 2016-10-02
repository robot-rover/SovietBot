package rr.industries.util.sql;

import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Sam
 */
public class TagTable extends Table {
    public TagTable(Statement executor) {
        super("tags", executor,
                new Column("guildid", "text", false),
                new Column("tagname", "text", false),
                new Column("tagcontent", "text", false)
        );
        this.createIndex("tagindex", "guildid, tagname", true);
    }

    public void makeTag(IGuild guild, String name, String content) {
        LOG.info(content);
        insertValue(Value.of(guild.getID(), true), Value.of(name, true), Value.of(content, false));
    }

    public boolean deleteTag(IGuild guild, String name) {
        boolean found = getTag(guild, name).isPresent();
        removeEntry(Value.of(guild.getID(), true), Value.of(name, true), Value.empty());
        return found;
    }

    public Optional<String> getTag(IGuild guild, String name) {
        try {
            ResultSet result = queryValue(Value.of(guild.getID(), true), Value.of(name, true), Value.of(null, false));
            if (result.next()) {
                return Optional.of(result.getString("tagcontent"));
            }
        } catch (SQLException e) {
            LOG.error(SQLException.class.getName(), e);
        }
        return Optional.empty();
    }

    public List<String> getAllTags(IGuild guild) {
        try {
            ResultSet result = queryValue(Value.of(guild.getID(), true), Value.empty(), Value.empty());
            List<String> tags = new ArrayList<>();
            while (result.next()) {
                tags.add(result.getString("tagname"));
            }
            return tags;
        } catch (SQLException e) {
            LOG.error(SQLException.class.getName(), e);
        }
        return new ArrayList<>();
    }
}
