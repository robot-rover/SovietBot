package rr.industries;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JooQTest {
    private final static Logger LOG = LoggerFactory.getLogger(JooQTest.class);
    public static void main(String[] args){
        DSLContext connection;
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                LOG.error("Could not load SQLite JDBC Driver", ex);
                return;
            }
            Connection conn = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            connection = DSL.using(conn, SQLDialect.SQLITE);
        } catch (SQLException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
            return;
        }
        LOG.info("Database Initialized");
    }
}
