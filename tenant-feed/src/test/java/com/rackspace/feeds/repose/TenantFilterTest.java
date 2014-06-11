package com.rackspace.feeds.repose;

import org.junit.Test;
import org.xml.sax.SAXParseException;


import static org.junit.Assert.*;

/**
 * These tests verify the TenantFilter.getResponse() method, which is free from any repose-specific interfaces.
 */
public class TenantFilterTest {

    static final String tidGood = "1234";
    static final String contentJson = "{ 'json': 'content'}";


    @Test
    public void entriesUri200CodeJson() throws Exception {

        TenantFilter tf = new TenantFilter();

        CodeContent c = tf.getResponse( new CodeContent( 200, contentJson ), tidGood, false );

        assertEquals( 200, c.getStatusCode() );
        assertEquals( contentJson, c.getContent() );
    }

    @Test
    public void entriesUri200CodeValidXml() throws Exception {

        TenantFilter tf = new TenantFilter();

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

        CodeContent c = tf.getResponse( new CodeContent( 200, content ), tidGood, true );

        assertEquals( 200, c.getStatusCode() );
        assertEquals( content, c.getContent() );
    }

    @Test
    public void entriesUri200CodeNoTidXml() throws Exception {

        TenantFilter tf = new TenantFilter();

        String content = "<atom:entry/>";

        CodeContent c = tf.getResponse( new CodeContent( 200, content ), tidGood, true );

        assertEquals( 404, c.getStatusCode() );
        assertEquals( TenantFilter.getErrorMessage( 404, TenantFilter.notFound ), c.getContent() );
    }

    @Test( expected= SAXParseException.class )
    public void entriesUri200CodeInvalidXML() throws Exception {

        TenantFilter tf = new TenantFilter();

        String content = "<atom:entry>";

        CodeContent c = tf.getResponse( new CodeContent( 200, content ), tidGood, true );
    }

    @Test
    public void notEntriesUri200Code() throws Exception {

        TenantFilter tf = new TenantFilter();

        String content = "non-entries";

        CodeContent c = tf.getResponse( new CodeContent( 200, content ), null, true );

        assertEquals( 200, c.getStatusCode() );
        assertEquals( content, c.getContent() );
    }

    @Test
    public void entriesUri500Code() throws Exception {

        TenantFilter tf = new TenantFilter();

        String content = "error message";

        CodeContent c = tf.getResponse( new CodeContent( 500, content ), tidGood, true );

        assertEquals( 500, c.getStatusCode() );
        assertEquals( content, c.getContent() );
    }
}
