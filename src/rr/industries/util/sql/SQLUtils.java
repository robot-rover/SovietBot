package rr.industries.util.sql;

import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.util.BotActions;
import rr.industries.util.BotUtils;
import rr.industries.util.Permissions;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/6/2016
 */
public class SQLUtils {
    public static final Logger LOG = LoggerFactory.getLogger(SQLUtils.class);

    public static void initTable(String tableName, List<Column> columns, Statement executor, @Nullable BotActions actions) {
        StringBuilder initTable = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            initTable.append(columns.get(i).toString());
            if (i + 1 < columns.size())
                initTable.append(",");
            initTable.append("\n");
        }
        try {
            executor.execute("create table if not exists " + tableName + "(" + initTable.toString() +
                    ");PRAGMA AUTO_VACUUM = FULL;");

            ResultSetMetaData metaData = executor.executeQuery("SELECT * FROM " + tableName + " WHERE 0=1").getMetaData();
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
            if (actions != null) {
                actions.sqlError(ex, "createTable", LOG);
            } else {
                LOG.error("SQL Exception initializing table: " + tableName, ex);
            }
        }
    }

    public static void createIndex(String tableName, String indexName, String columns, boolean unique, Statement executor, @Nullable BotActions actions) {
        try {
            executor.execute("DROP INDEX IF EXISTS " + indexName);
            LOG.info("Creating Index: " + indexName);
            executor.execute("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName + " on " + tableName + " (" + columns + ");");
        } catch (SQLException ex) {
            if (actions != null) {
                actions.sqlError(ex, "createIndex", LOG);
            } else {
                LOG.error("SQL Exception creating index: " + indexName, ex);
            }
        }
    }

    public static void updatePerms(String userID, String guildID, Permissions perm, Statement executor, BotActions actions) {
        try {
            ResultSet rs = executor.executeQuery("SELECT perm from perms WHERE guildid=" + guildID + " AND userid=" + userID);
            if (!rs.next()) {
                if (perm.level > 0) {
                    executor.execute("INSERT INTO perms VALUES(" + guildID + ", " + userID + ", " + perm.level + ");");
                }
            } else {
                if (perm.level == 0) {
                    executor.execute("DELETE FROM perms WHERE guildid=" + guildID + " AND userid=" + userID);
                } else {
                    executor.execute("UPDATE perms Set perm=" + perm.level + " WHERE guildid=" + guildID + " AND userid=" + userID);
                }
            }
        } catch (SQLException ex) {
            actions.sqlError(ex, "CommContext<init>", LOG);
        }
    }

    public static Permissions getPerms(String userID, String guildID, Statement executor, BotActions actions) {
        try {
            ResultSet rs = executor.executeQuery("SELECT perm from perms WHERE guildid='" + guildID + "' AND userid='" + userID + "'");
            if (!rs.next()) {
                return Permissions.NORMAL;
            } else {
                return BotUtils.toPerms(rs.getInt("perm"));
            }
        } catch (SQLException ex) {
            actions.sqlError(ex, "CommContext<init>", LOG);
        }
        return Permissions.NORMAL;
    }

    public static ResultSet queryValue(String table, String columns, @Nullable String conditions, Statement executor) throws SQLException {
        return executor.executeQuery("Select " + columns + " from " + table + " where " + conditions);
    }

    /*public static void setValue(String table, String column, String value, Map<String, String> conditions, Statement executor) throws SQLException {
        String wheres = "";
        String init = "";
        String columnList = "";
        Iterator<String> it = conditions.keySet().iterator();
        while(it.hasNext()){
            String addKey = it.next();
            String addValue = conditions.get(addKey);
            wheres = wheres.concat(addKey + "='" + addValue + "', ");
            init = init.concat()
        }
        ResultSet rs = executor.executeQuery("Select" + column + " from " + table + " where" + wheres);
        if(rs.next()){
            executor.execute("UPDATE " + table + " Set " + column + "=" + value + " WHERE " + wheres);
        } else {
            executor.execute("INSERT INTO " + table + " VALUES(" + values + ");");
        }
    }*/
    public static String getTimezone(String userID, Statement executor, BotActions actions) {
        String timezone = null;
        try {
            ResultSet rs = executor.executeQuery("SELECT timezone From users where userid=" + userID + "");
            if (rs.next()) {
                timezone = rs.getString("timezone");
            }
        } catch (SQLException ex) {
            actions.sqlError(ex, "getTimezone", LOG);
        }
        return timezone;
    }

    public static void setTimezone(String userID, String timezone, Statement executor, BotActions actions) {
        try {
            ResultSet rs = executor.executeQuery("Select timezone from users where userid='" + userID + "'");
            if (rs.next()) {
                executor.execute("UPDATE users Set timezone='" + timezone + "' WHERE userid=" + userID);
            } else {
                executor.execute("INSERT INTO users VALUES('" + userID + "', '" + timezone + "');");
            }
        } catch (SQLException ex) {
            actions.sqlError(ex, "setTimezone", LOG);
        }
    }
}
