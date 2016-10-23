
package rr.industries.pojos.urlshortener;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Error {

    @SerializedName("domain")
    @Expose
    public String domain;
    @SerializedName("reason")
    @Expose
    public String reason;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("locationType")
    @Expose
    public String locationType;
    @SerializedName("location")
    @Expose
    public String location;

    @Override
    public String toString() {
        return new Gson().toJson(this, Error.class);
    }

}
