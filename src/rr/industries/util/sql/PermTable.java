package rr.industries.util.sql;

import rr.industries.Configuration;
import rr.industries.exceptions.BotException;
import rr.industries.util.BotUtils;
import rr.industries.util.Entry;
import rr.industries.util.Permissions;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author robot_rover
 */
public class PermTable extends Table implements ITable {
    private Configuration config;

    public PermTable(Connection connection, Configuration config) throws BotException {
        super("perms", connection,
                new Column("guildid", "text", false),
                new Column("userid", "text", false),
                new Column("perm", "int", false)
        );
        this.config = config;
        this.createIndex("permsindex", "guildid, userid", true);

    }

    public Permissions getPerms(IUser user, IGuild guild) throws BotException {
        if (Arrays.asList(config.operators).contains(user.getStringID()))
            return Permissions.BOTOPERATOR;
        if (guild == null) {
            return Permissions.REGULAR;
        }
        if (guild.getOwner().equals(user))
            return Permissions.SERVEROWNER;
        try (Statement executor = connection.createStatement()) {
            ResultSet result = queryValue(executor, Value.of(guild.getStringID(), true), Value.of(user.getStringID(), true), Value.empty());
            if (result.next()) {
                return BotUtils.toPerms(result.getInt("perm"));
            }
            return Permissions.NORMAL;
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }

    public void setPerms(IGuild guild, IUser user, Permissions permissions) throws BotException {
        if (permissions == Permissions.NORMAL)
            removeEntry(Value.of(guild.getStringID(), true), Value.of(user.getStringID(), true), Value.empty());
        else
            insertValue(Value.of(guild.getStringID(), true), Value.of(user.getStringID(), true), Value.of(Integer.toString(permissions.level), false));
    }

    public List<Entry<Long, Integer>> getAllPerms(IGuild guild) {
        try (Statement executor = connection.createStatement()) {
            List<Entry<Long, Integer>> list = new ArrayList<>();
            ResultSet rs = executor.executeQuery("SELECT userid, perm FROM " + getName() + " where guildid=" + guild.getStringID() + " ORDER BY perm DESC;");
            while (rs.next()) {
                list.add(new Entry<>(Long.parseLong(rs.getString("userid")), rs.getInt("perm")));
            }
            return list;
        } catch (SQLException ex) {
            LOG.warn("SQL Error", ex);
        }
        return new ArrayList<>();
    }
}
