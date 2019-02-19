package rr.industries.util.sql;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import reactor.core.publisher.Mono;
import rr.industries.Configuration;
import rr.industries.util.BotUtils;
import rr.industries.util.Permissions;

import java.util.ArrayList;
import java.util.List;

import static rr.industries.jooq.Tables.PERMS;

/**
 * @author robot_rover
 */
public class PermTable extends Table {
    private Configuration config;

    public PermTable(DSLContext connection, Configuration config) {
        super(PERMS, connection);
        this.config = config;
    }

    private Permissions getPerms(User user, Guild guild) {

        if (config.isOperator(user.getId()))
            return Permissions.BOTOPERATOR;
        if (guild.getOwnerId().equals(user.getId()))
            return Permissions.SERVEROWNER;
        Record1<Integer> perm = database.select(PERMS.PERM).from(PERMS).where(PERMS.GUILDID.eq(guild.getId().asLong()).and(PERMS.USERID.eq(user.getId().asLong()))).fetchAny();
        if(perm != null) {
            return BotUtils.toPerms(perm.value1());
        }
        else
            return Permissions.NORMAL;
    }

    public Mono<Permissions> getPerms(User user, Message e) {
        return e.getGuild().hasElement().flatMap(v -> {
            if(v) {
                return e.getGuild().map(g -> getPerms(user, g));
            } else {
                return Mono.just(Permissions.SERVEROWNER);
            }
        });
    }

    public void setPerms(Snowflake guild, Snowflake user, Permissions permissions) {
        if (permissions == Permissions.NORMAL)
            database.deleteFrom(PERMS).where(PERMS.GUILDID.eq(guild.asLong()).and(PERMS.USERID.eq(user.asLong()))).execute();
        else if(database.update(PERMS).set(PERMS.PERM, permissions.level).where(PERMS.GUILDID.eq(guild.asLong()).and(PERMS.USERID.eq(user.asLong()))).execute() == 0){
            database.insertInto(PERMS).set(PERMS.PERM, permissions.level).set(PERMS.GUILDID, guild.asLong()).set(PERMS.USERID, user.asLong()).execute();
        }
    }

    public List<Record2<Long, Integer>> getAllPerms(Snowflake guild) {
        return new ArrayList<>(database.select(PERMS.USERID, PERMS.PERM).from(PERMS).where(PERMS.GUILDID.eq(guild.asLong())).orderBy(PERMS.PERM.desc()).fetch());
    }
}
