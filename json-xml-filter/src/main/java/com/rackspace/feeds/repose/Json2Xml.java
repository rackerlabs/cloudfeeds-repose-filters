package com.rackspace.feeds.repose;

import javanet.staxutils.IndentingXMLStreamWriter;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.boon.json.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Conversion from JSON to XML rules:
 * <ul>
 *   <li> All Maps in JSON are treated as XML nodes
 *   <li> All arrays are treated as successive XML nodes of the same type.
 *   <li> The @text key signifies that the value is a text node of the xml node.
 *   <li> The @type key signifies the XML namespace of the xml node.  For event nodes, in
 *        the http://schemas.dmtf.org/cloud/audit/1.0/event namespace, the namespace is read from the typeURI attribute.
 *   <li> All key-value pairs are treated as attributes, unless under the http://www.w3.org/2005/Atom namespace.
 *   <li> For nodes &amp; attributes in the http://www.w3.org/2005/Atom namespace, all key-value pairs are treated as XML
 *        nodes and their text values.  The one exception to this is when a map contains a @text node, then all other
 *        key-value pairs in that map are treated as attributes.  Any key-map or key-list pairs in this map are treated
 *        as XML nodes.
 *   <li> Arbitrary JSON can be submitted by setting type="application/json" in the content node and including the JSON
 *        as the value of a @text key.
 *   <li> attachments nodes in the http://schemas.dmtf.org/cloud/audit/1.0/event namespace &amp; its children are
 *        handled in a custom manner.
 * </ul>
 *
 */
public class Json2Xml {

    static private JsonParserFactory JSON_FACTORY;
    static private JsonSerializerFactory JSON_SERIALIZER_FACTORY;

    static {
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl");
        JSON_FACTORY = new JsonParserFactory();
        JSON_FACTORY.setCheckDates( false );
        JSON_SERIALIZER_FACTORY = new JsonSerializerFactory();
    }
    static private XMLOutputFactory XML_FACTORY = XMLOutputFactory.newFactory();

    static final String TYPE = "@type";
    static final String TEXT = "@text";
    static final String NS = "ns";
    static final String JSON_CTYPE = "application/json";
    static final String XML_CTYPE = "application/xml";

    static final String ATOM_NS = "http://www.w3.org/2005/Atom";
    static final String TYPE_ATTR = "type";
    static final String CATEGORY = "category";
    static final String LINK = "link";
    static final String EVENT = "event";
    static final String TYPE_URI = "typeURI";
    static final String CADF_NS = "http://schemas.dmtf.org/cloud/audit/1.0/event";
    static final String ATTACHMENT = "attachment";
    static final String ATTACHMENTS = "attachments";
    static final String CONTENT_TYPE_CADF = "contentType";
    static final String CONTENT = "content";
    static final String VERSION = "version";

    private int prefixInt = 0;

    public String json2Xml( InputStream istream ) throws JSONException, JsonException, XMLStreamException {

        JsonParserAndMapper parser = JSON_FACTORY.createFastParser();

        Map<String, Object> map = parser.parseMap( istream );


        if ( map.keySet().size() != 1 ) {

            throw new JSONException( "JSON message must have single root object" );
        }

        StringWriter writer = new StringWriter();

        XML_FACTORY.setProperty("escapeCharacters", true);
        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter( XML_FACTORY.createXMLStreamWriter( writer ) );

        xmlWriter.writeStartDocument( );

        String key = map.keySet().iterator().next();
        writeNode( key, map.get( key ), xmlWriter, new NameSpaceMaps(), null );
        xmlWriter.close();

        return writer.toString();
    }

