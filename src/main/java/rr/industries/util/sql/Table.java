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
    protected org.jooq.Table table;
    protected DSLContext database;

    protected Table(org.jooq.Table table, DSLContext context, Field[] fields, Constraint[] constraints) throws BotException {
        this.database = context;
        this.table = table;

        database.createTableIfNotExists(table).columns(fields).constraints(constraints).execute();
        InformationSchema is = database.informationSchema(table);
        List<String> columnNames = is.getColumns().stream().map(org.jooq.util.xml.jaxb.Column::getColumnName).collect(Collectors.toList());
        for(Field field : fields){
            if(!columnNames.contains(field.getName())){
                database.alterTable(table).add(field.getName(), field.getDataType()).execute();
            }
        }
    }

    /*protected ResultSet queryValue(Statement executor, Value... vals) throws BotException {
        List<Entry<Column, Value>> values = toEntryList(vals);
        try {
            ResultSet result = executor.executeQuery("Select " + values.stream().map(v -> v.first().name).collect(Collectors.joining(", ")) + " from " + tableName +
                    getConditions(vals));
            return result;
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }*/

    /**
     * @return true if overwritten, false if created
     */
    /*protected boolean insertValue(Value... vals) throws BotException {
        if (vals.length != columns.length) {
            throw new ServerError("Not a Value object for every column");
        }
        boolean found;
        ResultSet result;
        Value[] queryVals = new Value[vals.length];
        try (Statement executor = connection.createStatement()) {
            result = queryValue(executor, vals);
            found = result.next();
            for (int i = 0; i < vals.length; i++) {
                if (found && vals[i].isBlank()) {
                    queryVals[i] = Value.of(result.getString(columns[i].name), false);
                } else {
                    queryVals[i] = vals[i];
                }
            }
            executor.execute("DELETE FROM " + tableName + getConditions(vals));
            executor.execute("INSERT INTO " + tableName + " VALUES (" +
                    Arrays.stream(queryVals).map(v -> "'" + v + "'").collect(Collectors.joining(", ")) + ")");
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
        return found;

    }*/

    /*private String getConditions(Value... vals) {
        if (toEntryList(vals).stream().noneMatch(v -> v.second().shouldQuery()))
            return "";
        return " where " + toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'").collect(Collectors.joining(" AND "));
    }*/

    /*protected void removeEntry(Value... vals) throws BotException {
        try (Statement executor = connection.createStatement()) {
            executor.execute("DELETE FROM " + tableName + " Where " +
                    toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'")
                            .collect(Collectors.joining(" AND ")));
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }*/

    /*private List<Entry<Column, Value>> toEntryList(Value... vals) {
        if (vals.length != columns.length)
            throw new InputMismatchException("Received " + vals.length + " values. Required " + columns.length + ".");
        List<Entry<Column, Value>> entries = new ArrayList<>();
        for (int i = 0; i < vals.length; i++) {
            entries.add(new Entry<>(columns[i], vals[i]));
        }
        return entries;
    }*/

    public String getName() {
        return table.getName();
    }
}
