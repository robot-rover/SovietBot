
package rr.industries.pojos.dictionary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Tuc {

    @SerializedName("phrase")
    @Expose
    public Phrase phrase;
    @SerializedName("meanings")
    @Expose
    public List<Meaning> meanings = new ArrayList<Meaning>();

}
