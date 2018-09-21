package com.rackspace.feeds.repose;

import org.apache.commons.io.IOUtils;
import org.boon.json.JsonException;
import org.openrepose.commons.utils.io.BufferedServletInputStream;
import org.openrepose.commons.utils.servlet.http.HttpServletRequestWrapper;
import org.openrepose.commons.utils.servlet.http.HttpServletResponseWrapper;
import org.openrepose.commons.utils.servlet.http.ResponseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;


/**
 *
 * Converts incoming JSON payloads into XML.  Uses the following rules:
 *
 * Only operates on POST with the content-type of application/vnd.rackspace.atom+json
 *
 * See conversion rules in Json2Xml.
 */
@Named
public class JsonXmlFilter implements Filter {

    static private Logger LOG = LoggerFactory.getLogger( JsonXmlFilter.class );

    static final String RAX_ATOM_JSON = "application/vnd.rackspace.atom+json";
    static final String CONTENT_TYPE = "Content-Type";
    static final String ATOM_TYPE = "application/atom+xml";
    static final String POST = "POST";
    static final String ERROR_PREFACE = "The following error was encountered after the JSON body was converted to XML: ";
    static final String JSON_ERROR_PREFACE = "Unable to parse invalid JSON: ";

    public void init(FilterConfig filterConfig) throws ServletException {

        LOG.debug( "initialize" );
    }

    public void doFilter( ServletRequest servletRequest,
                          ServletResponse servletResponse, FilterChain filterChain ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String type = httpRequest.getHeader( CONTENT_TYPE );
        String method = httpRequest.getMethod();

        if( isJsonPost( type, method ) ) {

            LOG.debug( "Processing request with Content-type: " + type + "; method: " + method );

            HttpServletResponseWrapper mutableResponse = new HttpServletResponseWrapper((HttpServletResponse) servletResponse, ResponseMode.PASSTHROUGH, ResponseMode.PASSTHROUGH);


            try {

                LOG.debug( "Processing JSON" );

                String xmlOutput = json2Xml( servletRequest.getInputStream() );
                LOG.debug( "XML output: " + xmlOutput );

                // repose-specific stuff
                BufferedServletInputStream istream = new BufferedServletInputStream( IOUtils.toInputStream( xmlOutput ) );
                HttpServletRequestWrapper mutableRequest = new HttpServletRequestWrapper((HttpServletRequest) servletRequest, istream);
                mutableRequest.replaceHeader( CONTENT_TYPE, ATOM_TYPE );

                filterChain.doFilter( mutableRequest, mutableResponse );

                if ( mutableResponse.getStatus() == 400 ) {

                    mutableResponse.sendError( mutableResponse.getStatus(),
                            jsonEscape( ERROR_PREFACE + mutableResponse.getReason() ) );
                }
            } catch ( Json2Xml.JSONException e ) {

                LOG.debug( e.getMessage() );
                mutableResponse.sendError( 400, jsonEscape( JSON_ERROR_PREFACE + e.getMessage() ) );

            } catch ( JsonException e ) {

                LOG.debug( e.getMessage() );
                mutableResponse.sendError( 400, jsonEscape( JSON_ERROR_PREFACE + e.getMessage() ) );

            } catch ( Exception e ) {

                LOG.error( getClass().getName(), e );
                mutableResponse.sendError( 500, jsonEscape( e.getMessage() ) );

            }
        }
        else {

            filterChain.doFilter( servletRequest, servletResponse );
        }
    }

    protected String json2Xml( InputStream stream ) throws Json2Xml.JSONException, XMLStreamException {

        return (new Json2Xml()).json2Xml( stream );
    }

    protected String jsonEscape( String msg ) {

        return msg.replaceAll( "\\n", "\\\\n" );
    }

    private boolean isJsonPost( String type, String method ) {
        return type != null && type.equals(RAX_ATOM_JSON) && method.equals( POST );
    }

    public void destroy() {

    }
}
