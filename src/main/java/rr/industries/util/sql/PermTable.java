package rr.industries.util.sql;

import org.jooq.*;
import org.jooq.impl.DSL;
import rr.industries.Configuration;
import rr.industries.exceptions.BotException;
import rr.industries.util.BotUtils;
import rr.industries.util.Entry;
import rr.industries.util.Permissions;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static rr.industries.jooq.Tables.*;

/**
 * @author robot_rover
 */
public class PermTable extends Table implements ITable {
    private Configuration config;

    public PermTable(DSLContext connection, Configuration config) throws BotException {
        super(PERMS, connection,
                new Field[]{PERMS.GUILDID, PERMS.USERID, PERMS.PERM},
                new Constraint[]{}
        );
        /*database.alterTable(table).alterColumn(PERMS.GUILDID).setNotNull().execute();
        database.alterTable(table).alterColumn(PERMS.USERID).setNotNull().execute();
        database.alterTable(table).alterColumn(PERMS.PERM).setNotNull().execute();*/
        this.config = config;
    }

    private Permissions getPerms(IUser user, IGuild guild) throws BotException {
        if (Arrays.asList(config.operators).contains(user.getStringID()))
            return Permissions.BOTOPERATOR;
        if (guild == null) {
            return Permissions.REGULAR;
        }
        if (guild.getOwner().equals(user))
            return Permissions.SERVEROWNER;
        Record1<Integer> perm = database.select(PERMS.PERM).from(table).where(PERMS.GUILDID.eq(guild.getStringID()).and(PERMS.USERID.eq(user.getStringID()))).fetchAny();
        if(perm != null)
            return BotUtils.toPerms(perm.value1());
        else
            return Permissions.NORMAL;
    }

    public Permissions getPerms(IUser user, IMessage e) throws BotException {
        if (e.getChannel().isPrivate())
            return getPerms(user, (IGuild) null);
        else
            return getPerms(user, e.getGuild());
    }

    public void setPerms(IGuild guild, IUser user, Permissions permissions) throws BotException {
        if (permissions == Permissions.NORMAL)
            database.deleteFrom(table).where(PERMS.GUILDID.eq(guild.getStringID()).and(PERMS.USERID.eq(user.getStringID()))).execute();
        else if(database.update(table).set(PERMS.PERM, permissions.level).where(PERMS.GUILDID.eq(guild.getStringID()).and(PERMS.USERID.eq(user.getStringID()))).execute() == 0){
            database.insertInto(table).set(PERMS.PERM, permissions.level).set(PERMS.GUILDID, guild.getStringID()).set(PERMS.USERID, user.getStringID()).execute();
        }
    }

    public List<Record2<String, Integer>> getAllPerms(IGuild guild) {
        return new ArrayList<>(database.select(PERMS.USERID, PERMS.PERM).from(table).where(PERMS.GUILDID.eq(guild.getStringID())).orderBy(PERMS.PERM.desc()).fetch());
    }
}
