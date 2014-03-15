package com.alcshare.proxyconfig.util
import spock.lang.Specification

class AESUtilTest extends Specification {
    def testCycle() {
        when:
        def enc = AESUtil.encrypt("This is a test")

        then:
        "This is a test" == AESUtil.decrypt(enc);
    }
}