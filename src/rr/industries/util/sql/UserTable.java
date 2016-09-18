package rr.industries.util.sql;

import sx.blah.discord.handle.obj.IUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/17/2016
 */
public class UserTable extends Table {
    public UserTable(Statement executor) {
        super("users", executor,
                new Column("userid", "text", false),
                new Column("timezone", "text", true),
                new Column("zipcode", "text", true)
        );
        this.createIndex("userindex", "userid", true);
    }

    public Optional<String> getTimeZone(IUser user) {
        try {
            ResultSet rs = queryValue("timezone", "userid='" + user.getID() + "'");
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
        setValue("userid='" + user.getID() + "'", user.getID(), timeZone, null);
    }
}
