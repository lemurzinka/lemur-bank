package botBank.retrievers;


import org.springframework.stereotype.Component;

import java.util.Map;
import org.springframework.web.client.RestTemplate;

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
