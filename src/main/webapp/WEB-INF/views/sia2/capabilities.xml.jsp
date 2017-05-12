<%@ page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<vosi:capabilities
    xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0"
    xmlns:sia="http://www.ivoa.net/xml/SIA/v1.1"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1"
    xsi:schemaLocation="http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/SIA/v1.1 http://www.ivoa.net/xml/SIA/v1.1 http://www.ivoa.net/xml/VODataService/v1.1 http://www.ivoa.net/xml/VODataService/v1.1">
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
	<capability standardID="ivo://ivoa.net/std/SIA#query-2.0" xsi:type="sia:SimpleImageAccess"> 
		<interface xsi:type="vs:ParamHTTP" version="2.0" role="std">
			<accessURL use="base">${siapURL}</accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
		</interface>
		<imageServiceType>Pointed</imageServiceType>
		<maxImageExtent>
			<long>10</long>
			<lat>10</lat>
		</maxImageExtent>
		<testQuery>
			<pos>
				<long>343.770</long>
				<lat>-8.737</lat>
			</pos>
			<size>
				<long>0.1</long>
				<lat>0.1</lat>
			</size>
			<extras>FACILITY=BETA</extras>
		</testQuery>
	</capability>
</vosi:capabilities>
