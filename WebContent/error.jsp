<%@ include file="/layout/taglibs.jsp" %>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="${gui.subHeader}"/>
    </s:layout-component>
    
    <s:layout-component name="html_head">
        <script>
	        $(function(){
	            $("label#breadcrumb").html("<fmt:message key='header.error.500'/>");
	        });
        </script>
    </s:layout-component>
    
    <s:layout-component name="content">
	    <h1 class="largeheader"><fmt:message key="header.error.500"/></h1><div class="header-hr"></div>
	    <h2><fmt:message key="message.error.500"/></h2>
	    ${param.exception}
    </s:layout-component>
</s:layout-render>