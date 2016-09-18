
package rr.industries.geoCoding;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Geometry {

    @SerializedName("location")
    @Expose
    public Location location;
    @SerializedName("location_type")
    @Expose
    public String locationType;
    @SerializedName("viewport")
    @Expose
    public Viewport viewport;

}
