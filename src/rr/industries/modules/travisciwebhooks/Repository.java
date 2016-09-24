
package rr.industries.modules.travisciwebhooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Repository {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("owner_name")
    @Expose
    public String ownerName;
    @SerializedName("url")
    @Expose
    public String url;

}
