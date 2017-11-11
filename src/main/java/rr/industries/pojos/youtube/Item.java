
package rr.industries.pojos.youtube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Item {

    @SerializedName("id")
    @Expose
    public Id id;

    @SerializedName("snippet")
    @Expose
    public Snippet snippet;

}
