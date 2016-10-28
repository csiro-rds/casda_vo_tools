# CASDA VO Tools Release Notes

## v1.4 - 2016-10-28

* New Features
    *   Removed PostGIS health check
	*   Allow for a separate higher volume SODA endpoint in datalink responses
	*   Hide services with blank descriptions in datalink responses
	

* Bug fixes
    *   Length for character type was incorrectly set as 0
	*   Invalid VOTable types in results tables for automatically discovered table columns
	*   Tap UI page has broken links
	*   Blank metadata entries cause error on xml output

## v1.3 - 2016-07-28

* New Features
    *   Updated documentation to be primarily aimed at deployers rather than developers
    *   Page css is now configurable
    *   Allowed for logging to be externally configured
    *   Page footer now includes build information
    *   Added support for PostgreSQL BIT type
    *   Added conversion of non UTF-8 data to XML character codes 

* Bug fixes
    *   Large queries (> 300,000 results) would use up too much memory
    *   Context name was hardcoded to casda_vo_tools in some places
    *   Removed default use of syslog which resulted in errors logged on startup
    *   Unit tests failed if run outside CSIRO network
    *   Real and text columns were not mapped to VOTable types
    *   /tap/async endpoint now returns an empty job list rather than a 401 response

## v1.2 - 2016-04-28

* Initial open source release