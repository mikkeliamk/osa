<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="select"> --%>
		<fmt:message key="ingest.${pageIndex}" var="typeSelectLabel"/> <!-- Could not use fmt message-tag as an attribute for input fields, workaround by using ${typeSelectLabel} -->
		<fmt:message key="ingest.${pageIndex}.value" var="typeSelectValue"/>
		<div class="ingest-form-row">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<c:choose>
		        <c:when test="${mdElement == 'type'}">
		            <s:select name="${fieldname}[${mdElement}].value" class="selectsearch required-${formElement.required}" disabled="disabled">
		                <s:option value="${typeSelectValue}">${typeSelectLabel}</s:option>
		            </s:select>
		        </c:when>
		        <c:otherwise>
                    <c:choose>
	                    <c:when test="${formElement.multivalue != 'multifield'}">
				            <s:select name="${fieldname}[${mdElement}].value" class="selectsearch required-${formElement.required}">
				                <c:if test="${mdElement != 'accessRights'}">
				                    <s:option value=""><fmt:message key="general.default"/></s:option>
				                </c:if>
				                <c:forEach items="${formElement.enumValues}" var="enumValue">
				                    <s:option value="${enumValue}"><fmt:message key="capture.${mdElement}.${enumValue}"/></s:option>
				                </c:forEach>
				            </s:select>
                        </c:when>
			            <c:otherwise>
	                        <s:layout-render name="/layout/components/ingest/sub_components/multifield.jsp" 
	                                mdElement="${mdElement}" 
	                                formElement="${formElement}" 
	                                fieldname="${fieldname}"/>
                        </c:otherwise>
		            </c:choose>
		        </c:otherwise>
		    </c:choose>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}" />
	    </div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
