
package rr.industries.pojos.dictionary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Sense {

    @SerializedName("definition")
    @Expose
    public String definition;
    @SerializedName("examples")
    @Expose
    public List<Example> examples = new ArrayList<Example>();

}
