
package rr.industries.pojos.dictionary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Result {

    @SerializedName("senses")
    @Expose
    public List<Sense> senses = new ArrayList<Sense>();

    @SerializedName("headword")
    @Expose
    public String headword;

}
