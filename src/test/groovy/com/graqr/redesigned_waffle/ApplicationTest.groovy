package com.graqr.redesigned_waffle

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class ApplicationTest extends Specification {

    @Inject
    Application application

    void "GetNewApi"() {
        when:
        application.searchTargetStore(true, 'star%20wars')

        then:
        noExceptionThrown()
    }
}
