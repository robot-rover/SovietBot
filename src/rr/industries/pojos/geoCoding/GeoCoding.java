
package rr.industries.pojos.geoCoding;

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

    /**
     * Possible Values:
     * "OK" indicates that no errors occurred; the address was successfully parsed and at least one geocode was returned.
     * "ZERO_RESULTS" indicates that the geocode was successful but returned no results. This may occur if the geocoder was passed a non-existent address.
     * "OVER_QUERY_LIMIT" indicates that you are over your quota.
     * "REQUEST_DENIED" indicates that your request was denied.
     * "INVALID_REQUEST" generally indicates that the query (address, components or latlng) is missing.
     * "UNKNOWN_ERROR" indicates that the request could not be processed due to a server error. The request may succeed if you try again.
     */
    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("error_message")
    @Expose
    public String error_message;

}
