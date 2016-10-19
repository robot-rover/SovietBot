package rr.industries.util.sql;

import rr.industries.Configuration;
import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Sam
 */
public class PrefixTable extends Table implements ITable {
    private Configuration config;

    public PrefixTable(Statement executor, Configuration config) throws BotException {
        super("prefixtable", executor,
                new Column("guildid", "text", false),
                new Column("prefix", "text", false)
        );
        this.config = config;
        this.createIndex("prefixindex", "guildid", true);
    }

    public void setPrefix(IGuild guild, String prefix) throws BotException {
        if (prefix.equals(config.commChar))
            removeEntry(Value.of(guild.getID(), true), Value.empty());
        else
            insertValue(Value.of(guild.getID(), true), Value.of(prefix, false));
    }

    public String getPrefix(IGuild guild) {
        try {
            ResultSet set = queryValue(Value.of(guild.getID(), true), Value.empty());
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
