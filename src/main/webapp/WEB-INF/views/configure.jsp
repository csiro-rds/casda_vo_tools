<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="${css }">
        <title>VO Tools Configuration</title>
        <style type="text/css">
/* Style overrides until we get a standard style. */
form .form-field label {
    font-weight: bold;
}
div .success{
color: green;
font-size: 14;
padding-bottom: 7px;
font-weight: bold;
}
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
    <body>
        <jsp:include page="includes/header.jsp" >
        	<jsp:param name="addressPrefix" value="../"/>
        </jsp:include>
        <div id="main">
            <br/>
            <div class="meta" style="float: right; font-size: 15;">
				<a  href="../logout">logout</a>
            </div>
            <h1>
                VO Tools Configuration Page
            </h1>
            <br/>
            <div id="configuration">
            	<c:if test="${!passwordSetup}">
                	<h2>Configuration text in YAML format</h2>
               	</c:if>
           		<c:if test="${passwordSetup}">
           			<h2>Please enter the Administrator password for this instance of CASDA VO Tools</h2>
           			<c:if test="${passwordError}">
           				<h4 style="color: red; font-weight: bold; font-size:15px">Please choose a password at least 8 characters long which contains one upper case letter,<br/> one lower case letter, one number and a punctuation character</h4>
           			</c:if>
           			<c:if test="${!passwordError}">
           				<h4>Please choose a password at least 8 characters long which contains one upper case letter,<br/> one lower case letter, one number and a punctuation character</h4>
           			</c:if>
           			<c:if test="${ioError}">
           				<h4 style="color: red">The password could not be saved to file, please check the logs and the permissions of your server.</h4>
           			</c:if>
           			<br/>
           		</c:if>
           		<c:if test="${success}">
                	<div id="successMessage" class="success">${successMessage }</div>
                </c:if>
                <form action="${pageContext.request.contextPath}/configure/act" method="post">
                    <div class="form-field" style="width:800">
	                    <c:if test="${!passwordSetup}">
	                      <table>
	                        <tr>
	                          <td>
	                            <textarea id="config" name="config" rows="40" cols="100" placeholder="Please enter configuration in YAML format">${config}</textarea>
	                          </td>
	                          <td style="vertical-align:top;">
	                            <button style="margin-left:20px" class="button-link" type="submit" name="submit" value="CURRENT">Current</button>
	                            <button style="margin-top:10px;margin-left:20px" class="button-link" name="submit" type="submit" value="EXPLORE">Explore</button>
	                            <button style="margin-top:10px;margin-bottom:10px;margin-left:20px" class="button-link" name="submit" type="submit" value="APPLY">Apply</button>
	                            <div style="margin-top:10px;margin-left:25px">Allowed change level</div>
	                            <select style="margin-top:10px;margin-left:40px" name="changeLevel">
	                               <option value="NONE" SELECTED>None</option>
	                               <option value="UPDATE">Update</option>
	                               <option value="REINDEX">Reindex</option>
	                               <option value="DROP">Drop</option>
	                            </select> 
	                          </td>
	                        </tr>
	                      </table>
	                     </c:if>
	                     <c:if test="${passwordSetup}">
	                     	<table>
								<tr class="padBottom">
									<td>Password:</td>
									<td><input type="password" name="password1" id="password1"/></td>
								</tr>
								<tr class="padBottom">
									<td>Confirm Password:</td>
									<td><input type="password" name="password2" id="password2"/></td>				
								</tr>
								<tr >
									<td colspan='2' align="center">
										<input name="submit" type="submit" value="Save Password" />
									</td>
								</tr>
							</table>
	                     </c:if>
	                   </div>
                </form>
            </div>
            <br/>
        </div>
        <jsp:include page="includes/footer.jsp" />
    </body>
</html>
