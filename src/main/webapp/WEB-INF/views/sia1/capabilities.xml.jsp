<%@ page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
<c:forEach items="${siaSurveys}" var="item">
	<capability standardID="ivo://ivoa.net/std/SIA">
		<description>${item[1]}</description>
		<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
			<accessURL use="base">${item[0]}</accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
		</interface>
			
		<imageServiceType>Cutout</imageServiceType>
		<maxImageExtent>
			<long>2</long>
			<lat>2</lat>
		</maxImageExtent>
		<testQuery>
			<pos>
				<long>343.770</long>
				<lat>-34.737</lat>
			</pos>
			<size>
				<long>0.1</long>
				<lat>0.1</lat>
			</size>
		</testQuery>
	</capability>
</c:forEach>	
</vosi:capabilities>
