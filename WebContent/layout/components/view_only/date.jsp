<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="date">
		<div class="view-form-row-container">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span><c:out value="${document[mdElement].value}"/></span>
		</div>
	</s:layout-component>
</s:layout-definition>
 