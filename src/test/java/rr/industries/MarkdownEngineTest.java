package rr.industries;

import rr.industries.pageengine.MarkdownEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MarkdownEngineTest {
    public static void main(String[] args) throws SQLException {
        System.out.println(MarkdownEngine.generate("*Italics*"));
        System.out.println(MarkdownEngine.generate("**Bold**"));
        System.out.println(MarkdownEngine.generate("```code```"));
        System.out.println(MarkdownEngine.generate("`code`"));
        Connection conn = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");

    }
}
