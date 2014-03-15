package com.alcshare.proxyconfig.util;

import spock.lang.Specification;

/**
 *
 */
public class JavaVersionTest extends Specification
{
    def "test all string values"() {
        when:
            def ver = new JavaVersion("1.2.3_10-ea")
        then:
            ver.stringMajor == "1"
            ver.stringMinor == "2"
            ver.stringTertiary == "3"
            ver.stringUpdate == "10"
            ver.identifier == "ea"
    }

    def "test string missing optional"() {
        when:
            def ver = new JavaVersion("1.20.30")
        then:
        ver.stringMajor == "1"
        ver.stringMinor == "20"
        ver.stringTertiary == "30"
        ver.stringUpdate == null
        ver.identifier == null
    }

    def "test isAtLeast"() {
        expect:
            new JavaVersion(a).isAtLeast(new JavaVersion(b))

        where:
        a          |     b
        "1.2.3"    | "1.2.3"
        "2.0.0"    | "1.2.3"
        "1.3.0"    | "1.2.3"
        "1.2.4"    | "1.2.3"
        "1.2.3_10" | "1.2.3"
        "1.2.3_10" | "1.2.3_9"
        "1.2.3_10" | "1.2.3_10"
    }
}
