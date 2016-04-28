<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="https://daptst.csiro.au/dap/resources-2.6.6/css/style.css">
        <title>Data Link Basic UI</title>
        <style type="text/css">
/* Style overrides until we get a standard style. */
form .form-field label {
    font-weight: bold;
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
            <br/>
            <h1>
                Data Link Basic Functions Test Page
            </h1>
            <br/>
            <div id="capabilities">
                <h2>Availability</h2>
                <a href="/casda_vo_tools/datalink/availability">VO Tools Availability</a>
            </div>
            <br/>
            <div id="capabilities">
                <h2>Capabilities</h2>
                <a href="/casda_vo_tools/datalink/capabilities">VO Tools Capabilities</a>
            </div>
            <br/>
				<h2>Synchronous Query</h2>
                <form action="/casda_vo_tools/datalink/links" method="get">
                    <div class="form-field">
                        <label for="fileID">File ID</label>
                   		<input type="text" id="fileID">
                    </div>
                    <br/>
                    <div class="form-field">
                        <input type="submit" value="Submit">
                    </div>
                </form>
            <br/>
        </div>
        <div id="footer">
            <span class="copyright">Copyright (c) 2010-2014 CSIRO Australia. All Rights Reserved</span>
            <ul class="links footerLinks">
              <li><a href="https://daptst.csiro.au/dap/acknowledgements" target="dapLinks">Acknowledgements</a></li>
              <li><a href="https://daptst.csiro.au/dap/legal" target="dapLinks">Legal Notice and Disclaimer</a></li>
              <li><a href="https://daptst.csiro.au/dap/privacy" target="dapLinks">Privacy</a></li>
              <li><a href="https://daptst.csiro.au/dap/copyright" target="dapLinks">Copyright</a></li>
            </ul>
    		<br>
            <span class="serverInfo">Server Name: Localhost. Build Number: Someit... (30 Jul 2014)</span>
        </div>
    </body>
</html>
