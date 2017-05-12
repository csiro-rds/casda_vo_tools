<%@ page contentType="application/xml" %><?xml version="1.0" encoding="UTF-8"?>
<vosi:capabilities
    xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0"
    xmlns:tap="http://www.ivoa.net/xml/TAPRegExt/v1.0"
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
	<capability standardID="ivo://ivoa.net/std/VOSI#tables">
		<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
			<accessURL use="full">
				${tablesURL}
			</accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
		</interface>
	</capability>
	<capability standardID="ivo://ivoa.net/std/TAP" xsi:type="tap:TableAccess"> 
		<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
			<accessURL use="base">${tapURL}</accessURL>
			<queryType>GET</queryType>
			<resultType>application/xml</resultType>
		</interface>
		<dataModel ivo-id="ivo://ivoa.net/std/ObsCore/v1.0">ObsCore 1.0</dataModel>
		<language>
			<name>${languageName}</name>
			<version ivo-id="ivo://ivoa.net/std/${languageName}#v${languageVersion}">${languageVersion}</version>
			<description>${languageDesc}</description>
		</language>
		<outputFormat>
			<mime>${outputFormatMime}</mime>
			<alias>${outputFormatAlias}</alias>
		</outputFormat>
		<retentionPeriod>
			<default>${retentionPeriodDefault}</default>
			<hard>${retentionPeriodHard}</hard>
		</retentionPeriod>
		<executionDuration>
			<default>${execDurationDefault}</default>
			<hard>${execDurationHard}</hard>
		</executionDuration>
		<outputLimit>
			<hard unit="row">${outputLimitHard}</hard>
		</outputLimit>
	</capability>
</vosi:capabilities>
