
package rr.industries.pojos.xkcd;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class XkcdSearch {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("results")
    @Expose
    public List<Result> results = null;

    public static class Result {

        @SerializedName("number")
        @Expose
        public Integer number;
        @SerializedName("title")
        @Expose
        public String title;
        @SerializedName("titletext")
        @Expose
        public String titletext;
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("image")
        @Expose
        public String image;
        @SerializedName("date")
        @Expose
        public String date;

    }

}
