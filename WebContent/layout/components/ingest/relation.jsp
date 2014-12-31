<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="relation"> --%>
		<div class="ingest-form-row">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<c:if test="${formElement.multivalue != 'multifield'}">
				<c:if test="${empty formElement.source}">
					<!-- relation name in visible field -->
					<s:text id="relationNAME_${mdElement}" name="browsename" value="${actionBean.getRelationName(mdElement)}" class="textrelation required-${formElement.required}"/>
		
					<!-- relation id in hidden field -->
					<s:hidden id="relationID_${mdElement}" name="${fieldname}[${mdElement}].value"/>
					
					<fmt:message key="button.browse" var="browse"/>
					<fmt:message key="capture.${mdElement}" var="mdElem"/>
					<button id="browse_${mdElement}" name="${mdElement}" class="browseBtn sub-form-button" title="${browse} '${fn:toLowerCase(mdElem)}'">...</button>
					<input type="hidden" id="${mdElement}" value="${mdElement}" />
				</c:if>
				<c:if test="${!empty formElement.source}">
					<s:layout-render name="/layout/components/ingest/sub_components/onki_autocomplete.jsp" 
                                     mdElement="${mdElement}" 
                                     formElement="${formElement}" 
                                     source="${formElement.source}" 
                                     fieldname="${fieldname}"/>
				</c:if>
			</c:if>
			<c:if test="${formElement.multivalue == 'multifield'}">
                <s:layout-render name="/layout/components/ingest/sub_components/multifield.jsp" 
                                mdElement="${mdElement}" 
                                formElement="${formElement}" 
                                fieldname="${fieldname}"/>
			</c:if>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}" />
		</div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
