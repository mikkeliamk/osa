<%@ page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%-- Use s-prefix tags for normal fields,
	 sd-prefix for fields where you need HTML5 attributes/other attributes not supported by s-prefix tags.
	 Custom attributes starting with data- are valid HTML5 markup.
 --%>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="sd" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="osa" uri="/WEB-INF/osa.tld" %>

<%-- Specifies the base name of the resource bundle that is to be loaded. --%>
<fmt:setBundle basename="fi.mamk.osa.core.messages"/>

<%-- Specifies a two-part code that represents language code and country code. --%>
<fmt:setLocale value="${user.locale}" scope="session" />
