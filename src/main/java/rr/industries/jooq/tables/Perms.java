/*
 * This file is generated by jOOQ.
*/
package rr.industries.jooq.tables;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import rr.industries.jooq.DefaultSchema;
import rr.industries.jooq.tables.records.PermsRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Perms extends TableImpl<PermsRecord> {

    private static final long serialVersionUID = -68804650;

    /**
     * The reference instance of <code>perms</code>
     */
    public static final Perms PERMS = new Perms();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PermsRecord> getRecordType() {
        return PermsRecord.class;
    }

    /**
     * The column <code>perms.guildid</code>.
     */
    public final TableField<PermsRecord, Long> GUILDID = createField("guildid", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>perms.userid</code>.
     */
    public final TableField<PermsRecord, Long> USERID = createField("userid", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>perms.perm</code>.
     */
    public final TableField<PermsRecord, Integer> PERM = createField("perm", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>perms</code> table reference
     */
    public Perms() {
        this(DSL.name("perms"), null);
    }

    /**
     * Create an aliased <code>perms</code> table reference
     */
    public Perms(String alias) {
        this(DSL.name(alias), PERMS);
    }

    /**
     * Create an aliased <code>perms</code> table reference
     */
    public Perms(Name alias) {
        this(alias, PERMS);
    }

    private Perms(Name alias, Table<PermsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Perms(Name alias, Table<PermsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Perms as(String alias) {
        return new Perms(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Perms as(Name alias) {
        return new Perms(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Perms rename(String name) {
        return new Perms(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Perms rename(Name name) {
        return new Perms(name, null);
    }
}
