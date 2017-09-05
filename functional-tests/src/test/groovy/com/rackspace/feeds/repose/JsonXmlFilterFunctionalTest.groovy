package com.rackspace.feeds.repose

import org.openrepose.framework.test.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain

import static javax.servlet.http.HttpServletResponse.SC_OK

class JsonXmlFilterFunctionalTest extends ReposeValveTest {

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort as Integer, 'origin service')

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs('common', params)
        repose.start()
    }

    def "smoke test"() {
        given:
        def requestHeaders = [someHeader: 'abc']

        when:
        MessageChain mc = deproxy.makeRequest(url: reposeEndpoint, method: 'GET', headers: requestHeaders)
        sleep(20_000)

        then:
        mc.handlings.size() == 1
        mc.receivedResponse.code as Integer == SC_OK
        mc.handlings[0].request.headers.contains('someHeader')
        mc.handlings[0].request.headers.getFirstValue('someHeader') == 'abc'
    }
}
