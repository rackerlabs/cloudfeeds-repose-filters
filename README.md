# Custom Repose filters for Cloud Feeds
This component contains custom Repose filters which provide functionality 
required by Cloud Feeds in addition to the standard Repose filters.

## json-xml-filter
This filter is in the chain if the POST request contains request body in JSON
(content-type is vnd.rackspace.atom+json). 

This filter reads the JSON Atom request body and produces an XML equivalent.

# How to build
To build this component, we require:
* JDK 1.8
Note that JDK 1.7 will compile the code fine but the unit tests would fail.

## Simple build
```mvn clean install```

## Build an RPM
```mvn clean install -P build-rpm```

