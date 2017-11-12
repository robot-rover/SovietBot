package rr.industries;

import rr.industries.pageengine.PageEngine;

import java.io.IOException;

public class PageEngineTest {
    public static void main(String[] args) throws IOException {
        PageEngine engine = new PageEngine();
        engine.addEnvTag("css-prefix", "");
        engine.addEnvTag("title", "Global Title");
        String page = engine.newPage("template.txt").setTag("content", "twerk").generate();
        System.out.println(page);
    }
}
