package rr.industries;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import rr.industries.jooq.tables.Globaltags;
import rr.industries.pageengine.MarkdownEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static rr.industries.jooq.tables.Globaltags.GLOBALTAGS;

public class MarkdownEngineTest {
    public static void main(String[] args) throws SQLException {
        System.out.println(MarkdownEngine.generate("*Italics*"));
        System.out.println(MarkdownEngine.generate("**Bold**"));
        System.out.println(MarkdownEngine.generate("```code```"));
        System.out.println(MarkdownEngine.generate("`code`"));
        Connection conn = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
        DSLContext connection = DSL.using(conn, SQLDialect.SQLITE);

        Result<Record1<String>> result = connection.select(GLOBALTAGS.TAGCONTENT).from(GLOBALTAGS).fetch();
        for(Record1<String> record : result){
            System.out.println(MarkdownEngine.generate(record.component1()));
        }

    }
}
