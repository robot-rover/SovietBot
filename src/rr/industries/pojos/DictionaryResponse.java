
package rr.industries.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class DictionaryResponse {

    @SerializedName("result")
    @Expose
    public String result;
    @SerializedName("tuc")
    @Expose
    public List<Tuc> tuc = new ArrayList<Tuc>();
    @SerializedName("phrase")
    @Expose
    public String phrase;

}
