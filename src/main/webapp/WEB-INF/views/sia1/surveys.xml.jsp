<%@ page contentType="application/xml"%><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<Surveys>
<c:forEach var = "survey" items="${surveys}">
	<Survey>
		<Code>${survey.code}</Code>
		<Name>${survey.name}</Name>
		<Description>${survey.description}</Description>
		<Group><c:choose><c:when test="${empty survey.group}">${survey.name}</c:when><c:otherwise>${survey.group}</c:otherwise></c:choose></Group>
		<Endpoint>${survey.endpoint}</Endpoint>
	</Survey>
</c:forEach>
</Surveys>
