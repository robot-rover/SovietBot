package rr.industries.pageengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Page {
    private static Logger LOG = LoggerFactory.getLogger(Page.class);
    String url;
    Map<String, String> envTags;
    Map<String, String> localTags;
    protected Page(String url, Map<String, String> envTags){
        this.url = url;
        this.envTags = envTags;
        localTags = new HashMap<>();
    }

    public Page setTag(String tag, String data){
        localTags.put(tag, data);
        return this;
    }

    public String generate() {
        StringBuilder page = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(url), "UTF-8");
            int ch;
            while((ch = reader.read()) != -1){
                if((char)ch == '{'){
                    StringBuilder tagBuilder = new StringBuilder();
                    while((ch = reader.read()) != -1){
                        if((char)ch == '}')
                            break;
                        tagBuilder.append((char)ch);
                    }
                    String tag = tagBuilder.toString();
                    String tagData = localTags.get(tag);
                    if(tagData == null)
                        tagData = envTags.get(tag);
                    if(tagData == null)
                        LOG.warn("Unknown Tag '{}' Parsed", tag);
                    else {
                        page.append(tagData);
                    }

                }
                else page.append((char)ch);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Endcoding is Not Supported", e);
            return "";
        } catch (IOException e){
            LOG.error("Exception opening PageEngine template", e);
            return "";
        }
        return page.toString();
    }
}
