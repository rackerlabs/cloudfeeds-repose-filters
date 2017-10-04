package com.rackspace.feeds.repose

import org.openrepose.framework.test.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain

import static javax.servlet.http.HttpServletResponse.SC_OK
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE

class JsonXmlFilterFunctionalTest extends ReposeValveTest {

    static final String HTTP_POST = "POST"
    static final String RAX_ATOM_JSON = "application/vnd.rackspace.atom+json"
    static final String ATOM_TYPE = "application/atom+xml"

    static def xmlSlurper = new XmlSlurper()

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort as Integer, 'origin service')

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs('common', params)
        repose.start()
    }

    def "GET request should be successful"() {
        given: "the request contains a custom header"
        def headerName = 'SOME_HEADER'
        def headerValue = 'some value'
        def requestHeaders = [(headerName): headerValue]

        when: "a GET request is made to Repose"
        MessageChain mc = deproxy.makeRequest(url: reposeEndpoint, headers: requestHeaders)

        then: "the client received a good response"
        mc.receivedResponse
        mc.receivedResponse.code as Integer == SC_OK

        and: "the origin service received the request"
        mc.handlings.size() == 1

        and: "the request to the origin service contained the header from the client's request"
        mc.handlings[0].request.headers.contains(headerName)
        mc.handlings[0].request.headers.getFirstValue(headerName) == headerValue
    }

    def "JSON map with single element is treated like a root node"() {
        given: "a JSON body containing a single root element whose value is a Map (no @type nor @text specified anywhere)"
        def jsonBody = """\
{
    "entry" : {
        "title" : "some value"
    }
}
"""

        when: "a POST request is made to Repose"
        MessageChain mc = deproxy.makeRequest(
                url: reposeEndpoint,
                method: HTTP_POST,
                headers: [(CONTENT_TYPE): RAX_ATOM_JSON],
                requestBody: jsonBody)

        then: "the client received a good response"
        mc.receivedResponse
        mc.receivedResponse.code as Integer == SC_OK

        and: "the origin service received the request"
        mc.handlings.size() == 1

        and: "the content-type sent to the origin service was application/atom+xml"
        mc.handlings[0].request.headers.getFirstValue(CONTENT_TYPE) == ATOM_TYPE

        and: "the request body sent to the origin service was valid XML with the root node named 'entry'"
        def entryNode = xmlSlurper.parseText(mc.handlings[0].request.body as String)
        entryNode.name() == "entry"
    }
}
