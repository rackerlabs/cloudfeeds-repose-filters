package com.rackspace.feeds.repose;

import com.rackspace.papi.commons.util.io.BufferedServletInputStream;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletRequest;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.collections.BidiMap;
import org.boon.json.JsonException;
import org.boon.json.JsonParserAndMapper;
import org.boon.json.JsonParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Converts incoming JSON payloads into XML.  Uses the following rules:
 *
 * Only operates on POST with the content-type of application/vnd.rackspace.atom+json
 *
 * Conversion from JSON to XML rules:
 * <ul>
 *   <li> All Maps in JSON are treated as XML nodes
 *   <li> All arrays are treated as successive XML nodes of the same type.
 *   <li> The @text key signifies that the value is a text node of the xml node.
 *   <li> The @type key signifies the XML namespace of the xml node.
 *   <li> All key-value pairs are treated as attributes, unless under the http://www.w3.org/2005/Atom namespace.
 *   <li> For nodes & attributes in the http://www.w3.org/2005/Atom namespace, all key-value pairs are treated as XML
 *        nodes and their text values.  The one exception to this is when a map contains a @text node, then all other
 *        key-value pairs in that map are treated as attributes.  Any key-map or key-list pairs in this map are treated
 *        as XML nodes.
 * </ul>
 *
 */
public class JsonXmlFilter implements Filter {

    static private Logger LOG = LoggerFactory.getLogger( JsonXmlFilter.class );
    static private JsonParserFactory JSON_FACTORY;
    static private XMLOutputFactory XML_FACTORY = XMLOutputFactory.newInstance();
    static final String TYPE = "@type";
    static final String TEXT = "@text";
    static final String NS = "ns";
    static final String ATOM_JSON = "application/vnd.rackspace.atom+json";
    static final String CONTENT_TYPE = "Content-Type";
    static final String ATOM_TYPE = "application/atom+xml";
    static final String POST = "POST";
    static final String ATOM_NS = "http://www.w3.org/2005/Atom";
    static final String ERROR_PREFACE = "The following error was encountered after the JSON body was converted to XML: ";
    static final String JSON_ERROR_PREFACE = "Unable to parse invalid JSON: ";

    static {

        JSON_FACTORY = new JsonParserFactory();
        JSON_FACTORY.setCheckDates( false );
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        LOG.debug( "initialize" );
    }

    @Override
    public void doFilter( ServletRequest servletRequest,
                          ServletResponse servletResponse, FilterChain filterChain ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String type = httpRequest.getHeader( CONTENT_TYPE );
        String method = httpRequest.getMethod();

        if( isJsonPost( type, method ) ) {

            LOG.debug( "Processing request with Content-type: " + type + "; method: " + method );

            MutableHttpServletResponse mutableResponse =
                    MutableHttpServletResponse.wrap( (HttpServletRequest) servletRequest,
                            (HttpServletResponse) servletResponse );

            MutableHttpServletRequest mutableRequest = MutableHttpServletRequest.wrap( (HttpServletRequest) servletRequest );

            try {

                LOG.debug( "Processing JSON" );

                String xmlOutput = getJSON( servletRequest.getInputStream() );

                LOG.debug( "XML output: " + xmlOutput );

                // repose-specific stuff
                BufferedServletInputStream istream = new BufferedServletInputStream( IOUtils.toInputStream( xmlOutput ) );
                mutableRequest.setInputStream( istream );
                mutableRequest.replaceHeader( CONTENT_TYPE, ATOM_TYPE );

                mutableRequest.replaceHeader( "Accept", ATOM_TYPE );

                filterChain.doFilter( mutableRequest, mutableResponse );

                if ( mutableResponse.getStatus() == 400 ) {

                    mutableResponse.sendError( mutableResponse.getStatus(),
                            jsonEscape( ERROR_PREFACE + mutableResponse.getMessage() ) );
                }
            } catch ( JSONException e ) {

                LOG.debug( e.getMessage() );
                mutableResponse.sendError( 400, jsonEscape( JSON_ERROR_PREFACE + e.getMessage() ) );

            } catch ( JsonException e ) {

                LOG.debug( e.getMessage() );
                mutableResponse.sendError( 400, jsonEscape( JSON_ERROR_PREFACE + e.getMessage() ) );

            } catch ( Exception e ) {

                LOG.error( getClass().getName(), e );
                mutableResponse.sendError( 500, jsonEscape( e.getMessage() ) );

            } finally {

                mutableRequest.replaceHeader( "Accept", ATOM_JSON );
                mutableResponse.setContentType( ATOM_JSON );
            }
        }
        else {

            filterChain.doFilter( servletRequest, servletResponse );
        }
    }

    protected String jsonEscape( String msg ) {

        return msg.replaceAll( "\\n", "\\\\n" );
    }

    private boolean isJsonPost( String type, String method ) {
        return type != null && type.equals( ATOM_JSON ) && method.equals( POST );
    }

