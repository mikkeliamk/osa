<%@ include file="/layout/taglibs.jsp" %>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.mainHeader}"/>
	</s:layout-component>
	
    <s:layout-component name="content">
	    <div class="content-left-panel">
	    	<h1 class="largeheader"><fmt:message key="module.intro.title"/></h1>
	    	
	    	<%-- Parameter value is last update date, so no need to update translations, just the parameter value --%>
	    	<fmt:message key="module.intro.text"><fmt:param value="31.12.2014"/></fmt:message>
	    	
	    	<hr />
	    	
	    	<h2><fmt:message key="module.changelog.title"/></h2>
	    	<button id="toggleChangeLog" onclick="$('#changelog').toggle();"><fmt:message key="module.changelog.toggle"/></button>
	    	<br /><br />
	    	<div id="changelog" class="displayNone">
	    	    <b>31.12.2014</b>
                <ul>
                    <li>Ensimmäinen julkaisu</li>
                </ul>
	    	</div>
	    </div>
	   	    
    </s:layout-component>
</s:layout-render>
