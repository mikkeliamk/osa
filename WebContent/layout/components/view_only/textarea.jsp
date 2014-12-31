<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="textarea">
		<div class="view-form-row-container">
			<s:label for="${mdElement}" class="verticalAlignTop"><fmt:message key="capture.${mdElement}"/></s:label>
			<div class="textarea-div"><c:out value="${document[mdElement].value}"/></div>
		</div>
	</s:layout-component>
</s:layout-definition>
