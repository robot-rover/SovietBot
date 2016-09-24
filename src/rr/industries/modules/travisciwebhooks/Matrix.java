
package rr.industries.modules.travisciwebhooks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Matrix {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("repository_id")
    @Expose
    public Integer repositoryId;
    @SerializedName("number")
    @Expose
    public String number;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("started_at")
    @Expose
    public String startedAt;
    @SerializedName("finished_at")
    @Expose
    public String finishedAt;
    @SerializedName("config")
    @Expose
    public Config config;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("log")
    @Expose
    public String log;
    @SerializedName("result")
    @Expose
    public Integer result;
    @SerializedName("parent_id")
    @Expose
    public Integer parentId;
    @SerializedName("commit")
    @Expose
    public String commit;
    @SerializedName("branch")
    @Expose
    public String branch;
    @SerializedName("message")
    @Expose
    public String message;
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
    @SerializedName("compare_url")
    @Expose
    public String compareUrl;

}
