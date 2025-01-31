package botBank.retrievers;


import org.springframework.stereotype.Component;

import java.util.Map;
import org.springframework.web.client.RestTemplate;

/**
 * RateRetriever is a component responsible for retrieving currency rates from an external
 * API. It uses a RestTemplate to send a GET request to the API and returns the rates in a map.
 */

@Component
public class RateRetriever {


    private static final String URL =
            "http://data.fixer.io/api/latest?access_key=1a0c4a508ca7a31399eea86626f6d148";


    public Map<String, Double> getRates() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            RateResponse response = restTemplate.getForObject(URL, RateResponse.class);
            if (response != null && response.isSuccess()) {
                return response.getRates();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
