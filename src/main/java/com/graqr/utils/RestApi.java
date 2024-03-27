package com.graqr.utils;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v120.network.Network;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils class to intercept or parse apis used by a website.
 *
 * @since 0.0.1
 * @author jonathan zollinger
 */
@Singleton
public class RestApi {

    private static final Logger logger = LoggerFactory.getLogger(RestApi.class);
    final WebDriver driver;
    DevTools devtools;

    final WebDriverWait wait;

    @Value("webdriver.wait-seconds")
    int waitSeconds;

    RestApi() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        setDevTools();
        wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    /**
     * configure devtools to intercept api traffic
     */
    private void setDevTools() {
        logger.debug("instantiating devtools");
        devtools = ((HasDevTools) driver).getDevTools();
        devtools.createSession();
        devtools.send(Network.enable(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));
        logger.debug("devtools instantiated with network enabled with empty parameters");
    }

    /**
     * return endpoints for api requests made by a provided website.
     *
     * @param url webpage whose api endpoints are to be returned
     * @param domainToIntercept domain name to use as a filter against returned endpoints.
     * @param timeoutSeconds duration until method returns if no endpoints are found with the provided filter.
     * @return list of matching endpoints, or an empty list if no endpoints found.
     */
    List<String> sniffRequestEndpoints(URL url, @NotNull String domainToIntercept, @NotNull Duration timeoutSeconds) {
        List<String> capturedURLs = new CopyOnWriteArrayList<>();
        devtools.addListener(Network.requestWillBeSent(), req -> capturedURLs.add(req.getRequest().getUrl()));
        driver.get(url.toExternalForm());
        long startTime = System.currentTimeMillis();
        while (true) {
            List<String> currentUrls = new ArrayList<>(capturedURLs);
            Predicate<? super String> uriFilter = uri -> uri.contains(domainToIntercept);
            if (currentUrls.stream().anyMatch(uriFilter)) {
                driver.close();
                return currentUrls.stream().filter(uriFilter).distinct().toList();
            }

            if ((System.currentTimeMillis() - startTime) > timeoutSeconds.toMillis()) {
                driver.close();
                return new ArrayList<>();
            }
            logger.debug(String.format("captured endpoints don't include %s. Captured endpoints: %s",
                    domainToIntercept, String.join(", ", currentUrls)));
        }
    }

    /**
     *
     * Parses the api key used in the provided endpoint.
     *
     * @param endpoint full uri used in an api request whose query params include an api call.
     * @return api key used in provided endpoint. Returns an empty string if no api key is found.
     */
    String parseApiKey(@NotNull String endpoint) {
        Matcher matcher = Pattern.compile("[?|&]key=([^&]*+)").matcher(endpoint);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}