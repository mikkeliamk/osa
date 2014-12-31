<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
    <s:layout-component name="nested">
        <!-- Create the first instance of nested element fields and a button for more instances. -->
        <div class="view-form-row-container">
            <s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
            <div class="nested-element-container" id="${mdElement}-div">
	            
                <c:set var="nestedChildren" value="${actionBean.getNestedChildren(mdElement)}"/>
	            <c:set var="nestedElements" value="${formElement.nestedFormElements}"/>
	            
	            <c:set var="multifieldClass" value="${(formElement.multivalue == 'multifield') ? 'multifieldElem' : ''}" />
	            
	            <c:if test="${nestedChildren != null}">
	                <c:forEach items="${nestedChildren}" var="nestedChild" varStatus="loop">
		                <div class="nestedElement ${multifieldClass}">
		                    <c:forEach items="${nestedElements}" var="nestedElement">
                                <%-- Wrap each element inside p-tags if nesting style is multirow --%>
			                    ${(formElement.nestingStyle == 'multirow') ? '<p>' : ''}
				                    <c:if test="${nestedElement.fieldType == 'relation'}">
			                           ${actionBean.getNestedRelationName(mdElement, nestedElement.name, loop.index)}
				                    </c:if>
				                    <c:if test="${nestedElement.fieldType == 'select'}">
			                           <fmt:message key="capture.${nestedElement.name}.${document[mdElement].nestedElements[loop.index][nestedElement.name].value}" />
				                    </c:if>
				                    <c:if test="${nestedElement.fieldType == 'textfield'}">
			                           ${document[mdElement].nestedElements[loop.index][nestedElement.name].value}
				                    </c:if>
			                    ${(formElement.nestingStyle == 'multirow') ? '</p>' : ''}
		                    </c:forEach>
		                </div>
	                </c:forEach>
	            </c:if>
	        <div class="clearfix"></div>
	        </div>
        </div>
    </s:layout-component>
</s:layout-definition>
