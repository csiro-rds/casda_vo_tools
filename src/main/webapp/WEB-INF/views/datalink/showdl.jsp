<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="${css}">
        <title>Data Link Basic UI</title>
        <style type="text/css">
/* Style overrides until we get a standard style. */
form .form-field label {
    font-weight: bold;
}
        </style>
    </head>
    <body>
        <jsp:include page="../includes/header.jsp" />
        <div id="main">
            <br/>
            <h1>
                Data Link Basic Functions Test Page
            </h1>
            <br/>
            <div id="capabilities">
                <h2>Availability</h2>
                <a href="datalink/availability">VO Tools Availability</a>
            </div>
            <br/>
            <div id="capabilities">
                <h2>Capabilities</h2>
                <a href="datalink/capabilities">VO Tools Capabilities</a>
            </div>
            <br/>
				<h2>Synchronous Query</h2>
                <form action="datalink/links" method="get">
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
        <jsp:include page="../includes/footer.jsp" />
    </body>
</html>
