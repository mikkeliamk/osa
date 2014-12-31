<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="textfield">
		<div class="view-form-row-container">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
            <c:choose>
                <c:when test="${formElement.multivalue == 'delimited'}">
					<span><c:out value="${actionBean.getDelimitedMultivalue(mdElement)}"/></span>
             	</c:when>
                <c:otherwise>
                    <span id="${mdElement}"><c:out value="${document[mdElement].value}"/></span>
             	</c:otherwise>
            </c:choose>
        </div>
	</s:layout-component>
</s:layout-definition>