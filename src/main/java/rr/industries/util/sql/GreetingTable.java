package rr.industries.util.sql;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import reactor.core.publisher.Mono;
import rr.industries.Configuration;
import rr.industries.util.Entry;

import java.util.Optional;

import static rr.industries.jooq.Tables.GUILDS;


/**
 * Table for storing per guild information, such as join and leave message settings and prefix settings
 */
public class GreetingTable extends Table {
    Configuration config;
    public GreetingTable(DSLContext connection, Configuration config) {
        super(GUILDS, connection);
        this.config = config;
    }

    /**
     * Gets the message to be sent when a user joins a guild
     * @param guildId The id of the guild
     * @return An entry that contains the Message and the id of the channel to send it in (both may be null)
     */
    public Entry<String, Long> getJoinMessage(Snowflake guildId) {
        Record2<String, Long> message = database.select(GUILDS.JOINMESSAGE, GUILDS.GREETCHANNEL).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        if(message == null)
            return new Entry<>(null, null);
        return new Entry<>(message.value1(), message.value2());
    }

    /**
     * Gets the message to be sent when a user leaves a guild
     * @param guildId The id of the guild
     * @return An entry that contains the Message and the id of the channel to send it in (both may be null)
     */
    public Entry<String, Long> getLeaveMessage(Snowflake guildId) {
        Record2<String, Long> message = database.select(GUILDS.LEAVEMESSAGE, GUILDS.GREETCHANNEL).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        if(message == null)
            return new Entry<>(null, null);
        return new Entry<>(message.value1(), message.value2());
    }

    /**
     * Sets the message to be sent when a user joins a guild
     * @param guildId The id of the guild
     * @param message The message to send
     * @return Does this guild have a defined Greeting Channel
     */
    public boolean setJoinMessage(Snowflake guildId, String message) {
        Record2<Long, Long> previous = database.select(GUILDS.GUILDID, GUILDS.GREETCHANNEL).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        if(previous == null) {
            database.insertInto(GUILDS).columns(GUILDS.GUILDID, GUILDS.JOINMESSAGE).values(guildId.asLong(), message).execute();
            return false;
        } else {
            database.update(GUILDS).set(GUILDS.JOINMESSAGE, message).where(GUILDS.GUILDID.eq(guildId.asLong())).execute();
            return previous.value2() != null;
        }
    }

    /**
     * Sets the message to be sent when a user joins a guild
     * @param guildId The id of the guild
     * @param message The message to send
     * @return Does this guild have a defined Greeting Channel
     */
    public boolean setLeaveMessage(Snowflake guildId, String message) {
        Record2<Long, Long> previous = database.select(GUILDS.GUILDID, GUILDS.GREETCHANNEL).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        if(previous == null) {
            database.insertInto(GUILDS).columns(GUILDS.GUILDID, GUILDS.LEAVEMESSAGE).values(guildId.asLong(), message).execute();
            return false;
        } else {
            database.update(GUILDS).set(GUILDS.LEAVEMESSAGE, message).where(GUILDS.GUILDID.eq(guildId.asLong())).execute();
            return previous.value2() != null;
        }
    }

    /**
     * Sets the Greeting Channel of a guild
     * @param guildId The id of the guild
     */
    public void setChannel(Snowflake guildId, Snowflake channel) {
        if(database.update(GUILDS).set(GUILDS.GREETCHANNEL, channel.asLong()).where(GUILDS.GUILDID.eq(guildId.asLong())).execute() == 0) {
            database.insertInto(GUILDS).columns(GUILDS.GUILDID, GUILDS.GREETCHANNEL).values(guildId.asLong(), channel.asLong()).execute();
        }
    }

    /**
     * Gets the Greeting channel of a guild
     * @param guildId The id of the guild
     * @return The id of the channel
     */
    public Optional<Snowflake> getChannel(Snowflake guildId) {
        Record1<Long> channel = database.select(GUILDS.GREETCHANNEL).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        return Optional.ofNullable(channel).map(Record1::value1).map(Snowflake::of);
    }

    /**
     * Sets the prefix of a guild
     * @param guild The id ofo the guild
     * @param prefix The new prefix
     */
    public void setPrefix(Snowflake guild, String prefix) {
        String setPrefix = prefix.equals(config.commChar) ? null : prefix;
        Record1<Long> previous = database.select(GUILDS.GUILDID).from(GUILDS).where(GUILDS.GUILDID.eq(guild.asLong())).fetchAny();
        if (previous == null) {
            database.insertInto(GUILDS).columns(GUILDS.GUILDID, GUILDS.PREFIX).values(guild.asLong(), setPrefix).execute();
        } else {
            database.update(GUILDS).set(GUILDS.PREFIX, prefix).where(GUILDS.GUILDID.eq(guild.asLong())).execute();
        }
    }

    public Mono<String> getPrefix(Message e) {
        return e.getGuild().map(Guild::getId).map(this::getPrefix).defaultIfEmpty(config.commChar);
    }

    /**
     * Gets the applicable prefix for the guild
     * @param guildId The id of the guild
     * @return The prefix
     */
    private String getPrefix(Snowflake guildId) {
        Record1<String> prefix = database.select(GUILDS.PREFIX).from(GUILDS).where(GUILDS.GUILDID.eq(guildId.asLong())).fetchAny();
        return Optional.ofNullable(prefix).map(Record1::value1).orElse(config.commChar);
    }

    /**
     * Should the guild be filtered
     * @param guildID The id of the guild
     * @return if the guild should be filtered
     */
    public boolean shouldFilter(Snowflake guildID) {
        return Optional.ofNullable(database.select(GUILDS.FILTER).from(GUILDS).where(GUILDS.GUILDID.eq(guildID.asLong())).fetchAny()).map(Record1::value1).orElse(0) > 0;
    }

    /**
     * Set if a guild should be filtered
     * @param guildid The id of the guild
     * @param shouldFilter should the guild be filtered
     */
    public void setFilter(Snowflake guildid, boolean shouldFilter){
        Record1<Integer> previous = database.select(GUILDS.FILTER).from(GUILDS).where(GUILDS.GUILDID.eq(guildid.asLong())).fetchAny();
        if (previous == null) {
            database.insertInto(GUILDS).columns(GUILDS.GUILDID, GUILDS.FILTER).values(guildid.asLong(), shouldFilter ? 1 : 0).execute();
        } else {
            database.update(GUILDS).set(GUILDS.FILTER, shouldFilter ? 1 : 0).where(GUILDS.GUILDID.eq(guildid.asLong())).execute();
        }
    }

    /**
     * Gets info about a guild
     * @param guildid The id of the guild
     * @return The guild's prefix, greet channel, join message, and leave message (may be null)
     */
    public Optional<Record4<String, Long, String, String>> getServerInfo(Snowflake guildid) {
        Record4<String, Long, String, String> info = database.select(GUILDS.PREFIX, GUILDS.GREETCHANNEL, GUILDS.JOINMESSAGE, GUILDS.LEAVEMESSAGE).from(GUILDS).where(GUILDS.GUILDID.eq(guildid.asLong())).fetchAny();
        return Optional.ofNullable(info);
    }
}
