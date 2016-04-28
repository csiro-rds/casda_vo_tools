<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Error occurred
</h1>

<P>  Oh dear! </P>

<jsp:useBean id="timestamp" class="java.util.Date" scope="page" />

<c:set var="message" value="Unknown"/>
<c:if test="${exception ne null}">
	<c:set var="message" value="${exception}"/>
</c:if>
<p> Error occurred at <fmt:formatDate value="${timestamp}" pattern="dd/MM/yyyy hh:mm:ss:mmmm" />: 
<c:out value="${message}"/> </p>

<p> Please contact the CASDA IT Support team if you require assistance with this. </p>

</body>
</html>
