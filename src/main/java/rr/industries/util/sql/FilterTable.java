package rr.industries.util.sql;

import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import rr.industries.exceptions.BotException;

import static rr.industries.jooq.Tables.*;

public class FilterTable extends Table implements ITable {
    public FilterTable(DSLContext connection) throws BotException {
        super(FILTERTABLE, connection,
                new Field[]{FILTERTABLE.GUILDID},
                new Constraint[]{DSL.constraint("GUILD_UK").unique(FILTERTABLE.GUILDID)}
        );
    }

    public boolean shouldFilter(long guildID){
        return database.select(FILTERTABLE.GUILDID).from(FILTERTABLE).where(FILTERTABLE.GUILDID.eq(String.valueOf(guildID))).fetchAny() != null;
    }

    public void setFilter(long guildid, boolean shouldFilter){
        if(shouldFilter)
            addToFilter(guildid);
        else
            removeFromFilter(guildid);
    }

    public void addToFilter(long guildID){
        if(!shouldFilter(guildID))
            database.insertInto(FILTERTABLE).set(FILTERTABLE.GUILDID, String.valueOf(guildID)).execute();
    }

    public void removeFromFilter(long guildID){
        database.deleteFrom(FILTERTABLE).where(FILTERTABLE.GUILDID.eq(String.valueOf(guildID))).execute();
    }
}
