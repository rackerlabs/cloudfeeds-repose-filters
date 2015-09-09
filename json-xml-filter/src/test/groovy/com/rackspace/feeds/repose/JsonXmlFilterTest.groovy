package com.rackspace.feeds.repose

import org.apache.commons.io.IOUtils
import org.boon.json.JsonException
import spock.lang.Specification
import spock.lang.Unroll

class JsonXmlFilterTest extends Specification {

    static final int JSON = 1;
    static final int XML = 2;

    @Unroll
    def  "json -> xml: #label"( String label, String json, String expected ) {

        when:
        JsonXmlFilter filter = new JsonXmlFilter()
        String xml= filter.json2Xml( IOUtils.toInputStream( json) )
        println(xml)

        then:
        assert xml == expected

        where:
        [label, json, expected] << [
[
        "Valid User Access Event",
        """
{
    "entry" : {
        "@type"   : "http://www.w3.org/2005/Atom",
        "title"   : "Identity User Access Event",
        "content" : {
            "event" : {
                "typeURI"   : "http://schemas.dmtf.org/cloud/audit/1.0/event",
                "id"        : "6fa234aea93f38c26fa234aea93f38c2",
                "eventType" : "activity",
                "eventTime" : "2015-03-12T13:20:00-05:00",
                "action"    : "create/post",
                "outcome"   : "success",

                "initiator" : {
                    "id"      : "10.1.2.3",
                    "typeURI" : "network/node",
                    "name"    : "jackhandy",
                    "host"    : {
                        "address" : "10.1.2.3",
                        "agent"   : "curl/7.8 (i386-redhat-linux-gnu) libcurl 7.8"
                    }
                },

                "target" : {
                    "id"      : "x.x.x.x",
                    "typeURI" : "service",
                    "name"    : "IDM",
                    "host"    : {
                        "address" : "lon.identity.api.rackspacecloud.com"
                    }
                },

                "attachments" : [
                    {
                        "name"        : "auditData",
                        "contentType" : "http://feeds.api.rackspacecloud.com/cadf/user-access-event/auditData",
                        "content"     :  {
                            "auditData" : {
                                "region"          : "DFW",
                                "dataCenter"      : "DFW1",
                                "methodLabel"     : "createToken",
                                "requestURL"      : "https://lon.identity.api.rackspacecloud.com/v2.0/tokens",
                                "queryString"     : "",
                                "tenantId"        : "123456",
                                "responseMessage" : "OK",
                                "userName"        : "jackhandy",
                                "roles"           : "xxx",
                                "version"         : "1"
                            }
                        }
                    }
                ],

                "observer" : {
                    "id"      : "IDM-1-1",
                    "name"    : "repose-6.1.1.1",
                    "typeURI" : "service/security",
                    "host" : {
                        "address" : "repose"
                    }
                },

                "reason" : {
                    "reasonCode" : 200,
                    "reasonType" : "http://www.iana.org/assignments/http-status-codes/http-status-codes.xml"
                }
            }
        }
    }
}
""",
        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title>Identity User Access Event</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://schemas.dmtf.org/cloud/audit/1.0/event" action="create/post" eventTime="2015-03-12T13:20:00-05:00" eventType="activity" id="6fa234aea93f38c26fa234aea93f38c2" outcome="success" typeURI="http://schemas.dmtf.org/cloud/audit/1.0/event">
      <ns1:observer id="IDM-1-1" name="repose-6.1.1.1" typeURI="service/security">
        <ns1:host address="repose"></ns1:host>
      </ns1:observer>
      <ns1:reason reasonCode="200" reasonType="http://www.iana.org/assignments/http-status-codes/http-status-codes.xml"></ns1:reason>
      <ns1:attachments>
        <ns1:attachment xmlns:ns2="http://feeds.api.rackspacecloud.com/cadf/user-access-event" contentType="ns2:auditData" name="auditData">
          <ns1:content>
            <ns2:auditData version="1">
              <ns2:dataCenter>DFW1</ns2:dataCenter>
              <ns2:requestURL>https://lon.identity.api.rackspacecloud.com/v2.0/tokens</ns2:requestURL>
              <ns2:roles>xxx</ns2:roles>
              <ns2:methodLabel>createToken</ns2:methodLabel>
              <ns2:tenantId>123456</ns2:tenantId>
              <ns2:queryString></ns2:queryString>
              <ns2:region>DFW</ns2:region>
              <ns2:responseMessage>OK</ns2:responseMessage>
              <ns2:userName>jackhandy</ns2:userName>
            </ns2:auditData>
          </ns1:content>
        </ns1:attachment>
      </ns1:attachments>
      <ns1:initiator id="10.1.2.3" name="jackhandy" typeURI="network/node">
        <ns1:host address="10.1.2.3" agent="curl/7.8 (i386-redhat-linux-gnu) libcurl 7.8"></ns1:host>
      </ns1:initiator>
      <ns1:target id="x.x.x.x" name="IDM" typeURI="service">
        <ns1:host address="lon.identity.api.rackspacecloud.com"></ns1:host>
      </ns1:target>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""],
                [
        "Valid JSON",
        """
            { "entry" : {
                "@type" : "http://www.w3.org/2005/Atom",
                "title" : {
                    "@text" : "totally tubular title",
                    "type" : "text"
                },
                "content" : {
                    "event" : {
                        "@type" : "http://docs.rackspace.com/core/event",
                        "eventTime" : "2013-03-15T11:51:11Z",
                        "region" : "DFW",
                        "dataCenter" : "DFW1",
                        "type" : "INFO",
                        "id" : "8d89673c-c989-11e1-895a-0b3d632a8a8",
                        "version" : "1",
                        "product" : {
                            "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                            "serviceCode" : "EmailAppsMSService",
                            "version" : "1",
                            "key" : "(domain=5002_domain_2.com)|(service=5002_domain_2.com)",
                            "productType" : "lync",
                            "operation" : "UPDATE",
                            "status" : "COMPLETED",
                            "request" : "HTTP GET",
                            "response" : "200",
                            "dependent" : [
                                {
                                    "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                                },
                                {
                                    "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                                }
                            ]
                        }
                    }
                }
              }
            }
            """,
        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" eventTime="2013-03-15T11:51:11Z" id="8d89673c-c989-11e1-895a-0b3d632a8a8" region="DFW" type="INFO" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""],
                [
                        "Valid JSON with categories and links",
                        """
{ "entry" : {
    "@type" : "http://www.w3.org/2005/Atom",
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "category": [
      {
        "term": "DFW1"
      },
      {
        "term": "tid:1234"
      },
      {
        "term": "some.random.category"
      }
    ],
    "link": [
      {
        "href": "https://myhost/detail.html",
        "rel": "detail"
      },
      {
        "href": "https://myhost/contact.html",
        "rel": "contact"
      }
    ],
    "content" : {
        "event" : {
            "@type" : "http://docs.rackspace.com/core/event",
            "endTime" : "2012-06-15T10:19:52Z",
            "startTime" : "2012-06-14T10:19:52Z",
            "region" : "DFW",
            "dataCenter" : "DFW1",
            "type" : "USAGE",
            "id" : "8d89673c-c989-11e1-895a-0b3d632a8a89",
            "resourceId" : "3863d42a-ec9a-11e1-8e12-df8baa3ca440",
            "tenantId" : "1234",
            "version" : "1",
            "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "serviceCode" : "EmailAppsMSService",
                "version" : "1",
                "key" : "(domain=5002_domain_2.com)|(service=5002_domain_2.com)",
                "productType" : "lync",
                "operation" : "UPDATE",
                "status" : "COMPLETED",
                "request" : "HTTP GET",
                "response" : "200",
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:link href="https://myhost/detail.html" rel="detail"/>
  <ns0:link href="https://myhost/contact.html" rel="contact"/>
  <ns0:category term="DFW1"/>
  <ns0:category term="tid:1234"/>
  <ns0:category term="some.random.category"/>
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""
                ],
                [       "Mixed Content (JSON inside XML)",
                        """
{
  "entry": {
    "updated": "2014-10-29T16:24:02.856Z",
    "title": {
      "@text": "Dedicated vCloud event",
      "type": "text"
    },
    "@type": "http://www.w3.org/2005/Atom",
    "content": {
      "@text": {
        "vmProperties": {
          "containerOS": "Microsoft Windows Server 2012 (64-bit)",
          "cpuInfo": {
            "coresPerSocket": 1,
            "cpuCount": 4
          },
          "computerName": "WindowsServ-001",
          "hypervisor": "581846",
          "memoryMb": "4096",
          "vcdName": "vcd02-2848639.mv.rackspace.com",
          "organization": "urn:vcloud:org:0bc35ece-34d8-45bc-a61e-9e223e022165",
          "vmName": "WindowsServer_2012_R2_Standard_vcloud_core",
          "networks": [
            {
              "isPrimary": "true",
              "vlan": "1470",
              "ipAddress": "192.168.100.5",
              "name": "ExNet-Inside-VLAN1470"
            }
          ],
          "vcdUrn": "urn:vcloud:vm:fc51f8a1-11c5-4f78-89bf-611af92d8b83",
          "vcenterUuid": "4219fe9a-fc2d-cd52-d55d-4d6842c4beb2"
        },
        "datacenter": "IAD3",
        "timestamp": "2014-10-29T16:23:13.431+0000",
        "source": "qe.virtops.rackspacecloud.com",
        "raxData": {
          "osName.WINDOWS_2012_R2_STD_X64": {
            "category": "osName",
            "attributes": {
              "Dedicated vCloud Director OS Type": "WINDOWS"
            },
            "type": "DeviceConfig",
            "name": "WINDOWS_2012_R2_STD_X64"
          },
          "platform.DVC_WINDOWS_UNSUPPORTED": {
            "category": "platform",
            "attributes": {},
            "type": "DeviceConfig",
            "name": "DVC_WINDOWS_UNSUPPORTED"
          }
        },
        "type": "virtops.vm.create",
        "tenant": "hybrid:2848639"
      },
      "type": "application/json"
    },
    "published": "2014-10-29T16:24:02.856Z",
    "id": "urn:uuid:1f1b37e2-503b-4f32-bc8b-6d1d054efc21"
  }
}
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:id>urn:uuid:1f1b37e2-503b-4f32-bc8b-6d1d054efc21</ns0:id>
  <ns0:published>2014-10-29T16:24:02.856Z</ns0:published>
  <ns0:updated>2014-10-29T16:24:02.856Z</ns0:updated>
  <ns0:title type="text">Dedicated vCloud event</ns0:title>
  <ns0:content type="application/json">{"datacenter":"IAD3","raxData":{"osName.WINDOWS_2012_R2_STD_X64":{"attributes":{"Dedicated vCloud Director OS Type":"WINDOWS"},"category":"osName","name":"WINDOWS_2012_R2_STD_X64","type":"DeviceConfig"},"platform.DVC_WINDOWS_UNSUPPORTED":{"attributes":{},"category":"platform","name":"DVC_WINDOWS_UNSUPPORTED","type":"DeviceConfig"}},"source":"qe.virtops.rackspacecloud.com","tenant":"hybrid:2848639","timestamp":"2014-10-29T16:23:13.431+0000","type":"virtops.vm.create","vmProperties":{"computerName":"WindowsServ-001","containerOS":"Microsoft Windows Server 2012 (64-bit)","cpuInfo":{"coresPerSocket":1,"cpuCount":4},"hypervisor":"581846","memoryMb":"4096","networks":[{"ipAddress":"192.168.100.5","isPrimary":"true","name":"ExNet-Inside-VLAN1470","vlan":"1470"}],"organization":"urn:vcloud:org:0bc35ece-34d8-45bc-a61e-9e223e022165","vcdName":"vcd02-2848639.mv.rackspace.com","vcdUrn":"urn:vcloud:vm:fc51f8a1-11c5-4f78-89bf-611af92d8b83","vcenterUuid":"4219fe9a-fc2d-cd52-d55d-4d6842c4beb2","vmName":"WindowsServer_2012_R2_Standard_vcloud_core"}}</ns0:content>
</ns0:entry>"""
                ],
                [
                        "Valid JSON comprehensive Atom entry",
                        """
                        {
                            "entry": {
                            "category": [
                                    {
                                        "term": "rgn:DFW",
                                        "scheme": "http://docs.rackspace.com",
                                        "label": "region"
                                    },
                                    {
                                        "term": "dc:DFW1",
                                        "scheme": "http://docs.rackspace.com",
                                        "label": "datacenter"
                                    },
                                    {
                                        "term": "tid:123456",
                                        "scheme": "http://docs.rackspace.com",
                                        "label": "tenantId"
                                    }
                            ],
                            "updated": "2005-07-31T12:29:29Z",
                            "title": {
                                "@text": "Less: <b> < </b>",
                                "type": "html"
                            },
                            "author": {
                                "name": "Joe Racker",
                                "uri": "http://docs.rackspace.com/"
                            },
                            "summary": {
                                "@text": "Summary: <b>HAVE A GREAT DAY!</b>",
                                "type": "html"
                            },
                            "content": {
                                "@text": "<p><i>[Update: The Atom draft is finished.]</i></p>",
                                "type": "html"
                            },
                            "link": [
                                    {
                                        "href": "http://example.org/2005/04/02/atom",
                                        "rel": "alternate"
                                    },
                                    {
                                        "href": "http://example.org/audio/ph34r_my_podcast.mp3",
                                        "rel": "enclosure"
                                    }
                            ],
                            "published": "2003-12-13T08:29:29-04:00",
                            "id": "tag:example.org,2003:4.2397",
                            "@type": "http://www.w3.org/2005/Atom"
                        }
                }
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:id>tag:example.org,2003:4.2397</ns0:id>
  <ns0:published>2003-12-13T08:29:29-04:00</ns0:published>
  <ns0:updated>2005-07-31T12:29:29Z</ns0:updated>
  <ns0:summary type="html">Summary: &lt;b&gt;HAVE A GREAT DAY!&lt;/b&gt;</ns0:summary>
  <ns0:author>
    <ns0:name>Joe Racker</ns0:name>
    <ns0:uri>http://docs.rackspace.com/</ns0:uri>
  </ns0:author>
  <ns0:link href="http://example.org/2005/04/02/atom" rel="alternate"/>
  <ns0:link href="http://example.org/audio/ph34r_my_podcast.mp3" rel="enclosure"/>
  <ns0:category label="region" scheme="http://docs.rackspace.com" term="rgn:DFW"/>
  <ns0:category label="datacenter" scheme="http://docs.rackspace.com" term="dc:DFW1"/>
  <ns0:category label="tenantId" scheme="http://docs.rackspace.com" term="tid:123456"/>
  <ns0:title type="html">Less: &lt;b&gt; &lt; &lt;/b&gt;</ns0:title>
  <ns0:content type="html">&lt;p&gt;&lt;i&gt;[Update: The Atom draft is finished.]&lt;/i&gt;&lt;/p&gt;</ns0:content>
</ns0:entry>"""
                ],
                [
                        "JSON with same namespace declared multiple times",
                        """
{ "entry" : {
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "@type" : "http://www.w3.org/2005/Atom",
    "content" : {
        "@type" : "http://www.w3.org/2005/Atom",
        "event" : {
            "@type" : "http://docs.rackspace.com/core/event",
            "endTime" : "2012-06-15T10:19:52Z",
            "startTime" : "2012-06-14T10:19:52Z",
            "region" : "DFW",
            "dataCenter" : "DFW1",
            "type" : "USAGE",
            "id" : "8d89673c-c989-11e1-895a-0b3d632a8a89",
            "resourceId" : "3863d42a-ec9a-11e1-8e12-df8baa3ca440",
            "tenantId" : "1234",
            "version" : "1",
            "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "serviceCode" : "EmailAppsMSService",
                "version" : "1",
                "key" : "(domain=5002_domain_2.com)|(service=5002_domain_2.com)",
                "productType" : "lync",
                "operation" : "UPDATE",
                "status" : "COMPLETED",
                "request" : "HTTP GET",
                "response" : "200",
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}
""",
    """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""
                ],
                [
                        "JSON with integer in namespace",
                        """
{ "entry" : {
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "@type" : 1,
    "content" : {
        "event" : {
            "@type" : 2,
            "version" : "1",
            "product" : {
                "@type" : 3,
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="1">
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content>
    <ns1:event xmlns:ns1="2" version="1">
      <ns2:product xmlns:ns2="3">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""
                ],
                [
                        "JSON with no namespace",
                        """
{ "entry" : {
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "content" : {
        "event" : {
            "version" : "1",
            "product" : {
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}""",
                """<?xml version="1.0" ?>
<entry>
  <title type="text">totally tubular title</title>
  <content>
    <event version="1">
      <product>
        <dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></dependent>
        <dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></dependent>
      </product>
    </event>
  </content>
</entry>"""],
                [
                        "JSON with array of arrays in entry",
                        """
{ "entry" : {
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "@type" : "http://www.w3.org/2005/Atom",
    "content" : [ {
        "event" : [{
            "@type" : "http://docs.rackspace.com/core/event",
            },
            {
            "@type" : "http://docs.rackspace.com/core/event",
            }]
        },
        {
        "event" : [{
            "@type" : "http://docs.rackspace.com/core/event",
            },
            {
            "@type" : "http://docs.rackspace.com/core/event",
            }]
        } ]
    }
  }
}
""","""<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event"></ns1:event>
    <ns2:event xmlns:ns2="http://docs.rackspace.com/core/event"></ns2:event>
  </ns0:content>
  <ns0:content type="application/xml">
    <ns3:event xmlns:ns3="http://docs.rackspace.com/core/event"></ns3:event>
    <ns4:event xmlns:ns4="http://docs.rackspace.com/core/event"></ns4:event>
  </ns0:content>
</ns0:entry>"""
                ],
                [
                        """escaped newline in JSON string""",
                        """
{ "entry" : {
    "@type" : "http://www.w3.org/2005/Atom",
    "title" : {
        "@text" : "\\ntotally tubular title\\n",
        "type" : "text"
    },
    "content" : {
        "event" : {
            "@type" : "http://docs.rackspace.com/core/event",
            "endTime" : "2012-06-15T10:19:52Z",
            "startTime" : "2012-06-14T10:19:52Z",
            "region" : "DFW",
            "dataCenter" : "DFW1",
            "type" : "USAGE",
            "id" : "8d89673c-c989-11e1-895a-0b3d632a8a89",
            "resourceId" : "3863d42a-ec9a-11e1-8e12-df8baa3ca440",
            "tenantId" : "1234",
            "version" : "1",
            "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "serviceCode" : "EmailAppsMSService",
                "version" : "1",
                "key" : "(domain=5002_domain_2.com)|(service=5002_domain_2.com)",
                "productType" : "lync",
                "operation" : "UPDATE",
                "status" : "COMPLETED",
                "request" : "HTTP GET",
                "response" : "200",
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title type="text">
totally tubular title
</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""
                ],
                ["Extra node in atom envelope",
                 """
{ "entry" : {
    "@type" : "http://www.w3.org/2005/Atom",
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "extended" : "extended content",
    "content" : {
        "event" : {
            "@type" : "http://docs.rackspace.com/core/event",
            "eventTime" : "2013-03-15T11:51:11Z",
            "region" : "DFW",
            "dataCenter" : "DFW1",
            "type" : "INFO",
            "id" : "8d89673c-c989-11e1-895a-0b3d632a8a89",
            "version" : "1",
            "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "serviceCode" : "EmailAppsMSService",
                "version" : "1",
                "key" : "(domain=5002_domain_2.com)|(service=5002_domain_2.com)",
                "productType" : "lync",
                "operation" : "UPDATE",
                "status" : "COMPLETED",
                "request" : "HTTP GET",
                "response" : "200",
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
  }
}
""",
                 """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:extended>extended content</ns0:extended>
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" eventTime="2013-03-15T11:51:11Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" type="INFO" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""
     ],
                [
                        "JSON with array at root",
                        """
{ "entry" : [ {
    "@type" : "http://www.w3.org/2005/Atom",
    "title" : {
        "@text" : "totally tubular title",
        "type" : "text"
    },
    "content" : {
        "event" : {
            "@type" : "http://docs.rackspace.com/core/event",
            "version" : "1",
            "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "response" : "200",
                "dependent" : [
                    {
                        "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                    },
                    {
                        "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                    }
                ]
            }
        }
    }
},
{
"@type" : "http://www.w3.org/2005/Atom",
"content" : {
"event" : {
    "@type" : "http://docs.rackspace.com/core/event",

    "version" : "1",
    "product" : {
        "@type" : "http://docs.rackspace.com/event/emailapps_msservice",

        "response" : "200",
        "dependent" : [
            {
                "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
            },
            {
                "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
            }
        ]
    }
}
}
}
]
}""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title type="text">totally tubular title</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" response="200">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
</ns0:entry>
<ns3:entry xmlns:ns3="http://www.w3.org/2005/Atom">
  <ns3:content type="application/xml">
    <ns4:event xmlns:ns4="http://docs.rackspace.com/core/event" version="1">
      <ns5:product xmlns:ns5="http://docs.rackspace.com/event/emailapps_msservice" response="200">
        <ns5:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns5:dependent>
        <ns5:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns5:dependent>
      </ns5:product>
    </ns4:event>
  </ns3:content>
</ns3:entry>"""
                ],
                [
                    "Content type='text'",
                        """
{
    "entry": {
        "updated": "2014-09-26T23:58:26.939Z",
        "author": {
          "name": "Atom Hopper Team"
        },
        "title": {
          "@text": "Slice Action",
          "type": "text"
        },
        "id": "urn:uuid:175317ad-46cd-0902-abb5-8d050219b315",
        "content": {
          "type": "text",
          "@text": "some random text inside the atom content element. should not happen with real product event, but we should not fail"
        },
        "published": "2014-09-26T23:58:26.939Z",
        "@type": "http://www.w3.org/2005/Atom"
    }
}
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:id>urn:uuid:175317ad-46cd-0902-abb5-8d050219b315</ns0:id>
  <ns0:published>2014-09-26T23:58:26.939Z</ns0:published>
  <ns0:updated>2014-09-26T23:58:26.939Z</ns0:updated>
  <ns0:author>
    <ns0:name>Atom Hopper Team</ns0:name>
  </ns0:author>
  <ns0:title type="text">Slice Action</ns0:title>
  <ns0:content type="text">some random text inside the atom content element. should not happen with real product event, but we should not fail</ns0:content>
</ns0:entry>"""
                ],
                [
                        "Valid User Access Event With Multiple Attachments",
                        """
{
    "entry" : {
        "@type"   : "http://www.w3.org/2005/Atom",
        "title"   : "Identity User Access Event",
        "content" : {
            "event" : {
                "typeURI"   : "http://schemas.dmtf.org/cloud/audit/1.0/event",
                "id"        : "6fa234aea93f38c26fa234aea93f38c2",
                "eventType" : "activity",
                "eventTime" : "2015-03-12T13:20:00-05:00",
                "action"    : "create/post",
                "outcome"   : "success",

                "initiator" : {
                    "id"      : "10.1.2.3",
                    "typeURI" : "network/node",
                    "name"    : "jackhandy",
                    "host"    : {
                        "address" : "10.1.2.3",
                        "agent"   : "curl/7.8 (i386-redhat-linux-gnu) libcurl 7.8"
                    }
                },

                "target" : {
                    "id"      : "x.x.x.x",
                    "typeURI" : "service",
                    "name"    : "IDM",
                    "host"    : {
                        "address" : "lon.identity.api.rackspacecloud.com"
                    }
                },

                "attachments" : [
                    {
                        "name"        : "auditData",
                        "contentType" : "http://feeds.api.rackspacecloud.com/cadf/user-access-event/auditData",
                        "content"     :  {
                            "auditData" : {
                                "region"          : "DFW",
                                "dataCenter"      : "DFW1",
                                "methodLabel"     : "createToken",
                                "requestURL"      : "https://lon.identity.api.rackspacecloud.com/v2.0/tokens",
                                "queryString"     : "",
                                "tenantId"        : "123456",
                                "responseMessage" : "OK",
                                "userName"        : "jackhandy",
                                "roles"           : "xxx",
                                "version"         : "1"
                            }
                        }
                    },
                    {
                        "name"        : "secondAttachmentData",
                        "contentType" : "http://feeds.api.rackspacecloud.com/cadf/user-access-event/secondAttachmentData",
                        "content"     :  {
                            "secondAttachmentData" : {
                                "key1"          : "DFW",
                                "key2"      : "DFW1"
                            }
                        }
                    }
                ],

                "observer" : {
                    "id"      : "IDM-1-1",
                    "name"    : "repose-6.1.1.1",
                    "typeURI" : "service/security",
                    "host" : {
                        "address" : "repose"
                    }
                },

                "reason" : {
                    "reasonCode" : 200,
                    "reasonType" : "http://www.iana.org/assignments/http-status-codes/http-status-codes.xml"
                }
            }
        }
    }
}
""",
                        """<?xml version="1.0" ?>
<ns0:entry xmlns:ns0="http://www.w3.org/2005/Atom">
  <ns0:title>Identity User Access Event</ns0:title>
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://schemas.dmtf.org/cloud/audit/1.0/event" action="create/post" eventTime="2015-03-12T13:20:00-05:00" eventType="activity" id="6fa234aea93f38c26fa234aea93f38c2" outcome="success" typeURI="http://schemas.dmtf.org/cloud/audit/1.0/event">
      <ns1:observer id="IDM-1-1" name="repose-6.1.1.1" typeURI="service/security">
        <ns1:host address="repose"></ns1:host>
      </ns1:observer>
      <ns1:reason reasonCode="200" reasonType="http://www.iana.org/assignments/http-status-codes/http-status-codes.xml"></ns1:reason>
      <ns1:attachments>
        <ns1:attachment xmlns:ns2="http://feeds.api.rackspacecloud.com/cadf/user-access-event" contentType="ns2:auditData" name="auditData">
          <ns1:content>
            <ns2:auditData version="1">
              <ns2:dataCenter>DFW1</ns2:dataCenter>
              <ns2:requestURL>https://lon.identity.api.rackspacecloud.com/v2.0/tokens</ns2:requestURL>
              <ns2:roles>xxx</ns2:roles>
              <ns2:methodLabel>createToken</ns2:methodLabel>
              <ns2:tenantId>123456</ns2:tenantId>
              <ns2:queryString></ns2:queryString>
              <ns2:region>DFW</ns2:region>
              <ns2:responseMessage>OK</ns2:responseMessage>
              <ns2:userName>jackhandy</ns2:userName>
            </ns2:auditData>
          </ns1:content>
        </ns1:attachment>
        <ns1:attachment xmlns:ns3="http://feeds.api.rackspacecloud.com/cadf/user-access-event" contentType="ns3:secondAttachmentData" name="secondAttachmentData">
          <ns1:content>
            <ns3:secondAttachmentData>
              <ns3:key1>DFW</ns3:key1>
              <ns3:key2>DFW1</ns3:key2>
            </ns3:secondAttachmentData>
          </ns1:content>
        </ns1:attachment>
      </ns1:attachments>
      <ns1:initiator id="10.1.2.3" name="jackhandy" typeURI="network/node">
        <ns1:host address="10.1.2.3" agent="curl/7.8 (i386-redhat-linux-gnu) libcurl 7.8"></ns1:host>
      </ns1:initiator>
      <ns1:target id="x.x.x.x" name="IDM" typeURI="service">
        <ns1:host address="lon.identity.api.rackspacecloud.com"></ns1:host>
      </ns1:target>
    </ns1:event>
  </ns0:content>
</ns0:entry>"""]
        ]
    }

    @Unroll
    def  "json -> xml exception: #label"( String label, String json, Class clazz ) {

        when:
        JsonXmlFilter filter = new JsonXmlFilter()

        filter.json2Xml(IOUtils.toInputStream(json))

        then:
        thrown clazz

        where:
        [label, json, clazz] << [[
                                         "Invalid JSON",
                                         """
{
  "entry" : {
    "@type" : "http://www.w3.org/2005/Atom",
    "content"
  }
}
}""",
        JsonException
                  ],
        [
                "JSON with multiple root nodes",
                """
{ "entry1" : {
"title" : {
"@text" : "totally tubular title",
"type" : "text"
},
"@type" : "http://www.w3.org/2005/Atom",
"content" : {
"event" : {
    "@type" : "http://docs.rackspace.com/core/event",

    "version" : "1",
    "product" : {
        "@type" : "http://docs.rackspace.com/event/emailapps_msservice",

        "response" : "200",
        "dependent" : [
            {
                "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
            },
            {
                "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
            }
        ]
    }
}
}
},
"entry2" : {
"title" : {
"@text" : "totally tubular title",
"type" : "text"
},
"@type" : "http://www.w3.org/2005/Atom",
"content" : {
"event" : {
    "@type" : "http://docs.rackspace.com/core/event",
    "version" : "1",
    "product" : {
        "@type" : "http://docs.rackspace.com/event/emailapps_msservice",

        "response" : "200",
        "dependent" : [
            {
                "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
            },
            {
                "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
            }
        ]
    }
}
}
}
}""",
                Json2Xml.JSONException
        ],
        [
                "JSON with @type as map",
                """
{ "entry" : {
"title" : {
"@text" : "totally tubular title",
"type" : "text"
},
"@type" : {
"attribute" : 1
},
"content" : {
"event" : {
    "@type" : "http://docs.rackspace.com/core/event",
    "version" : "1",
    "product" : {
        "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
        "response" : "200",
        "dependent" : [
            {
                "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
            },
            {
                "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
            }
        ]
    }
}
}
}
}""",
                Json2Xml.JSONException
        ],
        [
                "JSON with @type as map",
                """
{ "entry" : {
"title" : {
"@text" : "totally tubular title",
"type" : "text"
},
"@type" : [{
"attribute" : 1
},
{
"attribute2" : 2
}
],
"content" : {
"event" : {
    "@type" : "http://docs.rackspace.com/core/event",
    "version" : "1",
    "product" : {
        "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
        "response" : "200",
        "dependent" : [
            {
                "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
            },
            {
                "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
            }
        ]
    }
}
}
}
}""",
                Json2Xml.JSONException
        ],
        [
                "content type='application/xml' is not allowed",
                """
{
    "entry": {
        "updated": "2014-09-26T23:58:26.939Z",
        "author": {
          "name": "Atom Hopper Team"
        },
        "title": {
          "@text": "Slice Action",
          "type": "text"
        },
        "id": "urn:uuid:175317ad-46cd-0902-abb5-8d050219b315",
        "content": {
          "type": "application/xml",
          "event": {
              "@type" : "http://docs.rackspace.com/core/event",
              "version" : "1",
              "product" : {
                "@type" : "http://docs.rackspace.com/event/emailapps_msservice",
                "response" : "200",
                "dependent" : [
                  {
                    "key" : "(domain=1002_domain_200.com)|(service=1002_domain_100.com)"
                  },
                  {
                    "key" : "(domain=2002_domain_200.com)|(service=2002_domain_200.com)"
                  }
                ]
              }
          }
        },
        "published": "2014-09-26T23:58:26.939Z",
        "@type": "http://www.w3.org/2005/Atom"
    }
}
"""     ,
        Json2Xml.JSONException
        ],
            [
                    "content type='application/json' must contain json",
                    """
{
    "entry": {
        "updated": "2014-09-26T23:58:26.939Z",
        "author": {
          "name": "Atom Hopper Team"
        },
        "title": {
          "@text": "Slice Action",
          "type": "text"
        },
        "id": "urn:uuid:175317ad-46cd-0902-abb5-8d050219b315",
        "content": {
          "type": "application/json",
          "@text": "some text that is not json"
        },
        "published": "2014-09-26T23:58:26.939Z",
        "@type": "http://www.w3.org/2005/Atom"
    }
}
"""     ,
        Json2Xml.JSONException
            ]
    ]
}

    def "escapeJSON"() {

        when:

        def msg = """one
two
three
"""
        def escapeMsg = (new JsonXmlFilter()).jsonEscape( msg )

        then:
        assert escapeMsg == "one\\ntwo\\nthree\\n"
    }
}
