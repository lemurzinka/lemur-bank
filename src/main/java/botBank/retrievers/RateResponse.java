package botBank.retrievers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * RateResponse is a data transfer object (DTO) used to deserialize the response from the
 * currency rate API. It contains fields for the success status and a map of currency rates.
 */

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
