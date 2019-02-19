package rr.industries.util.sql;

import org.jooq.*;
import org.jooq.Constraint;
import org.jooq.util.xml.jaxb.InformationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author robot_rover
 */
public class Table {
    static Logger LOG = LoggerFactory.getLogger(Table.class);
    private org.jooq.Table table;
    protected DSLContext database;

    protected Table(org.jooq.Table table, DSLContext context) {
        this.database = context;
        this.table = table;
    }

    public String getName() {
        return table.getName();
    }
}
