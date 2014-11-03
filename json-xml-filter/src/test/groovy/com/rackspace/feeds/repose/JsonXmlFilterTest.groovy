package com.rackspace.feeds.repose

import org.apache.commons.io.IOUtils
import org.boon.json.JsonException
import spock.lang.Specification
import spock.lang.Unroll
import com.rackspace.feeds.repose.JsonXmlFilter.JSONException


class JsonXmlFilterTest extends Specification {

    static final int LABEL = 0
    static final int JSON = 1;
    static final int XML = 2;

    @Unroll
    def  "json -> xml: #label"( String label, String json, String expected ) {

        when:
        JsonXmlFilter filter = new JsonXmlFilter()
        String xml= filter.json2Xml( IOUtils.toInputStream( json) )

        then:
        assert expected == xml

        where:
        [label, json, expected] << [
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
  <ns0:content type="application/json">{"datacenter":"IAD3","raxData":{"osName.WINDOWS_2012_R2_STD_X64":{"attributes":{"Dedicated vCloud Director OS Type":"WINDOWS"},"category":"osName","name":"WINDOWS_2012_R2_STD_X64","type":"DeviceConfig"},"platform.DVC_WINDOWS_UNSUPPORTED":{"attributes":{},"category":"platform","name":"DVC_WINDOWS_UNSUPPORTED","type":"DeviceConfig"}},"source":"qe.virtops.rackspacecloud.com","tenant":"hybrid:2848639","timestamp":"2014-10-29T16:23:13.431+0000","type":"virtops.vm.create","vmProperties":{"computerName":"WindowsServ-001","containerOS":"Microsoft Windows Server 2012 (64-bit)","cpuInfo":{"coresPerSocket":1,"cpuCount":4},"hypervisor":"581846","memoryMb":"4096","networks":[{"ipAddress":"192.168.100.5","isPrimary":"true","name":"ExNet-Inside-VLAN1470","vlan":"1470"}],"organization":"urn:vcloud:org:0bc35ece-34d8-45bc-a61e-9e223e022165","vcdName":"vcd02-2848639.mv.rackspace.com","vcdUrn":"urn:vcloud:vm:fc51f8a1-11c5-4f78-89bf-611af92d8b83","vcenterUuid":"4219fe9a-fc2d-cd52-d55d-4d6842c4beb2","vmName":"WindowsServer_2012_R2_Standard_vcloud_core"}}</ns0:content>
  <ns0:title type="text">Dedicated vCloud event</ns0:title>
</ns0:entry>"""
                ],
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="2" version="1">
      <ns2:product xmlns:ns2="3">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <content type="application/xml">
    <event version="1">
      <product>
        <dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></dependent>
        <dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></dependent>
      </product>
    </event>
  </content>
  <title type="text">totally tubular title</title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event"></ns1:event>
    <ns2:event xmlns:ns2="http://docs.rackspace.com/core/event"></ns2:event>
  </ns0:content>
  <ns0:content type="application/xml">
    <ns3:event xmlns:ns3="http://docs.rackspace.com/core/event"></ns3:event>
    <ns4:event xmlns:ns4="http://docs.rackspace.com/core/event"></ns4:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" endTime="2012-06-15T10:19:52Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" resourceId="3863d42a-ec9a-11e1-8e12-df8baa3ca440" startTime="2012-06-14T10:19:52Z" tenantId="1234" type="USAGE" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">
totally tubular title
</ns0:title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" dataCenter="DFW1" eventTime="2013-03-15T11:51:11Z" id="8d89673c-c989-11e1-895a-0b3d632a8a89" region="DFW" type="INFO" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" key="(domain=5002_domain_2.com)|(service=5002_domain_2.com)" operation="UPDATE" productType="lync" request="HTTP GET" response="200" serviceCode="EmailAppsMSService" status="COMPLETED" version="1">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <ns0:content type="application/xml">
    <ns1:event xmlns:ns1="http://docs.rackspace.com/core/event" version="1">
      <ns2:product xmlns:ns2="http://docs.rackspace.com/event/emailapps_msservice" response="200">
        <ns2:dependent key="(domain=1002_domain_200.com)|(service=1002_domain_100.com)"></ns2:dependent>
        <ns2:dependent key="(domain=2002_domain_200.com)|(service=2002_domain_200.com)"></ns2:dependent>
      </ns2:product>
    </ns1:event>
  </ns0:content>
  <ns0:title type="text">totally tubular title</ns0:title>
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
  <ns0:content type="application/xml" type="text">some random text inside the atom content element. should not happen with real product event, but we should not fail</ns0:content>
  <ns0:author>
    <ns0:name>Atom Hopper Team</ns0:name>
  </ns0:author>
  <ns0:title type="text">Slice Action</ns0:title>
</ns0:entry>"""
                ]
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
                JSONException
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
                JSONException
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
                JSONException
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
        JSONException
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
        JSONException
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
