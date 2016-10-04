package rr.industries.util.sql;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/6/2016
 */
public class Column {
    public final String name;
    public final String type;
    public final boolean nullable;
    public final String defaultValue;

    public <T> Column(String name, String type, boolean nullable, T defaultValue) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.defaultValue = String.valueOf(defaultValue);
    }

    public Column(String name, String type, boolean nullable) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.defaultValue = null;
    }

    @Override
    public String toString() {
        return name + "\t" + type + "\t" + (nullable ? "" : "not NULL ") + (defaultValue != null ? "DEFAULT '" + defaultValue + "'" : "");
    }
}
