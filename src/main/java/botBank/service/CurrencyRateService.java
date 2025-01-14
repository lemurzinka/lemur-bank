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


    public Double getRate(String fromCurrency, String toCurrency) {
        Map<String, Double> rates = rateRetriever.getRates();
        if (rates == null || !rates.containsKey(fromCurrency) || !rates.containsKey(toCurrency)) {
            return null;
        }

        Double fromRate = rates.get(fromCurrency);
        Double toRate = rates.get(toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        Double usdToEur = rates.get("USD");
        Double eurToUsd = 1.0 / usdToEur;
        Double uahToEur = rates.get("UAH");
        Double uahToUsd = uahToEur * eurToUsd;
        Double usdToEurDirect = 1.0 / eurToUsd;
        Double eurToUah = 1.0 / uahToEur;
        Double usdToUah = 1.0 / uahToUsd;


        if (fromCurrency.equals("USD") && toCurrency.equals("EUR")) {
            return eurToUsd; // USD → EUR

        } else if (fromCurrency.equals("EUR") && toCurrency.equals("USD")) {
            return usdToEurDirect; // EUR → USD

        } else if (fromCurrency.equals("USD") && toCurrency.equals("UAH")) {
            return uahToUsd; // USD → UAH

        } else if (fromCurrency.equals("UAH") && toCurrency.equals("USD")) {
            return usdToUah; // UAH → USD

        } else if (fromCurrency.equals("EUR") && toCurrency.equals("UAH")) {
            return uahToEur; // EUR → UAH

        } else if (fromCurrency.equals("UAH") && toCurrency.equals("EUR")) {
            return eurToUah; // UAH → EUR
        } else {

            Double fromToUsd = 1.0 / fromRate;
            Double toFromUsd = toRate / rates.get("USD");
            return fromToUsd / toFromUsd;
        }
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
            return "Error.";
        }
    }
}
