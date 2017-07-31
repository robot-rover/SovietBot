package rr.industries.util.sql;

import rr.industries.Configuration;
import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Sam
 */
public class PrefixTable extends Table implements ITable {
    private Configuration config;

    public PrefixTable(Connection connection, Configuration config) throws BotException {
        super("prefixtable", connection,
                new Column("guildid", "text", false),
                new Column("prefix", "text", false)
        );
        this.config = config;
        this.createIndex("prefixindex", "guildid", true);
    }

    public void setPrefix(IGuild guild, String prefix) throws BotException {
        if (prefix.equals(config.commChar))
            removeEntry(Value.of(guild.getStringID(), true), Value.empty());
        else
            insertValue(Value.of(guild.getStringID(), true), Value.of(prefix, false));
    }

    public String getPrefix(IMessage e) {
        if (e.getChannel().isPrivate()) {
            return getPrefix((IGuild) null);
        } else {
            return getPrefix(e.getGuild());
        }
    }

    private String getPrefix(IGuild guild) {
        if (guild == null) {
            return config.commChar;
        }
        try (Statement executor = connection.createStatement()) {
            ResultSet set = queryValue(executor, Value.of(guild.getStringID(), true), Value.empty());
            if (set.next()) {
                return set.getString("prefix");
            } else {
                return config.commChar;
            }
        } catch (SQLException | BotException ex) {
            LOG.error("Silenced " + ex.getClass().getName(), ex);
            return config.commChar;
        }
    }


}
