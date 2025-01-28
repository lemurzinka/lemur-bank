package botBank.retrievers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RateResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("rates")
    private Map<String, Double> rates;

    public boolean isSuccess() {
        return success;
    }


    public Map<String, Double> getRates() {
        return rates;
    }

}
