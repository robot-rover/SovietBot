package rr.industries.pojos.urlshortener;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Sam
 */
public class URLResponse {
    public URLResponse(String url) {
        longUrl = url;
    }

    public String status;
    public String kind;
    public String id;
    public String longUrl;
    @SerializedName("error")
    @Expose
    public ErrorList error;

}
