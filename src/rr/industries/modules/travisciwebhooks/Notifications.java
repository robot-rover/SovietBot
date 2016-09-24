
package rr.industries.modules.travisciwebhooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Notifications {

    @SerializedName("webhooks")
    @Expose
    public List<String> webhooks = new ArrayList<String>();

}
