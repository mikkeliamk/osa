<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="select">
		<div class="view-form-row-container">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<c:choose>
		        <c:when test="${mdElement == 'type'}">
                    <span><fmt:message key="ingest.${pageIndex}"/></span>
		        </c:when>
		        <c:otherwise>
                    <c:if test="${mdElement != 'accessRights'}">
                        <span>
                            <c:if test="${!empty document[mdElement].value}">
                                <fmt:message key="capture.${mdElement}.${document[mdElement].value}"/>
                            </c:if>
                        </span>
                    </c:if>
                    <c:if test="${mdElement == 'accessRights'}">
                        <span>
                            <c:if test="${!empty document[mdElement].value}">
                                <fmt:message key="capture.accessRights.${document[mdElement].value}"/>
                            </c:if>
                        </span>
                    </c:if>
		        </c:otherwise>
		    </c:choose>
	    </div>
	</s:layout-component>
</s:layout-definition>
