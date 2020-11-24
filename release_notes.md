# CASDA VO Tools Release Notes

## v1.13 - 2020-11-12

* Bug fixes
    * Correct output of large calculated numeric values


## v1.12 - 2020-07-09

* Bug fixes
    * Correct error in tap_tables config preventing new installations


## v1.11 - 2019-07-30

* New Features
    * Add support for ObsCore v1.1
    * Add support for embargoing table contents using a released_date in the TAP table metadata
    * Allow config location to be specified
    * Update default postgres driver to v42.2.5
    * Improve logging of connection health


## v1.10 - 2018-03-08

* New Features
    *  Added new logging when SSAP and SIA2 queries were invalid or could not be processed 
    * Add a large web download limit for DataLink

* Bug fixes
    * Configure actions failed if a trailing slash was used on configure/home


## v1.9 - 2018-01-24

* Bug fixes
    * Add a specific error message is RA and Dec fields not specified via UCD for tables with cone search enabled
    * Tidied up datalink XML to improve standards compliance
    * Correct the type in /tables and query column metadata for timestamp columns


## v1.8 - 2017-10-05

* New Features
    *   TAP - Support the /tap/examples endpoint with configurable examples or preprepared page

* Bug fixes
    *   Confg - Fixed error when adding a table that had a foreign key to table that was not also being added

## v1.7 - 2017-06-29

* New Features
    *   Output s_region field in Aladin compatible format	

* Bug fixes
    *   TAP - Allow multiple tables to have the same column name

## v1.6 - 2016-10-28

* New Features
    *   Complete SSAP support for discovering and downloading spectra
	*   Allow deployers to provide a custom XSL stylesheet for votable results	

* Bug fixes
    *   SIAP - error.txt could not be downloaded if only file in results

## v1.5 - 2016-12-15

* New Features
    *   Initial support for SSAP - spectra discovery

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