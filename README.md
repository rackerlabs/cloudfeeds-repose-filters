cloudfeeds-repose-filters
=========================

Custom Repose filters for Cloud Feeds

Notes for repose:

When I run the filter within tomcat, respWrap.getContent() (line 99) returns 
the origin service's response body.

When I run the filter within repose, respWrap.getContent() (line 99) returns 
an empty string.

In repose, the origin services reponse is returned when this filter is 
enabled, so it passed through the filter chain, its just not available through
the standard ServletResponse API.