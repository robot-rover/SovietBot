package rr.industries.pageengine;

import com.twitter.Autolink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class MarkdownEngine {
    private static final Autolink linker = new Autolink();
    private static final Logger LOG = LoggerFactory.getLogger(MarkdownEngine.class);
    private MarkdownEngine(){}

    public static String generate(String source) {
        source = escapeHtml4(source.replace("```", "`"));
        boolean isItalic = false;
        boolean isBold = false;
        boolean isCode = false;
        StringBuilder page = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(source.getBytes("UTF-8")));
            int ch;
            while((ch = reader.read()) != -1){
                if((char)ch == '*'){
                    ch = reader.read();
                    StringBuilder tagBuilder;
                    if((char)ch == '*'){
                        if(isBold)
                            tagBuilder = new StringBuilder("</b>");
                        else
                            tagBuilder = new StringBuilder("<b>");
                        isBold = !isBold;
                    } else {
                        if(isItalic)
                            tagBuilder = new StringBuilder("</i>");
                        else
                            tagBuilder = new StringBuilder("<i>");
                        isItalic = !isItalic;
                        if(ch != -1)
                            tagBuilder.append((char)ch);
                    }
                    page.append(tagBuilder.toString());
                } else if((char)ch == '`'){
                    StringBuilder tagBuilder;
                    if(isCode)
                        tagBuilder = new StringBuilder("</code>");
                    else
                        tagBuilder = new StringBuilder("<code>");
                    isCode = !isCode;
                    page.append(tagBuilder.toString());
                } else
                    page.append((char)ch);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Endcoding is Not Supported", e);
            return "";
        } catch (IOException e){
            LOG.error("Exception opening PageEngine template", e);
            return "";
        }
        
        return linker.autoLinkURLs(page.toString());
    }
}
