<%--
- message.tag
-
- Copyright (C) 2012-2024 Rafael Corchuelo.
-
- In keeping with the traditional purpose of furthering education and research, it is
- the policy of the copyright owner to permit non-commercial use and redistribution of
- this software. It has been tested carefully, but it is not guaranteed for any particular
- purposes.  The copyright owner does not offer any warranties or representations, nor do
- they accept any liabilities with respect to them.
--%>

<%@tag body-content="empty" trimDirectiveWhitespaces="true"
	import="java.util.Collection, java.util.Map, java.util.HashMap, acme.client.helpers.JspHelper"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<%@attribute name="var" required="false" type="java.lang.String"%>
<%@attribute name="code" required="true" type="java.lang.String"%>
<%@attribute name="arguments" required="false" type="java.lang.String[]"%>

<jstl:choose>
	<jstl:when test="${var == null && !JspHelper.isBlank(code)}">
		<spring:message code="${code}" arguments="${arguments}" htmlEscape="true"/>
	</jstl:when>
	<jstl:when test="${var == null && JspHelper.isBlank(code)}">		
	</jstl:when>
	<jstl:when test="${var != null && !JspHelper.isBlank(code)}">
		<spring:message code="${code}" arguments="${arguments}" htmlEscape="true" var="${var}" scope="request"/>
	</jstl:when>
	<jstl:when test="${var != null && JspHelper.isBlank(code)}">
		<jstl:set var="__VAR__" value="${var}" scope="request"/>
		<% request.setAttribute((String)request.getAttribute("__VAR__"), ""); %>
	</jstl:when>
</jstl:choose>
