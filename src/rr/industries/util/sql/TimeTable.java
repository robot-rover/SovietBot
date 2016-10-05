package rr.industries.util.sql;

import sx.blah.discord.handle.obj.IUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author robot_rover
 */
public class TimeTable extends Table implements ITable {
    public TimeTable(Statement executor) {
        super("users", executor,
                new Column("userid", "text", false),
                new Column("timezone", "text", true)
        );
        this.createIndex("userindex", "userid", true);
    }

    public Optional<String> getTimeZone(IUser user) {
        try {
            ResultSet rs = queryValue(Value.of(user.getID(), true), Value.empty());
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

    public void setTimeZone(IUser user, String timeZone) {
        insertValue(Value.of(user.getID(), true), Value.of(timeZone, false));
    }
}