    /**
     * Recursively turns a Map object of a JSON file into an XML string, using the rules listed at the top
     * of this class.
     *
     * @param key - the JSON key which is being inspected
     * @param value - the JSON value which corresponds to the key
     * @param xmlWriter - writes the XML
     * @param nsMapP - a map of all declared namespaces in this scope and their prefixes
     * @param prefixP - prefix which corresponds to the namespace in the current scope
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private void writeNode( String key,
                           Object value,
                           XMLStreamWriter xmlWriter,
                           NameSpaceMaps nsMapP,
                           String prefixP ) throws XMLStreamException, JSONException {

        String prefix = prefixP;

        NameSpaceMaps nsMap = updateNameSpaceMap( key, nsMapP, prefix );

        if( value instanceof Map ) {
            //
            // this is a node
            //

            Map<String, Object> map = (Map<String, Object>)value;
            prefix = writeStartElementAndNamespace( key, xmlWriter, prefix, nsMap, map );

            String prefixContentType = checkAndWriteCadfContentType( key, xmlWriter, prefix, nsMap, map );

            if( checkAndWriteAtomJsonContent( key, xmlWriter, prefixP, nsMap, map ) )
                return;

            // write all attributes first
            // then write all array's and nodes
            Map<String, Object> mapElem = writeAttributes( xmlWriter, prefix, nsMap, map, prefixContentType );
            writeNodesAndEndElement( xmlWriter, prefix, nsMap, map, mapElem );
        }
        else if ( value instanceof List ) {

            //
            // this is an array
            //
            writeListElements( key, (List<Map>) value, xmlWriter, prefixP, prefix, nsMap );
        }
        else if( isAtomShortHand( prefix, nsMap ) || nsMap.isInCadfAttachCustom() ) {

            //
            // this is a node, because its in atom namespace
            //
            writeStartElement( key, xmlWriter, nsMap, prefix );

            xmlWriter.writeCharacters( value.toString() );
            xmlWriter.writeEndElement();
        }
        else {
            //
            // this is an attribute
            //
            xmlWriter.writeAttribute( key, value.toString() );
        }
    }

    /**
     * Write JSON list as XML elements.  Handles the following special cases
     *
     * - Atom link & category nodes
     * - CADF attachments nodes
     *
     * @param key the array's JSON key
     * @param list the array of values
     * @param xmlWriter
     * @param prefixParent - the parent node's prefix
     * @param prefix - the array's key prefix
     * @param nsMap - the map of all namspaces
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private void writeListElements( String key,
                                    List<Map> list,
                                    XMLStreamWriter xmlWriter,
                                    String prefixParent,
                                    String prefix,
                                    NameSpaceMaps nsMap ) throws XMLStreamException, JSONException {


        // handle category/link differently
        if ( isAtomShortHand( prefixParent, nsMap ) && (key.equals(CATEGORY) || key.equals(LINK)) ) {
            for ( Map elem: list ) {
                xmlWriter.writeEmptyElement(prefix, key, ATOM_NS);
                Set<String> keys = elem.keySet();
                for ( String attrName : keys ) {
                    xmlWriter.writeAttribute(attrName, (String)elem.get(attrName));
                }
            }
        }
        // CADF attachments require each element to have its own attachment node
        else if( key.equals( ATTACHMENTS ) && isCadf( prefixParent, nsMap ) ) {

            xmlWriter.writeStartElement( prefix, key, CADF_NS );

            for( Map elem : list ) {

                writeNode( ATTACHMENT, elem, xmlWriter, nsMap, prefix );
            }

            xmlWriter.writeEndElement();
        }
        else {
            for( Map elem : list ) {
                writeNode( key, elem, xmlWriter, nsMap, prefix );
            }
        }
    }

    /**
     * Special check for Atom content node & its corresponding type attribute.  Handles the following unique cases:
     * 
     * - if type is "applicaton/json"
     * - error cases where the type is invalid
     *
     * @param key the current key-value pair key
     * @param xmlWriter xml writer object
     * @param prefixParent - prefix of parent node
     * @param nsMap - all namespaces in effect
     * @param map - value of the key-value pair
     *
     * @return if true, JSON was written as content and done
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private boolean checkAndWriteAtomJsonContent( String key,
                                                  XMLStreamWriter xmlWriter,
                                                  String prefixParent,
                                                  NameSpaceMaps nsMap,
                                                  Map<String, Object> map ) throws XMLStreamException, JSONException {

        if ( key.equals( CONTENT ) && isAtomShortHand(prefixParent, nsMap) ) {
            String typeValue = (String) map.get(TYPE_ATTR);

            // CF-154: handle JSON events with content type="application/json".
            // This will go in as <content type="application/json">...</content>
            if ( typeValue != null && typeValue.equals(JSON_CTYPE) ) {
                xmlWriter.writeAttribute(TYPE_ATTR, JSON_CTYPE);
                Object textValue = map.get(TEXT);
                if ( textValue != null && textValue instanceof Map ) {
                    Map jsonContent = (Map) textValue;

                    final JsonSerializer jsonSerializer = JSON_SERIALIZER_FACTORY.create();
                    Object obj = jsonSerializer.serialize(jsonContent);
                    xmlWriter.writeCharacters(obj.toString());
                    xmlWriter.writeEndElement();
                    return true;
                } else {
                    throw new JSONException("JSON content object with type='application/json' must have @text string value and @text must be parseable as Map");
                }
            } else if ( typeValue != null && typeValue.equals(XML_CTYPE) ) {
                throw new JSONException("JSON content object must not has type='application/xml'");
            } else if ( typeValue == null ) {
                xmlWriter.writeAttribute(TYPE_ATTR, "application/xml");
            }
        }
        return false;
    }

    /**
     * Create new namespace map node & check if we are in the custom content of a CADF attachement node.
     *
     * @param key current key-value pair key
     * @param nsMapP namespaces in effect
     * @param prefix current namepsace prefix in effect
     *
     * @return updated namespace map
     */
    private NameSpaceMaps updateNameSpaceMap( String key, NameSpaceMaps nsMapP, String prefix ) {
        NameSpaceMaps nsMap = new NameSpaceMaps( nsMapP );

        if( isCadfAttachCustom( key, nsMap, prefix ) )
            nsMap.setInCadfAttachCustom( true );
        return nsMap;
    }

