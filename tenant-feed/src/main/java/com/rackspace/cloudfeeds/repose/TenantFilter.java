package com.rackspace.cloudfeeds.repose;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletResponseWrapper;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter is to prevent observers reading an individual tenanted-entry from viewing other tenant's entries.
 *
 * If a response matches the entries URI pattern & is a 200, this filter verifies that the tenant id provided in the
 * tenanted-URI matches the tenant id for the entry.  If not, a 405 is returned.
 *
 * This is required as repose does not currently provide the ability to modify the status code based on a response's
 * content.
 */
public class TenantFilter implements Filter {

    public static final String notFound = "Resource no found.";

    public static final String internalError = "Internal Error: " + TenantFilter.class.getName();

    public static String getErrorMessage( int code, String mesg ) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<error xmlns=\"http://abdera.apache.org\">\n" +
              "<code>" + code + "</code>\n" +
              "<message>" + StringEscapeUtils.escapeXml( mesg ) + "</message>\n" +
              "</error>";
    }

    private static final Logger LOG = LoggerFactory.getLogger( TenantFilter.class );

    // TODO: should we pool matcher instances instead?
    static final private Pattern patTidEntry = Pattern.compile( ".+/events/([^/?]+)/entries/[^?]+" );

    static final private XPathFactory xPathfactory = XPathFactory.newInstance();

    static final private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    /*
     * Since entries can have arbitrary prefixes which are not bound to namespaces, I'm turning off
     * namespace-aware.
     */
    static {
                 /* TODO:  removing to make running in tomcat easy
        try {
            builderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
        }
        catch ( ParserConfigurationException e ) {

            LOG.error( TenantFilter.class.getName(), e );
        }
        */
    }

    @Override
    public void init( FilterConfig filterConfigP ) throws ServletException {

        System.out.println( "Start " + TenantFilter.class.getName() );
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
          throws IOException, ServletException {

        ResponseWrapper respWrap = new ResponseWrapper( (HttpServletResponse) servletResponse );

        // get tid before translation rips it out of the request
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        Matcher match = patTidEntry.matcher( req.getRequestURI() );
        String tid = match.matches() ? match.group( 1 ) : null;

        filterChain.doFilter( servletRequest, respWrap );

        // TODO
        LOG.info( "TID: " + tid );
        LOG.info( "URI: " + req.getRequestURI() );
        //LOG.info( "Content: " + respWrap.getContentType() );
        LOG.info( "Status: " + respWrap.getStatus() );
        LOG.info( "resp Header 'Content-Type: " + respWrap.getHeader( "Content-Type" ) );

        String content = respWrap.getContent();

        System.out.println( "Content Body: " + content );
        LOG.info( "Content Body: " + content );

        PrintWriter outWriter = servletResponse.getWriter();

        if( tid != null
              && respWrap.getStatus() == 200
              && respWrap.getHeader( "Content-Type" ).contains( "application/atom+xml" ) ) {


            try {

                // TODO: should I pool:
                // - xpathexpression

                XPathExpression xpath = xPathfactory.newXPath().compile(
                    "/entry/category[@term='tid:" + tid + "']/@term" );

                Document doc = builderFactory.newDocumentBuilder().parse( new InputSource( new StringReader( respWrap.getContent() ) ) );

                // if no match return 404 & insert error message
                if ( xpath.evaluate( doc ).isEmpty() ) {

                    // TODO
                    LOG.info( "404" );

                    respWrap.setStatus( 404 );
                    outWriter.write( getErrorMessage( 404, notFound ) );

                    return;
                }
            }
            catch ( Exception e ) {

                // if internal error, report as such
                respWrap.setStatus( 503 );

                outWriter.write( getErrorMessage( 503, internalError ) );

                LOG.error( internalError, e );

                return;
            }
        }

        // else, write out received response & keep status code
        outWriter.write( content );
    }

    @Override
    public void destroy() {
    }

    private class FilterServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream stream;

        public FilterServletOutputStream( ByteArrayOutputStream streamP ) {
            stream = streamP;
        }

        @Override
        public void write(int b) throws IOException  {
            stream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException  {
            stream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException  {
            stream.write(b,off,len);
        }
    }

    private class ResponseWrapper extends HttpServletResponseWrapper {

        private ByteArrayOutputStream stream = new ByteArrayOutputStream();
        private PrintWriter writer = new PrintWriter( stream );
        private ServletOutputStream soStream = new FilterServletOutputStream( stream );


        public String getContent() {
            try {
                stream.flush();
                stream.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return stream.toString();
        }

        public ResponseWrapper( HttpServletResponse resp ) {
            super( resp );
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {

            return soStream;
        }

        @Override
        public PrintWriter getWriter() {

            return writer;
        }
    }
}
