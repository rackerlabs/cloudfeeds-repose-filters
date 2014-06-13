# Custom Repose filters for Cloud Feeds
This component provides custom Repose filters which provide functionality 
required by Cloud Feeds in addition to the standard Repose filters.

## tenant-feed
This filter is should only be run on a tenanted-entriy URI  `/.+/events/[^/?]+/entries/.+`

This filter ensures that the tenant id in the URL matches the tenant id of the provided
token as well as the `tid:[tenantid]` category attached the requested entry.

If the `tid:[tenantid]` does not match the URI & token, a 404 is returned.