    /**
     * Write children nodes & the end XML tag.
     *
     * @param xmlWriter
     * @param prefix
     * @param nsMap
     * @param map - map of the parent node
     * @param mapElem - map of all key-value pairs to be treated as XML nodes.
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private void writeNodesAndEndElement( XMLStreamWriter xmlWriter,
                                          String prefix,
                                          NameSpaceMaps nsMap,
                                          Map<String, Object> map,
                                          Map<String, Object> mapElem ) throws XMLStreamException, JSONException {

        for( String child : mapElem.keySet() ) {

            if( child.equals( TEXT ) ) {

                xmlWriter.writeCharacters( (String) mapElem.get( child ) );
            }
            else {

                writeNode( child, map.get( child ), xmlWriter, nsMap, prefix );
            }
        }

        xmlWriter.writeEndElement();
    }

    /**
     * Write key-value pairs as attributes.  Handle the following unique cases:
     *
     * - For CADF custom node in attachment, treat key-value as xml nodes & text
     * - For CADF contentType attribute, insert namespace prefix.
     *
     * @param xmlWriter - write XML
     * @param prefix - current XML namespace prefix
     * @param nsMap - map of all namespaces in effect
     * @param map - map of children to parse as attributes or elements
     * @param prefixContentType - prefix to insert into value
     *
     * @return list of elements to treat as xml nodes
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private Map<String, Object> writeAttributes( XMLStreamWriter xmlWriter,
                                                 String prefix,
                                                 NameSpaceMaps nsMap,
                                                 Map<String, Object> map,
                                                 String prefixContentType ) throws XMLStreamException, JSONException {

        Map<String, Object> mapElem = new HashMap<String, Object>();

        for( String child : map.keySet() ) {

            Object childValue = map.get( child );

            if( nsMap.isInCadfAttachCustom()
                    && child.equals( VERSION )) {

                xmlWriter.writeAttribute( child, (String)map.get( child ) );
            }
            else if( isCollection( childValue ) ||
                    child.equals( TEXT ) ||
                    nsMap.isInCadfAttachCustom() ) {

                mapElem.put( child, childValue );
            }
            else if( !child.equals( TYPE ) ) {

                // don't default to atom shorthand, do as attribute
                if( isAtomShortHand( prefix, nsMap ) && map.containsKey( TEXT ) ) {

                    xmlWriter.writeAttribute( child, (String)map.get( child ) );
                }
                // if CADF contentType attribute, add namespace prefix to value
                else if( child.equals( CONTENT_TYPE_CADF ) && isCadf( prefix, nsMap ) ) {

                    xmlWriter.writeAttribute( child, childValue.toString().replace( nsMap.getNsPrefix().inverseBidiMap().get( prefixContentType ) + "/", prefixContentType + ":" ) );
                }
                else
                    writeNode( child, map.get( child ), xmlWriter, nsMap, prefix );
            }
        }
        return mapElem;
    }

    /**
     * If a CADF attachment node
     * - add ContentType to nsPrefixMap
     * - write namespace
     *
     * @param key - key for current key-value pair
     * @param xmlWriter - write out XML
     * @param prefix - current xml namespace prefix
     * @param nsMap - map of all namespaces in effect
     * @param map - value of the key-value pair
     *
     * @return new prefix for XML namespace
     *
     * @throws XMLStreamException
     */
    private String checkAndWriteCadfContentType( String key,
                                                 XMLStreamWriter xmlWriter,
                                                 String prefix,
                                                 NameSpaceMaps nsMap,
                                                 Map<String, Object> map ) throws XMLStreamException {
        String prefixContentType = "";

        if ( key.equals( ATTACHMENT )
                && isCadf( prefix, nsMap )
                && map.containsKey( CONTENT_TYPE_CADF ) ) {

            int i = ( (String) map.get( CONTENT_TYPE_CADF ) ).lastIndexOf( '/' );
            String ns = ( (String) map.get( CONTENT_TYPE_CADF ) ).substring( 0, i );

            prefixContentType = NS + prefixInt++;
            nsMap.getNsPrefix().put( ns, prefixContentType );
            nsMap.setCadfPrefix( prefixContentType );
            nsMap.setCadfAttachCustomNode( ( (String) map.get( CONTENT_TYPE_CADF ) ).substring( i + 1 ) );

            xmlWriter.writeNamespace( prefixContentType, ns );
        }
        return prefixContentType;
    }

