
package rr.industries.geoCoding;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class GeoCoding {

    @SerializedName("results")
    @Expose
    public List<Result> results = new ArrayList<Result>();
    @SerializedName("status")
    @Expose
    public String status;

}
