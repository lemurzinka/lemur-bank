package tests;

import bot_bank.retrievers.RateRetriever;
import bot_bank.service.CurrencyRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * CurrencyRateServiceTest contains unit tests for the CurrencyRateService class. It verifies the correctness
 * of methods related to retrieving and formatting currency exchange rates.
 */

class CurrencyRateServiceTest {

    @Mock
    private RateRetriever rateRetriever;

    private CurrencyRateService currencyRateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currencyRateService = new CurrencyRateService(rateRetriever);
    }

    @Test
    void testGetRate() {
        Map<String, Double> rates = Map.of(
                "USD", 1.18,
                "EUR", 1.0,
                "UAH", 41.84
        );
        when(rateRetriever.getRates()).thenReturn(rates);

        Double rate = currencyRateService.getRate("USD", "EUR");
        assertNotNull(rate);
        assertEquals(1.0 / 1.18, rate);

        rate = currencyRateService.getRate("EUR", "USD");
        assertNotNull(rate);
        assertEquals(1.18, rate);

        rate = currencyRateService.getRate("USD", "UAH");
        assertNotNull(rate);
        assertEquals(41.84 * (1.0 / 1.18), rate);

        rate = currencyRateService.getRate("UAH", "USD");
        assertNotNull(rate);
        assertEquals(1.0 / (41.84 * (1.0 / 1.18)), rate);

        rate = currencyRateService.getRate("EUR", "UAH");
        assertNotNull(rate);
        assertEquals(41.84, rate);

        rate = currencyRateService.getRate("UAH", "EUR");
        assertNotNull(rate);
        assertEquals(1.0 / 41.84, rate);

        // Test invalid currencies
        rate = currencyRateService.getRate("USD", "GBP");
        assertNull(rate);

        rate = currencyRateService.getRate("GBP", "USD");
        assertNull(rate);
    }

    @Test
    void testGetFormattedRates() {
        Map<String, Double> rates = Map.of(
                "USD", 1.18,
                "EUR", 1.0,
                "UAH", 41.84
        );
        when(rateRetriever.getRates()).thenReturn(rates);

        String formattedRates = currencyRateService.getFormattedRates();
        assertNotNull(formattedRates);
        assertTrue(formattedRates.contains("EUR → USD: 0,85")); // 1 / 1.18
        assertTrue(formattedRates.contains("USD → EUR: 1,18")); // 1 * 1.18
        assertTrue(formattedRates.contains("UAH → EUR: 41,84")); // 41.84 * 1
        assertTrue(formattedRates.contains("UAH → USD: 35,46")); // 41.84 * (1 / 1.18)
        assertTrue(formattedRates.contains("EUR → UAH: 0,02")); //1 / 41.84
        assertTrue(formattedRates.contains("USD → UAH: 0,03")); // (1 / 41.84) * 1.18
    }

    @Test
    void testGetFormattedRatesWhenRatesAreNotAvailable() {
        when(rateRetriever.getRates()).thenReturn(null);

        String formattedRates = currencyRateService.getFormattedRates();
        assertNotNull(formattedRates);
        assertEquals("Error.", formattedRates);
    }
}
