package com.rackspace.cloudfeeds.repose;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * These tests verify that the correct response & status code are written to the ServletResponse object based
 * on the URI, tenant Id, and response coming from the origin service.
 */
public class TenantFilterTest {

    static final String typeAtom = "application/atom+xml; type=entry;charset=utf-8 ";
    static final String typeJson = "application/json";
    static final String entriesURI = "functest1/events/1234/entries/urn:uuid:c36318b8-5d03-6985-d379-fc5a9df6f4b0";

    @Ignore
    @Test
    public void entriesUri200CodeJson() throws IOException, ServletException {

        String content = "{ 'json': 'content'}";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 200, entriesURI, writer, typeJson );

        // status code is never reset
        verify( servletResponse, never() ).setStatus( anyInt() );

        // valid content is set on the response
        assertEquals( content, writer.toString() );
    }

    @Ignore
    @Test
    public void entriesUri200CodeValidXml() throws IOException, ServletException {

        String content = "<atom:entry>" +
              "<atom:id>urn:uuid:c36318b8-5d03-6985-d379-fc5a9df6f4b0</atom:id>" +
              "<atom:category term=\"tid:1234\" />" +
              "<atom:category term=\"rgn:DFW\" />" +
              "<atom:category term=\"dc:DFW1\" />" +
              "<atom:category term=\"rid:256638\" />" +
              "<atom:category term=\"cloudsites.subscription.sites_subscription.usage_snapshot\" />" +
              "<atom:author>" +
              "<atom:name>Feed Service Test Team</atom:name>" +
              "</atom:author>" +
              "<atom:content type=\"application/xml\">" +
              "<event dataCenter=\"DFW1\" environment=\"PROD\" eventTime=\"2012-03-12T11:51:11Z\" " +
              "id=\"c36318b8-5d03-6985-d379-fc5a9df6f4b0\" region=\"DFW\" resourceId=\"256638\" " +
              "resourceName=\"Mosso Standard Offering\" tenantId=\"1234\" type=\"USAGE_SNAPSHOT\" version=\"1\">" +
              "<sites:product action=\"SUBSCRIBE\" isNewAccount=\"false\" resourceType=\"SITES_SUBSCRIPTION\" " +
              "serviceCode=\"CloudSites\" version=\"1\" />" +
              "</event>" +
              "</atom:content>" +
              "<atom:link href=\"https://atom.test.ord1.us.ci.rackspace.net/functest1/events/entries/urn:uuid:c36318b8-5d03-6985-d379-fc5a9df6f4b0\" rel=\"self\" />" +
              "<atom:updated>2014-04-22T14:32:53.974Z</atom:updated>" +
              "<atom:published>2014-04-22T14:32:53.974Z</atom:published>" +
              "</atom:entry>";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 200, entriesURI, writer, typeAtom );

        // status code is never reset
        verify( servletResponse, never() ).setStatus( anyInt() );

        // valid content is set on the response
        assertEquals( content, writer.toString() );
    }

    @Ignore
    @Test
    public void entriesUri200CodeNoTidXml() throws IOException, ServletException {

        String content = "<atom:entry/>";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 200, entriesURI, writer, typeAtom );

        // status code is set to 405
        verify( servletResponse, atMost( 1 ) ).setStatus( 404 );

        // valid content is set on the response
        assertEquals( TenantFilter.getErrorMessage( 404, TenantFilter.notFound ), writer.toString() );
    }

    @Ignore
    @Test
    public void entriesUri200CodeInvalidXML() throws IOException, ServletException {

        String content = "<atom:entry>";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 200, entriesURI, writer, typeAtom );

        verify( servletResponse, atMost( 1 ) ).setStatus( 503 );

        assertEquals( TenantFilter.getErrorMessage( 503, TenantFilter.internalError ), writer.toString() );
    }

    @Ignore
    @Test
    public void notEntriesUri200Code() throws IOException, ServletException {

        final String content = "non-entries";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 200, "/not/entries", writer, typeAtom );

        verify( servletResponse, never() ).setStatus( anyInt() );

        assertEquals( content, writer.toString() );
    }

    @Ignore
    @Test
    public void entriesUri500Code() throws IOException, ServletException {

        final String content = "error message";

        CharArrayWriter writer = new CharArrayWriter();

        HttpServletResponse servletResponse = mockAndRun( content, 500, entriesURI, writer, typeAtom );

        verify( servletResponse, never() ).setStatus( anyInt() );

        assertEquals( content, writer.toString() );
    }

    private HttpServletResponse mockAndRun( final String content, int originStatus, String uri, CharArrayWriter writer,
                                            String contentType )
          throws IOException, ServletException {
        HttpServletRequest servletRequest = mock( HttpServletRequest.class );
        when(servletRequest.getRequestURI()).thenReturn( uri );

        HttpServletResponse servletResponse = mock (HttpServletResponse.class );
        when( servletResponse.getStatus() ).thenReturn( originStatus );
        when( servletResponse.getWriter() ).thenReturn( new PrintWriter( writer ) );
        when( servletResponse.getHeader( "Content-Type" ) ).thenReturn( contentType );

        FilterChain filterChain = mock( FilterChain.class );

        doAnswer( new Answer() {

            public Object answer( InvocationOnMock invocation ) throws IOException {

                ServletResponse resp = (ServletResponse)invocation.getArguments()[ 1 ];

                resp.getWriter().write( content );

                return null;
            } } ).when( filterChain ).doFilter( (ServletRequest) anyObject(), (ServletResponse) anyObject() );

        TenantFilter filter = new TenantFilter();

        filter.doFilter( servletRequest, servletResponse, filterChain );

        return servletResponse;
    }

}
