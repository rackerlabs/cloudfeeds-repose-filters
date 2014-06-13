package com.rackspace.feeds.repose;

import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletRequest;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is to prevent observers reading an individual tenanted-entry from viewing other tenant's entries.
 *
 * If a response matches the entries URI pattern & is a 200, this filter verifies that the tenant id provided in the
 * tenanted-URI matches the tenant id for the entry.  If not, a 404 is returned.
 *
 * This is required as repose does not currently provide the ability to modify the status code based on a response's
 * content.
 */
public class TenantFilter implements Filter {

    public static final String notFound = "Resource not found.";

    public static final String internalError = "Internal Error: " +  TenantFilter.class.getName() + ": ";

    private static final ObjectPool<XPathExpression> xpathPool = new GenericObjectPool<XPathExpression>( new TidXPathPooledObjectFactory<XPathExpression>() );

    private static final ObjectPool<Matcher> matcherPool = new GenericObjectPool<Matcher>( new TidMatcherPooledObjectFactory<Matcher>() );

    public static String getErrorMessage( int code, String mesg ) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<error xmlns=\"http://abdera.apache.org\">\n" +
              "<code>" + code + "</code>\n" +
              "<message>" + StringEscapeUtils.escapeXml( mesg ) + "</message>\n" +
              "</error>";
    }

    private static final Logger LOG = LoggerFactory.getLogger( TenantFilter.class );

    static final private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    /*
     * Since entries can have arbitrary prefixes which are not bound to namespaces, I'm turning off
     * namespace-aware.
     */
    static {

        try {
            builderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
        }
        catch ( ParserConfigurationException e ) {

            LOG.error( TenantFilter.class.getName(), e );
        }
    }

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
          throws IOException, ServletException {

        //Use the repose internal Wrapper to grab a response and modify it
        MutableHttpServletRequest mutableRequest =
              MutableHttpServletRequest.wrap( (HttpServletRequest) servletRequest );
        mutableRequest.setInputStream( servletRequest.getInputStream() );

        //Use a repose internal mutable response
        MutableHttpServletResponse mutableResponse =
              MutableHttpServletResponse.wrap( (HttpServletRequest) servletRequest,
                                               (HttpServletResponse) servletResponse );

        PrintWriter outWriter = mutableResponse.getWriter();
        CodeContent codeContent = new CodeContent();
        codeContent.setStatusCode( mutableResponse.getStatus() );

        try {

            // get tid before translation rips it out of the request
            Matcher match = matcherPool.borrowObject().reset( mutableRequest.getRequestURI() );
            String tid = match.matches() ? match.group( 1 ) : null;
            matcherPool.returnObject( match );


            //Fire off the next one in the filter chain
            filterChain.doFilter( mutableRequest, mutableResponse );

            //read in the entire content
            codeContent.setContent( new Scanner( mutableResponse.getInputStream() ).useDelimiter( "\\A" ).next() );

            codeContent = getResponse( codeContent, tid,
                                       mutableResponse.getHeader( "Content-Type" ).contains( "application/atom+xml" ) );
        }
        catch ( Exception e ) {

            // if internal error, report as such
            codeContent.setStatusCode( 503 );
            codeContent.setContent( getErrorMessage( 503, internalError + e.getMessage() ) );
            LOG.error( internalError, e );
        }
        finally {

            mutableResponse.setStatus( codeContent.getStatusCode() );
            outWriter.write( codeContent.getContent() );
            mutableResponse.commitBufferToServletOutputStream();
        }
    }

    // making this public so we can easily write unit tests
    public CodeContent getResponse( CodeContent codeContent,
                                    String tid,
                                    boolean isAtomXml ) throws Exception {
        if ( tid != null
              && codeContent.getStatusCode() == 200
              && isAtomXml ) {

            Document doc =
                  builderFactory.newDocumentBuilder().parse( new InputSource( new StringReader( codeContent.getContent() ) ) );

            XPathExpression xpath = xpathPool.borrowObject();

            String contentTid = xpath.evaluate( doc );

            xpathPool.returnObject( xpath );

            // if no match return 404 & insert error message
            if ( !contentTid.equals( "tid:" + tid ) ) {

                codeContent.setStatusCode( 404 );
                codeContent.setContent( getErrorMessage( 404, notFound ) );
            }
        }
        return codeContent;
    }

    @Override
    public void destroy() {
    }

    static class TidMatcherPooledObjectFactory<Matcher> extends BasePooledObjectFactory<Matcher> {

        static final private Pattern patTidEntry = Pattern.compile( ".+/events/([^/?]+)/entries/[^?]+" );

        @Override
        public Matcher create() {

            return (Matcher)patTidEntry.matcher( "" );
        }

        @Override
        public PooledObject<Matcher> wrap( Matcher m ) {

            return new DefaultPooledObject<Matcher>( m );
        }
    }


    static class TidXPathPooledObjectFactory<XPathExpression> extends BasePooledObjectFactory<XPathExpression> {

        static final private XPathFactory xPathfactory = XPathFactory.newInstance();

        @Override
        public XPathExpression create() throws XPathExpressionException {
            synchronized ( this ) {
                return (XPathExpression)xPathfactory.newXPath().compile( "/entry/category[starts-with(@term, \"tid:\")]/@term" );
            }
        }

        @Override
        public PooledObject<XPathExpression> wrap( XPathExpression xpath ) {

            return new DefaultPooledObject<XPathExpression>( xpath );
        }
    }
}
