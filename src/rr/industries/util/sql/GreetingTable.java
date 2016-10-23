package rr.industries.util.sql;

import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author Sam
 */
public class GreetingTable extends Table implements ITable {
    public GreetingTable(Connection connection) throws BotException {
        super("greetingtable", connection,
                new Column("guildid", "text", false),
                new Column("joinmessage", "text", true),
                new Column("leavemessage", "text", true));
        createIndex("greetingindex", "guildid", true);
    }

    public Optional<String> getJoinMessage(IGuild guild) throws BotException {
        try (Statement executor = connection.createStatement()) {
            ResultSet result = queryValue(executor, Value.of(guild.getID(), true), Value.empty(), Value.empty());
            if (result.next())
                if (result.getString("joinmessage").equals("null")) {
                    return Optional.empty();
                } else {
                    return Optional.of(result.getString("joinmessage"));
                }
            else
                return Optional.empty();

        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }

    public Optional<String> getLeaveMessage(IGuild guild) throws BotException {
        try (Statement executor = connection.createStatement()) {
            ResultSet result = queryValue(executor, Value.of(guild.getID(), true), Value.empty(), Value.empty());
            if (result.next())
                if (result.getString("leavemessage").equals("null")) {
                    return Optional.empty();
                } else {
                    return Optional.of(result.getString("leavemessage"));
                }
            else
                return Optional.empty();

        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }

    public void setJoinMessage(IGuild guild, String message) throws BotException {
        insertValue(Value.of(guild.getID(), true), Value.of(message, false), Value.empty());
    }

    public void setLeaveMessage(IGuild guild, String message) throws BotException {
        insertValue(Value.of(guild.getID(), true), Value.empty(), Value.of(message, false));
    }
}
