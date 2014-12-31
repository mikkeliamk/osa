<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="relation"> --%>
		<div class="view-form-row-container">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<c:if test="${formElement.multivalue != 'multifield'}">
                <span>${actionBean.getRelationName(mdElement)}</span>
			</c:if>
			<c:if test="${formElement.multivalue == 'multifield'}">
                <s:layout-render name="/layout/components/view_only/sub_components/multifield.jsp" 
                                mdElement="${mdElement}" 
                                formElement="${formElement}" 
                                fieldname="${fieldname}" 
                                document="${document}" />
			</c:if>
		</div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
