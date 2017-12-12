
package rr.industries.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CryptoCurrency {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("symbol")
    @Expose
    public String symbol;
    @SerializedName("rank")
    @Expose
    public String rank;
    @SerializedName("price_usd")
    @Expose
    public String priceUsd;
    @SerializedName("price_btc")
    @Expose
    public String priceBtc;
    @SerializedName("24h_volume_usd")
    @Expose
    public String _24hVolumeUsd;
    @SerializedName("market_cap_usd")
    @Expose
    public String marketCapUsd;
    @SerializedName("available_supply")
    @Expose
    public String availableSupply;
    @SerializedName("total_supply")
    @Expose
    public String totalSupply;
    @SerializedName("max_supply")
    @Expose
    public String maxSupply;
    @SerializedName("percent_change_1h")
    @Expose
    public String percentChange1h;
    @SerializedName("percent_change_24h")
    @Expose
    public String percentChange24h;
    @SerializedName("percent_change_7d")
    @Expose
    public String percentChange7d;
    @SerializedName("last_updated")
    @Expose
    public String lastUpdated;

}