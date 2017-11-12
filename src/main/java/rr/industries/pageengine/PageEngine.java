package rr.industries.pageengine;

import java.util.HashMap;
import java.util.Map;

public class PageEngine {
    private Map<String, String> environmentTags;
    private String defaultURL;
    public PageEngine(){
        environmentTags = new HashMap<>();
        defaultURL = null;
    }

    public PageEngine addEnvTag(String tag, String data){
        environmentTags.put(tag, data);
        return this;
    }

    public PageEngine setDefaultURL(String url){
        defaultURL = url;
        return this;
    }

    public PageEngine removeEnvTag(String tag){
        environmentTags.remove(tag);
        return this;
    }

    public Page newPage(String url){
        return new Page(url, environmentTags);
    }

    public Page newPage(){
        if(defaultURL == null)
            throw new NullPointerException("Cannot create new Page with Default URL when Default URL is null");
        return new Page(defaultURL, environmentTags);
    }
}
