package rr.industries.util.sql;

import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author robot_rover
 */
public class TimeTable extends Table implements ITable {
    public TimeTable(Connection connection) throws BotException {
        super("users", connection,
                new Column("userid", "text", false),
                new Column("timezone", "text", true)
        );
        this.createIndex("userindex", "userid", true);
    }

    public Optional<String> getTimeZone(IUser user) throws BotException {
        try (Statement executor = connection.createStatement()) {
            ResultSet rs = queryValue(executor, Value.of(user.getStringID(), true), Value.empty());
            if (rs.next()) {
                return Optional.ofNullable(rs.getString("timezone"));
            } else {
                return Optional.empty();
            }
        } catch (SQLException ex) {
            LOG.error("SQL Error", ex);
            return Optional.empty();
        }
    }

    public void setTimeZone(IUser user, String timeZone) throws BotException {
        insertValue(Value.of(user.getStringID(), true), Value.of(timeZone, false));
    }
}
