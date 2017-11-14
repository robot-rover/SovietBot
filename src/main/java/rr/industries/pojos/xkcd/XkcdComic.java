package rr.industries.pojos.xkcd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class XkcdComic {

    @SerializedName("month")
    @Expose
    public String month;
    @SerializedName("num")
    @Expose
    public Integer num;
    @SerializedName("link")
    @Expose
    public String link;
    @SerializedName("year")
    @Expose
    public String year;
    @SerializedName("news")
    @Expose
    public String news;
    @SerializedName("safe_title")
    @Expose
    public String safeTitle;
    @SerializedName("transcript")
    @Expose
    public String transcript;
    @SerializedName("alt")
    @Expose
    public String alt;
    @SerializedName("img")
    @Expose
    public String img;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("day")
    @Expose
    public String day;

}