package com.graqr.utils

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

@MicronautTest
class ApiInterceptorSpec extends Specification {

    @Shared
    RestApi apiInterceptor

    @Shared
    String url = "https://www.target.com/b/lego/-/N-56h5n?lnk=snav_rd_legos&redirect=true"

    void setup() {
        apiInterceptor = new RestApi()
    }

    void "SniffRequestURL"() {
        when:
        def apiEndpoints = apiInterceptor.sniffRequestEndpoints(
                URL.newInstance(url),
                domain,
                Duration.ofSeconds(seconds)
        )

        then:
        apiEndpoints.size() != 0
        noExceptionThrown()
        print(apiEndpoints.join("\n"))

        where:
        domain   | seconds | _
        "redsky" | 5       | _
        "redsky" | 10      | _
    }

    void "parseApiKey"() {
        when:
        def apiKey = apiInterceptor.parseApiKey(endpoint)

        then:
        apiKey == "9f36aeafbe60771e321a7cc95a78140772ab3e96"
        noExceptionThrown()

        where:
        endpoint                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | _
        "https://redsky.target.com/redsky_aggregations/v1/web/nearby_stores_v1?limit=5&within=100&place=84096&key=9f36aeafbe60771e321a7cc95a78140772ab3e96&visitor_id=018E92BEEE6C02019823806626F9B948&channel=WEB&page=%2Fb%2F56h5n"                                                                                                                                                                                                                                                                                                        | _
        "https://redsky.target.com/redsky_aggregations/v1/web/plp_search_v2?key=9f36aeafbe60771e321a7cc95a78140772ab3e96&brand_id=56h5n&channel=WEB&count=24&default_purchasability_filter=false&include_dmc_dmr=false&new_search=true&offset=0&page=%2Fb%2F56h5n&platform=desktop&pricing_store_id=3991&scheduled_delivery_store_id=2123&useragent=Mozilla%2F5.0+%28X11%3B+Linux+x86_64%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+HeadlessChrome%2F123.0.0.0+Safari%2F537.36&visitor_id=018E92BED37302018B592D7DEB265E5B&zip=84096" | _
        "https://redsky.target.com/redsky_aggregations/v1/web/general_recommendations_placement_v1?channel=WEB&include_sponsored_recommendations=false&key=9f36aeafbe60771e321a7cc95a78140772ab3e96&page=%2Fb%2F56h5n&placement_id=adapt_primary_top_sellers&pricing_store_id=3991&purchasable_store_ids=&visitor_id=018E92BED37302018B592D7DEB265E5B&brand_ids=56h5n&resolve_to_first_variation_child=false&slingshot_component_id=WEB-391958&platform=desktop&facet_ids=5ewil%2Cj48k8"                                                     | _
        "https://redsky.target.com/redsky_aggregations/v1/web/store_location_v1?store_id=2123&key=9f36aeafbe60771e321a7cc95a78140772ab3e96&visitor_id=018E92BED37302018B592D7DEB265E5B&channel=WEB&page=%2Fb%2F56h5nMar"                                                                                                                                                                                                                                                                                                                     | _
    }
}