<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Login Page</title>
<link rel="stylesheet" type="text/css" href="${css}" />
<style>
tr.padBottom > td
{
  padding-bottom: 1em;
}
input.centreButton
{
	align: center;
}
</style>
</head>
<body onload='document.f.username.focus();'>
	<jsp:include page="includes/header.jsp" />
<div id="main">

<h2 style="font-size:20px; padding-top: 15px">Login with Username and Password</h2>	
<%if (request.getParameter("error") != null) {%>
		<div> 
			<font color="red">
				Your login attempt was not successful, try again.
				<br>
				Invalid username and password.
			</font>
		</div>
		<br>
<%}%>
	
	<form name='f' action='login' method='POST'>
		<table>
			<tr class="padBottom">
				<td>User:</td>
				<td><input type='text' name='username' value=''></td>
			</tr>
			<tr class="padBottom">
				<td>Password:</td>
				<td><input type='password' name='password' /></td>				
			</tr>
			<tr >
				<td colspan='2' align="center">
					<input name="submit" type="submit" value="Login" />
				</td>
			</tr>
		</table>
	</form>
</div>
<jsp:include page="includes/footer.jsp" />
</body>
</html>