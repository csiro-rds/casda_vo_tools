<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="${css}">
        <title>TAP Basic UI</title>
        <style type="text/css">
/* Style overrides until we get a standard style. */
form .form-field label {
    font-weight: bold;
}
        </style>
        <c:url value="/tap" var="tapBaseUrl" />`
    </head>
    <body>
        <jsp:include page="../includes/header.jsp" />
        <div id="main">
            <br/>
            <h1>
                TAP Basic Functions Test Page
            </h1>
            <br/>
            <div id="capabilities">
                <h2>Availability</h2>
                <a href="${tapBaseUrl}/availability">VO Tools Availability</a>
            </div>
            <br/>
            <div id="capabilities">
                <h2>Capabilities</h2>
                <a href="${tapBaseUrl}/capabilities">VO Tools Capabilities</a>
            </div>
            <br/>
            <div id="synchronous_query">
                <h2>Synchronous Query</h2>
                <form action="${tapBaseUrl}/sync" method="get">
                    <input type="hidden" name="request" value="doQuery"/>
                    <input type="hidden" name="lang" value="ADQL"/>
                    <div class="form-field">
                        <label for="format">Format</label>
                        <select id="format" name="format">
                            <c:forEach items="${outputFormats}" var="outputFormat" varStatus="loop">
                            <option value="${outputFormat}"${loop.index == 0 ? 'selected="selected"' : ''}>${outputFormat}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-field">
                        <label for="query">Query</label>
                        <textarea id="query" name="query" rows="3" cols="60" placeholder="Please enter an ADQL query and click submit"></textarea>
                    </div>
                    <div class="form-field">
                        <input type="submit" value="Submit">
                    </div>
                </form>
            </div>
            <br/>
        </div>
		<jsp:include page="../includes/footer.jsp" />
    </body>
</html>
