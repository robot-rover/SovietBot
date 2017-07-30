package rr.industries.util.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.util.Entry;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author robot_rover
 */
public class Table {
    static Logger LOG = LoggerFactory.getLogger(Table.class);
    protected Column[] columns;
    protected String tableName;
    Connection connection;

    protected Table(String tableName, Connection connection, Column... columns) throws BotException {
        this.columns = columns;
        this.connection = connection;
        this.tableName = tableName;
        StringBuilder initTable = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            initTable.append(columns[i].toString());
            if (i + 1 < columns.length)
                initTable.append(",");
            initTable.append("\n");
        }
        try (Statement executor = connection.createStatement()) {
            executor.execute("create table if not exists " + tableName + "(" + initTable.toString() +
                    ");PRAGMA AUTO_VACUUM = FULL;");

            ResultSetMetaData metaData = executor.executeQuery("SELECT * FROM " + tableName).getMetaData();
            ArrayList<String> currentColumns = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                currentColumns.add(metaData.getColumnName(i));
            }
            for (Column column : columns) {
                if (!currentColumns.contains(column.name)) {
                    LOG.info("Adding Column: " + column.toString());
                    executor.execute("ALTER TABLE " + tableName + " ADD COLUMN " + column.toString());
                }
            }
        } catch (SQLException ex) {
            throw new ServerError("Could not create table " + tableName, ex);
        }
    }

    protected Table createIndex(String indexName, String columns, boolean unique) throws BotException {
        try (Statement executor = connection.createStatement()) {
            executor.execute("DROP INDEX IF EXISTS " + indexName);
            LOG.info("Creating Index: " + indexName);
            executor.execute("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName + " on " + tableName + " (" + columns + ");");
        } catch (SQLException ex) {
            throw new ServerError("SQL Exception creating index: " + indexName, ex);
        }
        return this;
    }
    
    protected ResultSet queryValue(Statement executor, Value... vals) throws BotException {
        List<Entry<Column, Value>> values = toEntryList(vals);
        try {
            ResultSet result = executor.executeQuery("Select " + values.stream().map(v -> v.first().name).collect(Collectors.joining(", ")) + " from " + tableName +
                    getConditions(vals));
            return result;
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }

    /**
     * @return true if overwritten, false if created
     */
    protected boolean insertValue(Value... vals) throws BotException {
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

    }

    private String getConditions(Value... vals) {
        if (toEntryList(vals).stream().noneMatch(v -> v.second().shouldQuery()))
            return "";
        return " where " + toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'").collect(Collectors.joining(" AND "));
    }

    protected void removeEntry(Value... vals) throws BotException {
        try (Statement executor = connection.createStatement()) {
            executor.execute("DELETE FROM " + tableName + " Where " +
                    toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'")
                            .collect(Collectors.joining(" AND ")));
        } catch (SQLException ex) {
            throw BotException.returnException(ex);
        }
    }

    private List<Entry<Column, Value>> toEntryList(Value... vals) {
        if (vals.length != columns.length)
            throw new InputMismatchException("Received " + vals.length + " values. Required " + columns.length + ".");
        List<Entry<Column, Value>> entries = new ArrayList<>();
        for (int i = 0; i < vals.length; i++) {
            entries.add(new Entry<>(columns[i], vals[i]));
        }
        return entries;
    }

    public String getName() {
        return tableName;
    }
}
