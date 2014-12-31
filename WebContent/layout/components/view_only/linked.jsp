<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="linked">
		<div class="view-form-row-container">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<div class="multifield-container">
				<c:forEach items="${document[mdElement].nestedElements}" var="ontoTerm" varStatus="loop">
	        		<c:if test="${ontoTerm != null}">
	       				<span class="onki_selected_term ellipsis" title="${ontoTerm['name'].value}">
	       					<c:if test="${fn:length(document[mdElement].nestedElements) >= 1}">
	       						${ontoTerm['name'].value}<c:if test="${!loop.last}"><b>,</b></c:if> 
	       					</c:if>
	       					<c:if test="${fn:length(document[mdElement].nestedElements) < 1}">
	       						${ontoTerm['name'].value}
	       					</c:if>
	       				</span>
	        		</c:if>
	        	</c:forEach>
        	</div>
		</div>
	</s:layout-component>
</s:layout-definition>
