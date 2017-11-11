
package rr.industries.pojos.dictionary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class DictionaryResponse {

    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("results")
    @Expose
    public List<Result> results = new ArrayList<Result>();

}
