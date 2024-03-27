package com.graqr.redesigned_waffle;

import com.microsoft.playwright.*;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.util.logging.ErrorManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Value("${goal.url}")
    String url;

    @Value("${goal.path}")
    String path;

    String searchTargetStore(Boolean prehydrateSearch, String searchTerm) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch()) {
                System.getenv().put("PW_EXPERIMENTAL_SERVICE_WORKER_NETWORK_EVENTS", "1");
                try (BrowserContext context = browser.newContext(service_workers='allow')) {
                    final String[] apiUrl = new String[1];
                    context.route(Pattern.compile(".*redsky.*"), route -> apiUrl[0] = route.request().url());
                    try (Page page = browser.newPage()) {
                        page.navigate(String.format("%s/%s" +
                                "prehydrateSearch=%b" +
                                "&SearchTerm=%s", url, path, prehydrateSearch, searchTerm)
                        );
                    }
                    if (null != apiUrl[0]){
                        Matcher matcher = Pattern.compile(".*(?<=\\?key=)[^&]+").matcher(apiUrl[0]);
                        if (matcher.find()){
                            return matcher.group();
                        }else {
                            throw new RuntimeException(String.format("unable to find a key in the provided url: %s", apiUrl[0]));
                        }
                    }else {
                        throw new RuntimeException("unable to find an api key, context route intercepted no api calls to the redsky domain");
                    }
                }
            }
        }
    }
}