    protected String getJSON( InputStream istream ) throws JSONException, JsonException, XMLStreamException {

        JsonParserAndMapper parser = JSON_FACTORY.createFastParser();

        Map<String, Object> map = parser.parseMap( istream );


        if ( map.keySet().size() != 1 ) {

           throw new JSONException( "JSON message must have single root object" );
        }

        StringWriter writer = new StringWriter();

        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter( XML_FACTORY.createXMLStreamWriter( writer ) );

        xmlWriter.writeStartDocument( );

        String key = map.keySet().iterator().next();
        writeNode( key, map.get( key ), xmlWriter, 0, new DualHashBidiMap(), null );
        xmlWriter.close();

        return writer.toString();
    }

    @Override
    public void destroy() {

    }

    /**
     * Recursively turns a Map object of a JSON file into an XML string, using the rules listed at the top
     * of this class.
     *
     * @param key - the JSON key which is being inspected
     * @param value - the JSON value which corresponds to the key
     * @param xmlWriter - writes the XML
     * @param prefixIntP - the next free integer, in case a namespace prefix needs to be declared
     * @param nsPrefixMapP - a map of all declared namespaces in this scope and their prefixes
     * @param prefixP - prefix which corresponds to the namespace in the current scope
     *
     * @return the next free integer for namespace prefixes (prefixIntP)
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private int writeNode( String key,
                           Object value,
                           XMLStreamWriter xmlWriter,
                           int prefixIntP,
                           BidiMap nsPrefixMapP,
                           String prefixP ) throws XMLStreamException, JSONException {

        BidiMap nsPrefixMap = new DualHashBidiMap( nsPrefixMapP );

        int prefixInt = prefixIntP;
        String prefix = prefixP;

        if( value instanceof Map ) {
            //
            // this is a node
            //

            Map<String, Object> map = (Map<String, Object>)value;

            // find namespace
            Object ons = map.get( TYPE );

            if( ons == null ) {

                writeStartElement( key, xmlWriter, nsPrefixMap, prefix );
            }
            else if ( isCollection( ons ) ) {

                throw new JSONException( key + "/@type attribute needs to be a valid namespace.  Value cannot be converted into a string value."  );
            }
            else {

                String ns = ons.toString();

                prefix = (String)nsPrefixMap.get( ns );

                if( prefix == null ) {

                    prefix = NS + prefixInt++;
                    nsPrefixMap.put( ns, prefix );
                    xmlWriter.writeStartElement( prefix, key, ns );
                    xmlWriter.writeNamespace( prefix, ns );
                }
                else {
                    xmlWriter.writeStartElement( prefix, key, ns );
                }
            }

            addContentType( key, xmlWriter );

            // write all attributes first
            // then write all array's and nodes
            Map<String, Object> mapElem = new HashMap<String, Object>();

            for( String child : map.keySet() ) {

                Object childValue = map.get( child );

                if( isCollection( childValue ) || child.equals( TEXT ) ) {

                    mapElem.put( child, childValue );
                }
                else if( !child.equals( TYPE ) ) {

                    // don't default to atom shorthand, do as attribute
                    if( isAtomShortHand( prefix, nsPrefixMap ) && map.containsKey( TEXT ) ) {

                        xmlWriter.writeAttribute( child, (String)map.get( child ) );
                    }
                    else
                        prefixInt = writeNode( child, map.get( child ), xmlWriter, prefixInt, nsPrefixMap, prefix );
                }
            }

            for( String child : mapElem.keySet() ) {

                if( child.equals( TEXT ) ) {

                    xmlWriter.writeCharacters(  (String)mapElem.get( child ) );
                }
                else {

                    prefixInt = writeNode( child, map.get( child ), xmlWriter, prefixInt, nsPrefixMap, prefix );
                }
            }

            xmlWriter.writeEndElement();
        }
        else if ( value instanceof List ) {

            //
            // this is an array
            //

            List<Map> list = (List<Map>)value;

            for( Map elem : list ) {

                prefixInt = writeNode( key, elem, xmlWriter, prefixInt, nsPrefixMap, prefix );
            }
        }
        else if( isAtomShortHand( prefix, nsPrefixMap ) ) {

            //
            // this is a node, because its in atom namespace
            //
            writeStartElement( key, xmlWriter, nsPrefixMap, prefix );

            addContentType( key, xmlWriter );

            xmlWriter.writeCharacters( value.toString() );
            xmlWriter.writeEndElement();
        }
        else {
            //
            // this is an attribute
            //
            xmlWriter.writeAttribute( key, value.toString() );
        }

        return prefixInt;
    }

    private void addContentType( String key, XMLStreamWriter xmlWriter ) throws XMLStreamException {
        if( key.equals( "content" ) ) {
            xmlWriter.writeAttribute( "type", "application/xml" );
        }
    }

    private boolean isAtomShortHand( String prefix, BidiMap nsPrefixMap ) {
        return ATOM_NS.equals( nsPrefixMap.inverseBidiMap().get( prefix ) );
    }

    private void writeStartElement( String key, XMLStreamWriter xmlWriter, BidiMap nsPrefixMap, String prefix ) throws XMLStreamException {
        if( prefix == null )
            xmlWriter.writeStartElement( key );
        else {
            xmlWriter.writeStartElement( prefix, key, (String)nsPrefixMap.inverseBidiMap().get( prefix ) );
        }
    }
    private boolean isCollection( Object o ) {

        return o instanceof Map || o instanceof List;
    }

    static public class JSONException extends Exception {

        JSONException( String msg ) {
            super( msg );
        }
    }
}
