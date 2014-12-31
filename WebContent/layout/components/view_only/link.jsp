<%@ include file="/layout/taglibs.jsp" %>
<s:useActionBean var="ingestaction" beanclass="fi.mamk.osa.stripes.IngestAction"/>
<s:layout-definition>
	<s:layout-component name="link">
		<p>
            <c:set var="childListSize" value=""/>
            <c:if test="${mdElement == 'groups' || mdElement == 'content'}">
                <c:set var="childList" value="${ingestaction.listChildren(pid)}"/>
                <c:set var="childListSize" value="(${fn:length(childList)})"/>
            </c:if>
            
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/> ${childListSize}</s:label>
			
			<c:choose>
			    <c:when test="${mdElement == 'attachment'}">
			        <c:if test="${!empty pid}">
			        	<div id="att-thumbnail-container" class="object-container"></div>
			        </c:if> 
			    </c:when>
			    <c:when test="${mdElement == 'groups' || mdElement == 'content'}">
			        <c:if test="${!empty pid}">
                        <c:set var="childList" value="${ingestaction.listChildren(pid)}"/>
			            <c:if test="${!empty childList}">
			                <div id="childobjects" class="object-container">
			                    <c:forEach items="${childList}" var="child">
			                        <s:link href="Ingest.action?pid=${child.pid}&view="><img src="img/icons/silk/${child.imagename}" class="smallicon" alt="${child.type}"/> ${child.name}</s:link><br/>
			                    </c:forEach> 
			                </div>
			            </c:if>
			            <c:if test="${empty childList}">
			                <label><fmt:message key="header.no${mdElement}found"/></label>
			            </c:if>
			        </c:if>
			    </c:when>
			</c:choose>
		</p>
	</s:layout-component>
</s:layout-definition>