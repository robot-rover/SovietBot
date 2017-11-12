package rr.industries;

import rr.industries.pageengine.MarkdownEngine;

public class MarkdownEngineTest {
    public static void main(String[] args){
        System.out.println(MarkdownEngine.generate("*Italics*"));
        System.out.println(MarkdownEngine.generate("**Bold**"));
        System.out.println(MarkdownEngine.generate("```code```"));
        System.out.println(MarkdownEngine.generate("`code`"));
        System.out.println(MarkdownEngine.generate("*Italic* nothing **Bold** ```code **bold** code```"));
    }
}
