<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Logout Page</title>
<link rel="stylesheet" type="text/css" href="${css}" />
<style>
tr.padBottom > td
{
  padding-bottom: 1em;
}
</style>
</head>
<body>
   	<jsp:include page="includes/header.jsp" />
<div id="main">
	<div class="meta" style="float: right; font-size: 15; padding-top:10px">
		<a  href="login">login</a>
	</div>
<h1 style="padding-top: 40px; padding-bottom: 20px">You have just logged out of the Casda VO Tools Configuration area</h1>

<h2 style="font-size:20px">Please close you browser to ensure the cache is cleared!</h2>

</div>
<jsp:include page="includes/footer.jsp" />
</body>
</html>