    /**
     * Write start XML element & namespace, if required.  Handles the special cases:
     *
     * - If in the Cadf attachemnt custom node, make sure to add the contentType as the namespace
     *
     * @param key - key for key-value pair
     * @param xmlWriter - write out XML
     * @param prefix - current XML namespace prefix
     * @param nsMap - all namespaces in effect
     * @param map - value of the key-value pair
     *
     * @return existing namespace prefix
     *
     * @throws XMLStreamException
     * @throws JSONException
     */
    private String writeStartElementAndNamespace( String key,
                                                  XMLStreamWriter xmlWriter,
                                                  String prefix,
                                                  NameSpaceMaps nsMap,
                                                  Map<String, Object> map ) throws XMLStreamException, JSONException {
        // find namespace
        Object ons = getNamespace( key, map );

        if ( key.equals( nsMap.getCadfAttachCustomNode()) && isCadf( prefix, nsMap ) ) {

            prefix = nsMap.getCadfPrefix();
            xmlWriter.writeStartElement( prefix, key, (String) nsMap.getNsPrefix().inverseBidiMap().get( nsMap.getCadfPrefix() ) );
        }
        else if( ons == null ) {

            writeStartElement( key, xmlWriter, nsMap, prefix );
        }
        else if ( isCollection( ons ) ) {

            throw new JSONException( key + "/@type attribute needs to be a valid namespace.  Value cannot be converted into a string value."  );
        }
        else {

            String ns = ons.toString();

            prefix = (String)nsMap.getNsPrefix().get( ns );

            if( prefix == null ) {

                prefix = NS + prefixInt++;
                nsMap.getNsPrefix().put( ns, prefix );
                xmlWriter.writeStartElement( prefix, key, ns );
                xmlWriter.writeNamespace( prefix, ns );
            }
            else {
                xmlWriter.writeStartElement( prefix, key, ns );
            }
        }
        return prefix;
    }

    /**
     * Is the key-value pair the cadf attachment custom content?
     *
     * @param key - key for current key-value pair
     * @param nsMap - all namespaces in effect
     * @param prefix - current xml namespace prefix
     *
     * @return
     */
    private boolean isCadfAttachCustom( String key, NameSpaceMaps nsMap, String prefix ) {
        return key.equals( nsMap.getCadfAttachCustomNode() ) &&
          isCadf( prefix, nsMap );
    }

    private Object getNamespace( String key, Map<String, Object> map ) {

        if( key.equals( EVENT )
                && map.containsKey( TYPE_URI ))
            return map.get( TYPE_URI );
        else
            return map.get( TYPE );
    }

    private boolean isCadf( String prefix, NameSpaceMaps nsMap ) {

        return CADF_NS.equals( nsMap.getNsPrefix().inverseBidiMap().get( prefix ) );
    }

    private boolean isAtomShortHand( String prefix, NameSpaceMaps nsMap ) {
        return ATOM_NS.equals( nsMap.getNsPrefix().inverseBidiMap().get( prefix ) );
    }

    private void writeStartElement( String key, XMLStreamWriter xmlWriter, NameSpaceMaps nsMap, String prefix ) throws XMLStreamException {
        if( prefix == null )
            xmlWriter.writeStartElement( key );
        else {
            xmlWriter.writeStartElement( prefix, key, (String) nsMap.getNsPrefix().inverseBidiMap().get( prefix ) );
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


    /**
     * Holds state information for the process like namespaces &amp; CADF custom attachment info.
     */
    static public class NameSpaceMaps {

        private BidiMap nsPrefix = new DualHashBidiMap();
        private String cadfPrefix = "";
        private String cadfAttachCustomNode = "";
        private boolean isInCadfAttachCustom = false;

        public NameSpaceMaps() { }

        public NameSpaceMaps( NameSpaceMaps maps ) {

            nsPrefix.putAll( maps.getNsPrefix() );
            cadfPrefix = maps.getCadfPrefix();
            cadfAttachCustomNode = maps.getCadfAttachCustomNode();
            isInCadfAttachCustom = maps.isInCadfAttachCustom();
        }

        public BidiMap getNsPrefix() {
            return nsPrefix;
        }

        public String getCadfPrefix() {
            return cadfPrefix;
        }

        public void setCadfPrefix( String cadfPrefix ) {
            this.cadfPrefix = cadfPrefix;
        }

        public String getCadfAttachCustomNode() {
            return cadfAttachCustomNode;
        }

        public void setCadfAttachCustomNode( String cadfNode ) {
            this.cadfAttachCustomNode = cadfNode;
        }


        public boolean isInCadfAttachCustom() {
            return isInCadfAttachCustom;
        }

        public void setInCadfAttachCustom( boolean isInCadfAttachCustom ) {
            this.isInCadfAttachCustom = isInCadfAttachCustom;
        }
    }
}
