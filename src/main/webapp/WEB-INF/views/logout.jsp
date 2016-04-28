<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Logout Page</title>
<link rel="stylesheet" type="text/css" href="https://daptst.csiro.au/dap/resources-2.6.6/css/style.css" />
<style>
tr.padBottom > td
{
  padding-bottom: 1em;
}
</style>
</head>
<body>
   	<div id="header">
		<a href="/casda_vo_tools/tap" title="CASDA VO Tools Home"><img class="headerLogoImage" src="https://daptst.csiro.au/dap/resources-2.6.6/images/csiro_logo.png" alt="CSIRO Logo"></a>
		<span class="banner">CASDA VO Tools</span>
		<div style="position: relative;"><span class="envBanner">Local Environment</span></div>
	</div>
<div id="main">
	<div class="meta" style="float: right; font-size: 15; padding-top:10px">
		<a  href="../casda_vo_tools/login">login</a>
	</div>
<h1 style="padding-top: 40px; padding-bottom: 20px">You have just logged out of the Casda VO Tools Configuration area</h1>

<h2 style="font-size:20px">Please close you browser to ensure the cache is cleared!</h2>

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