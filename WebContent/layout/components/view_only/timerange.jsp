<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="timerange">
		<div class="view-form-row-container">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
            <div class="temporal-range-container displayInlineBlock">
                <c:forEach items="${formElement.nestedFormElements}" var="nestedChild" varStatus="loop">
                    ${document[nestedChild.name].value}
                    ${(loop.index == 0 && !empty document[nestedChild.name]) ? ' &minus; ' : ''}
                </c:forEach>
            </div>
		</div>
	</s:layout-component>
</s:layout-definition>
