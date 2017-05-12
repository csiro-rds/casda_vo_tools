<%@ page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<vosi:capabilities
    xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0"
    xmlns:ssap="http://www.ivoa.net/xml/SSA/v1.1"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1"
    xsi:schemaLocation="http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/SSA/v1.1 http://www.ivoa.net/xml/SSA/v1.1 http://www.ivoa.net/xml/VODataService/v1.1 http://www.ivoa.net/xml/VODataService/v1.1">
    <capability standardID="ivo://ivoa.net/std/VOSI#capabilities">
        <interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
            <accessURL use="full">
                ${capabilitiesURL}
            </accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
        </interface>
    </capability>
	<capability standardID="ivo://ivoa.net/std/VOSI#availability">
    	<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
	       <accessURL use="full">
                ${availabilityURL}
	       </accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
		</interface>
	</capability>
	<capability standardID="ivo://ivoa.net/std/SSA" xsi:type="ssap:SimpleSpectralAccess"> 
		<interface role="std" version="1.0" xsi:type="vs:ParamHTTP">
			<accessURL use="base">${ssapURL}</accessURL>
			<queryType>GET</queryType>
		</interface>
		<complianceLevel>minimal</complianceLevel>
		<dataSource>pointed</dataSource>
		<creationType>archival</creationType>
		<supportedFrame>FK5</supportedFrame>
		<supportedFrame>ICRS</supportedFrame>
		<supportedFrame>GALACTIC-II</supportedFrame>
		<maxRecords>${outputLimit.hard}</maxRecords>
		<defaultMaxRecords>${max.records}</defaultMaxRecords>
		<testQuery>
			<queryDataCmd>MAXREC=1&amp;REQUEST=queryData</queryDataCmd>
		</testQuery>
	</capability>
</vosi:capabilities>
