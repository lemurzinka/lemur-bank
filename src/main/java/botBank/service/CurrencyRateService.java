package botBank.service;

import botBank.retrievers.RateRetriever;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CurrencyRateService {

    private final RateRetriever rateRetriever;

    public CurrencyRateService(RateRetriever rateRetriever) {
        this.rateRetriever = rateRetriever;
    }

    public String getFormattedRates() {
        Map<String, Double> rates = rateRetriever.getRates();
        if (rates != null) {
            Double usdToEur = rates.get("USD");
            Double eurToUsd = 1.0 / usdToEur;
            Double uahToEur = rates.get("UAH");
            Double uahToUsd = uahToEur * eurToUsd;
            Double usdToEurDirect = 1.0 / eurToUsd;
            Double eurToUah = 1.0 / uahToEur;
            Double usdToUah = 1.0 / uahToUsd;

            return String.format(
                    "Курси валют:\n" +
                            "EUR → USD: %.2f\n" +
                            "USD → EUR: %.2f\n" +
                            "UAH → EUR: %.2f\n" +
                            "UAH → USD: %.2f\n" +
                            "EUR → UAH: %.2f\n" +
                            "USD → UAH: %.2f",
                    eurToUsd, usdToEurDirect, uahToEur, uahToUsd, eurToUah, usdToUah);
        } else {
            return "Не вдалося отримати курси валют. Спробуйте пізніше.";
        }
    }
}
