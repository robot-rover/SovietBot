package rr.industries.util.sql;

import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import rr.industries.Configuration;
import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static rr.industries.jooq.Tables.*;

/**
 * @author Sam
 */
public class PrefixTable extends Table implements ITable {
    private Configuration config;

    public PrefixTable(DSLContext connection, Configuration config) throws BotException {
        super(PREFIXTABLE, connection,
                new Field[]{PREFIXTABLE.GUILDID, PREFIXTABLE.PREFIX},
                new Constraint[]{DSL.constraint("GUILD_UK").unique(PREFIXTABLE.GUILDID)}
        );

        this.config = config;
    }

    public void setPrefix(IGuild guild, String prefix) throws BotException {
        if (prefix.equals(config.commChar))
            database.deleteFrom(table).where(PREFIXTABLE.GUILDID.eq(guild.getStringID())).execute();
        else
            database.update(table).set(PREFIXTABLE.PREFIX, prefix).where(PREFIXTABLE.GUILDID.eq(guild.getStringID())).execute();
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
        Record1<String> prefix = database.select(PREFIXTABLE.PREFIX).from(table).where(PREFIXTABLE.GUILDID.eq(guild.getStringID())).fetchAny();
        if(prefix != null)
            return prefix.component1();
        else
            return config.commChar;
    }


}
