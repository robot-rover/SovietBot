package rr.industries.util.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.util.Entry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/17/2016
 */
public class Table {
    static Logger LOG = LoggerFactory.getLogger(Table.class);
    private Column[] columns;
    private String tableName;
    Statement executor;

    protected Table(String tableName, Statement executor, Column... columns) {
        this.columns = columns;
        this.executor = executor;
        this.tableName = tableName;
        StringBuilder initTable = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            initTable.append(columns[i].toString());
            if (i + 1 < columns.length)
                initTable.append(",");
            initTable.append("\n");
        }
        try {
            executor.execute("create table if not exists " + tableName + "(" + initTable.toString() +
                    ");PRAGMA AUTO_VACUUM = FULL;");

            ResultSetMetaData metaData = executor.executeQuery("SELECT * FROM " + tableName).getMetaData();
            ArrayList<String> currentColumns = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                currentColumns.add(metaData.getColumnName(i));
            }
            for (Column column : columns) {
                if (!currentColumns.contains(column.name)) {
                    LOG.info("Adding Column: " + column.name);
                    executor.execute("ALTER TABLE " + tableName + " ADD COLUMN " + column.toString());
                }
            }
        } catch (SQLException ex) {
            LOG.warn("SQL Error", ex);
        }
    }

    protected Table createIndex(String indexName, String columns, boolean unique) {
        try {
            executor.execute("DROP INDEX IF EXISTS " + indexName);
            LOG.info("Creating Index: " + indexName);
            executor.execute("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName + " on " + tableName + " (" + columns + ");");
        } catch (SQLException ex) {
            LOG.error("SQL Exception creating index: " + indexName, ex);
        }
        return this;
    }

    protected ResultSet queryValue(Value... vals) throws SQLException {
        List<Entry<Column, Value>> values = toEntryList(vals);
        return executor.executeQuery("Select " + values.stream().map(v -> v.first().name).collect(Collectors.joining(", ")) + " from " + tableName + " where " +
                values.stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'").collect(Collectors.joining(" AND ")));
    }

    @Deprecated
    protected void setValue(String conditions, String... vals) {
        try {
            executor.execute("REPLACE INTO " + tableName + " (" + getColumns() + ") VALUES(" + getValues(conditions, vals) + ")");
        } catch (SQLException ex) {
            LOG.warn("SQL Exception", ex);
        }
    }

    protected void insertValue(Value... vals) {
        try {
            executor.execute("DELETE FROM " + tableName + " WHERE " +
                    toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'").collect(Collectors.joining(" AND ")));
            executor.execute("INSERT INTO " + tableName + " VALUES (" +
                    Arrays.asList(vals).stream().map(v -> "'" + v + "'").collect(Collectors.joining(", ")) + ")");
        } catch (SQLException ex) {
            LOG.error("SQL Error", ex);
        }

    }

    protected String getColumns() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            string.append(columns[i].name);
            if (i + 1 < columns.length)
                string.append(", ");
        }
        return string.toString();
    }

    protected String getValues(String conditions, String[] cols) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] == null)
                string.append("(SELECT " + columns[i].name + " FROM " + tableName + " WHERE " + conditions + ")");
            else
                string.append("'" + cols[i] + "'");
            if (i + 1 < cols.length)
                string.append(", ");
        }
        return string.toString();
    }

    protected void removeEntry(Value... vals) {
        try {
            executor.execute("DELETE FROM " + tableName + " Where " +
                    toEntryList(vals).stream().filter(v -> v.second().shouldQuery()).map(v -> v.first().name + "='" + v.second() + "'")
                            .collect(Collectors.joining(" AND ")));
        } catch (SQLException ex) {
            LOG.error("SQL Error", ex);
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
