<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="units">
		<p>
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<c:if test="${mdElement == 'extent'}">
				<span>${document[mdElement].value} ${document[mdElement].value}</span>
			</c:if>
		</p>
	</s:layout-component>
</s:layout-definition>