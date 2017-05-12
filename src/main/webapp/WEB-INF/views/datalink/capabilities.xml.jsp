<%@ page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<vosi:capabilities
    xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1"
    xsi:schemaLocation="http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VODataService/v1.1 http://www.ivoa.net/xml/VODataService/v1.1">
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
	<capability standardID="ivo://ivoa.net/std/DataLink#links-1.0" > 
		<interface version="1.0" xsi:type="vs:ParamHTTP" role="std">
			<accessURL use="base">${datalinkURL}</accessURL>
			<queryType>GET</queryType>
			<queryType>POST</queryType>
			<resultType>application/x-votable+xml;content=datalink</resultType>
			<param use="required" std="true">
				<name>ID</name>
				<description>publisher dataset identifier</description>
				<ucd>meta.id;meta.main</ucd>
				<dataType>string</dataType>
			</param>
		</interface>
	</capability>
</vosi:capabilities>
