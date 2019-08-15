<%@ page contentType="text/html; charset=ISO-8859-1"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01
Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="${css}" />
		<title>TAP Examples</title>
		<style type="text/css">
		/* Style overrides until we get a standard style. */
		form .form-field label {
			font-weight: bold;
		}
		</style>
	</head>
	<body vocab="http://www.ivoa.net/rdf/examples#">
		<jsp:include page="../includes/header.jsp">
			<jsp:param name="addressPrefix" value="../" />
		</jsp:include>
		<div id="main">
			<br/>
			<h1>TAP Examples</h1>
			<!-- Error when no examples exist -->
			<c:if test="${not empty error}">
				<br/>
				<h1 style="color:red">${error}</h1>
			</c:if>
			<c:forEach items="${examples}" var="example" varStatus="iter">
				<div typeof="example" id="${example.id}" resource="#${example.id}" >
					<!-- Query Name, Mandatory Field -->
					<h2 property="name">${example.name}</h2>
	
					<!-- Description, Non-Mandatory -->
					<c:if test="${not empty example.description}">
						<p>${example.description}</p>
					</c:if>
	
					<!-- Tables List, Optional List of Table names that pertain to the query -->
					<c:if test="${not empty example.tables}">
						<h4 style="display:inline">Tables:</h4>
						<c:forEach items="${example.tables}" var="table" varStatus="i">
							<div style="display:inline" property="table">${table}</div><c:if test="${not i.last}">,</c:if>
						</c:forEach>
					</c:if>
					<br/>
					<br/>

					<!-- ADQL Query, Mandatory Field -->
					<h4>Query:</h4><br/>
					<div style="background: #E8E8E8; padding:10px" property="query">${example.query}</div>
					<br/> 
					<br/>
					<br/>
				</div>
			</c:forEach>
		</div>
		<jsp:include page="../includes/footer.jsp" />
	</body>
</html>
