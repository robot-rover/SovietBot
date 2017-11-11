package rr.industries.util.sql;

import org.jooq.*;
import org.jooq.impl.DSL;
import rr.industries.exceptions.BotException;
import rr.industries.jooq.tables.Greetingtable;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import static rr.industries.jooq.Tables.*;


/**
 * @author Sam
 */
public class GreetingTable extends Table implements ITable {
    public GreetingTable(DSLContext connection) throws BotException {
        super(GREETINGTABLE, connection,
                new Field[]{GREETINGTABLE.GUILDID, GREETINGTABLE.JOINMESSAGE, GREETINGTABLE.LEAVEMESSAGE},
                new Constraint[]{DSL.constraint("GUILD_UK").unique(GREETINGTABLE.GUILDID)}
        );
        /*database.alterTable(table).alterColumn(GREETINGTABLE.GUILDID).setNotNull().execute();*/
    }

    public Optional<String> getJoinMessage(IGuild guild) throws BotException {
        return Optional.ofNullable(database.select(GREETINGTABLE.JOINMESSAGE).from(table).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).fetchAny().component1());
    }

    public Optional<String> getLeaveMessage(IGuild guild) throws BotException {
        return Optional.ofNullable(database.select(GREETINGTABLE.LEAVEMESSAGE).from(table).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).fetchAny().component1());
    }

    public void setJoinMessage(IGuild guild, String message) throws BotException {
        Record1<String> previous = database.select(GREETINGTABLE.GUILDID).from(table).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).fetchAny();
        if(previous == null)
            database.insertInto(GREETINGTABLE).columns(GREETINGTABLE.GUILDID, GREETINGTABLE.JOINMESSAGE, GREETINGTABLE.LEAVEMESSAGE).values(guild.getStringID(), message, null).execute();
        else
            database.update(table).set(GREETINGTABLE.JOINMESSAGE, message).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).execute();
    }

    public void setLeaveMessage(IGuild guild, String message) throws BotException {
        Record1<String> previous = database.select(GREETINGTABLE.GUILDID).from(table).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).fetchAny();
        if(previous == null)
            database.insertInto(GREETINGTABLE).columns(GREETINGTABLE.GUILDID, GREETINGTABLE.JOINMESSAGE, GREETINGTABLE.LEAVEMESSAGE).values(guild.getStringID(), null, message).execute();
        else
            database.update(table).set(GREETINGTABLE.LEAVEMESSAGE, message).where(GREETINGTABLE.GUILDID.eq(guild.getStringID())).execute();
    }
}
