
package rr.industries.pojos.travisciwebhooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class TravisWebhook {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("number")
    @Expose
    public String number;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("started_at")
    @Expose
    public String startedAt;
    @SerializedName("finished_at")
    @Expose
    public String finishedAt;
    @SerializedName("duration")
    @Expose
    public Integer duration;
    @SerializedName("status_message")
    @Expose
    public String statusMessage;
    @SerializedName("commit")
    @Expose
    public String commit;
    @SerializedName("branch")
    @Expose
    public String branch;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("compare_url")
    @Expose
    public String compareUrl;
    @SerializedName("committed_at")
    @Expose
    public String committedAt;
    @SerializedName("committer_name")
    @Expose
    public String committerName;
    @SerializedName("committer_email")
    @Expose
    public String committerEmail;
    @SerializedName("author_name")
    @Expose
    public String authorName;
    @SerializedName("author_email")
    @Expose
    public String authorEmail;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("build_url")
    @Expose
    public String buildUrl;
    @SerializedName("repository")
    @Expose
    public Repository repository;
    @SerializedName("config")
    @Expose
    public Config config;
    @SerializedName("matrix")
    @Expose
    public List<Matrix> matrix = new ArrayList<Matrix>();

}
