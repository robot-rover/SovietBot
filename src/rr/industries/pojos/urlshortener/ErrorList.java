
package rr.industries.pojos.urlshortener;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class ErrorList {

    @SerializedName("errors")
    @Expose
    public List<Error> errors = new ArrayList<Error>();
    @SerializedName("code")
    @Expose
    public Integer code;
    @SerializedName("message")
    @Expose
    public String message;

}
