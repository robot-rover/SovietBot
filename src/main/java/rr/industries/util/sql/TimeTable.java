package rr.industries.util.sql;

import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import rr.industries.exceptions.BotException;
import sx.blah.discord.handle.obj.IUser;
import static rr.industries.jooq.Tables.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author robot_rover
 */
public class TimeTable extends Table implements ITable {
    public TimeTable(DSLContext connection) throws BotException {
        super(USERS, connection,
                new Field[]{USERS.USERID, USERS.TIMEZONE},
                new Constraint[]{DSL.constraint("USERID_UK").unique(USERS.USERID)}
        );
    }

    public Optional<String> getTimeZone(IUser user) throws BotException {
        Record2<String, String> time = database.select(USERS.USERID, USERS.TIMEZONE).from(table).where(USERS.USERID.eq(user.getStringID())).fetchAny();
        if(time != null)
            return Optional.of(time.component2());
        return Optional.empty();
    }

    public void setTimeZone(IUser user, String timeZone) throws BotException {
        if(getTimeZone(user).isPresent()){
            database.update(table).set(USERS.TIMEZONE, timeZone).where(USERS.USERID.eq(user.getStringID())).execute();
        }
        else {
            database.insertInto(table).columns(USERS.USERID, USERS.TIMEZONE).values(user.getStringID(), timeZone).execute();
        }
    }
}
