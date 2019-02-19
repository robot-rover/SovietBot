/*
 * This file is generated by jOOQ.
*/
package rr.industries.jooq;


import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.AbstractKeys;

import rr.industries.jooq.tables.Guilds;


/**
 * A class modelling indexes of tables of the <code></code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index SQLITE_AUTOINDEX_GUILDS_1 = Indexes0.SQLITE_AUTOINDEX_GUILDS_1;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 extends AbstractKeys {
        public static Index SQLITE_AUTOINDEX_GUILDS_1 = createIndex("sqlite_autoindex_guilds_1", Guilds.GUILDS, new OrderField[] { Guilds.GUILDS.GUILDID }, true);
    }
}
