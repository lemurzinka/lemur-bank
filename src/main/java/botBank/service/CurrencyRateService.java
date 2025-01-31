package botBank.service;

import botBank.retrievers.RateRetriever;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CurrencyRateService provides methods to retrieve and calculate currency exchange rates.
 * It uses RateRetriever to get the latest rates and offers methods to get specific rates and
 * formatted rate information for different currencies.
 */

@Service
@AllArgsConstructor
public class CurrencyRateService {

    private static final Logger LOGGER = LogManager.getLogger(CurrencyRateService.class);

    private final RateRetriever rateRetriever;


    public Double getRate(String fromCurrency, String toCurrency) {
        LOGGER.info("Getting rate from {} to {}", fromCurrency, toCurrency);
        Map<String, Double> rates = rateRetriever.getRates();
        if (rates == null || !rates.containsKey(fromCurrency) || !rates.containsKey(toCurrency)) {
            LOGGER.warn("Rates not available for {} or {}", fromCurrency, toCurrency);
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

        Double conversionRate;
        if (fromCurrency.equals("USD") && toCurrency.equals("EUR")) {
            conversionRate = eurToUsd; // USD → EUR
        } else if (fromCurrency.equals("EUR") && toCurrency.equals("USD")) {
            conversionRate = usdToEurDirect; // EUR → USD
        } else if (fromCurrency.equals("USD") && toCurrency.equals("UAH")) {
            conversionRate = uahToUsd; // USD → UAH
        } else if (fromCurrency.equals("UAH") && toCurrency.equals("USD")) {
            conversionRate = usdToUah; // UAH → USD
        } else if (fromCurrency.equals("EUR") && toCurrency.equals("UAH")) {
            conversionRate = uahToEur; // EUR → UAH
        } else if (fromCurrency.equals("UAH") && toCurrency.equals("EUR")) {
            conversionRate = eurToUah; // UAH → EUR
        } else {
            Double fromToUsd = 1.0 / fromRate;
            Double toFromUsd = toRate / rates.get("USD");
            conversionRate = fromToUsd / toFromUsd;
        }
        LOGGER.info("Conversion rate from {} to {}: {}", fromCurrency, toCurrency, conversionRate);
        return conversionRate;
    }

    public String getFormattedRates() {
        LOGGER.info("Getting formatted rates");
        Map<String, Double> rates = rateRetriever.getRates();
        if (rates != null) {
            Double usdToEur = rates.get("USD");
            Double eurToUsd = 1.0 / usdToEur;
            Double uahToEur = rates.get("UAH");
            Double uahToUsd = uahToEur * eurToUsd;
            Double usdToEurDirect = 1.0 / eurToUsd;
            Double eurToUah = 1.0 / uahToEur;
            Double usdToUah = 1.0 / uahToUsd;

            String formattedRates = String.format(
                    "Exchange rates:\n" +
                            "EUR → USD: %.2f\n" +
                            "USD → EUR: %.2f\n" +
                            "UAH → EUR: %.2f\n" +
                            "UAH → USD: %.2f\n" +
                            "EUR → UAH: %.2f\n" +
                            "USD → UAH: %.2f",
                    eurToUsd, usdToEurDirect, uahToEur, uahToUsd, eurToUah, usdToUah);
            LOGGER.info("Formatted rates: {}", formattedRates);
            return formattedRates;
        } else {
            LOGGER.error("Rates are not available");
            return "Error.";
        }
    }

}
