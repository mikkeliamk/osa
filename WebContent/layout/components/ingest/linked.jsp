<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
		<div class="ingest-form-row">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<c:set var="source" value="${formElement.source}"/>
			<c:if test="${source != null && !empty source}"> <%-- Might be a redundant check, source should always be present with type 'linked'. Better be safe than sorry --%>
<%-- 				<c:choose> --%>
<%-- 					<c:when test="${source eq 'yso' || source eq 'sapo'}"> --%>
						<s:layout-render name="/layout/components/ingest/sub_components/onki_autocomplete.jsp" 
	                                     mdElement="${mdElement}" 
	                                     formElement="${formElement}" 
	                                     source="${formElement.source}" 
	                                     fieldname="${fieldname}"/>
<%-- 					</c:when> --%>
<%-- 				</c:choose> --%>
			</c:if>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}"/>
			<s:hidden name="${fieldname}[${mdElement}].multiValueType" value="${formElement.multivalue}"/>
		</div>
</s:layout-definition>
