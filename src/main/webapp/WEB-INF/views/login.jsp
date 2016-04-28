<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Login Page</title>
<link rel="stylesheet" type="text/css" href="https://daptst.csiro.au/dap/resources-2.6.6/css/style.css" />
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
   	<div id="header">
		<a href="/casda_vo_tools/tap" title="CASDA VO Tools Home"><img class="headerLogoImage" src="https://daptst.csiro.au/dap/resources-2.6.6/images/csiro_logo.png" alt="CSIRO Logo"></a>
		<span class="banner">CASDA VO Tools</span>
		<div style="position: relative;"><span class="envBanner">Local Environment</span></div>
	</div>
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
<div id="footer">
  <span class="copyright">Copyright (c) 2010-2015 CSIRO Australia. All Rights Reserved</span>
  <ul class="links footerLinks">
    <li><a href="https://daptst.csiro.au/dap/acknowledgements" target="dapLinks">Acknowledgements</a></li>
    <li><a href="https://daptst.csiro.au/dap/legal" target="dapLinks">Legal Notice and Disclaimer</a></li>
    <li><a href="https://daptst.csiro.au/dap/privacy" target="dapLinks">Privacy</a></li>
    <li><a href="https://daptst.csiro.au/dap/copyright" target="dapLinks">Copyright</a></li>
  </ul>
	<br>
</div>
</body>
</html>