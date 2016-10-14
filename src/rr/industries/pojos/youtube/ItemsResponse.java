
package rr.industries.pojos.youtube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ItemsResponse {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("snippet")
    @Expose
    public Snippet snippet;

}
