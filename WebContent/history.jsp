<%@ include file="/layout/taglibs.jsp" %>

<%-- Check if user logged in --%>
<c:if test="${empty user || user.anonymous == true}"><c:redirect url="/" /></c:if>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="${gui.subHeader}"/>
    </s:layout-component>
    
    <s:layout-component name="content">
        <s:messages/>
        <s:errors globalErrorsOnly="true"/>

        <s:form name="ingest" action="/Ingest.action">
        <s:useActionBean var="ingestaction" beanclass="fi.mamk.osa.stripes.IngestAction"/>
        <c:set var="pid" value="${ingestaction.getPid()}" />
        <c:set var="historyelements" value="${ingestaction.getHistoryElements()}" />

        <p><b><fmt:message key="message.showhistory"/> ${pid}</b></p>

        <div id="history">        
        <c:forEach items="${historyelements}" var="currentelement" varStatus="loop">
        <c:choose>
            <c:when test="${currentelement.name == 'date'}">
                <br><s:label for="${currentelement.name}">${currentelement.value}</s:label><br>
            </c:when>
            <c:when test="${currentelement.name == 'RELS_EXT'}">
                <s:label for="${currentelement.name}">${currentelement.name}</s:label><br>
                <br>
            </c:when>
            <c:otherwise>
                <s:submit name="showDatastream" value="${currentelement.name}" onclick="setLink('${currentelement.value}');"/>
                <s:hidden name="link" value="${currentelement.value}" id="link"/>
                <br>
            </c:otherwise>
        </c:choose>
        </c:forEach>
        </div>
                        
        </s:form>
    </s:layout-component>
</s:layout-render>

<script>
function setLink(link) {
    // set current link value to hidden variable
    window.document.getElementById("link").value = link;
}

$(function() {
	$("label#breadcrumb").html("<fmt:message key='link.history'/>");
	document.title = "<fmt:message key='link.history'/> | " + document.title;
});
</script>