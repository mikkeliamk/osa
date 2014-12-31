<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="textfield"> --%>
		<div class="ingest-form-row">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<c:choose>
	           	<c:when test="${mdElement == 'pid'}">
	                <s:text name="pid" value="${formElement.value}" class="textsearch required-${formElement.required}" readonly="${formElement.readonly}"/>
	            </c:when>
	            <c:otherwise>
	                <c:choose>
	                    <c:when test="${formElement.multivalue == 'delimited'}">
                            <c:if test="${empty formElement.source}">
		                        <s:text name="${fieldname}[${mdElement}].value" 
	 	                                value="${actionBean.getDelimitedMultivalue(mdElement)}"
	 	                                class="textsearch required-${formElement.required}" 
	 	                                readonly="${formElement.readonly}"/>
                            </c:if>
                           
                            <c:if test="${!empty formElement.source}">
                                <s:layout-render name="/layout/components/ingest/sub_components/onki_autocomplete.jsp" 
                                                mdElement="${mdElement}" 
                                                formElement="${formElement}" 
                                                source="${formElement.source}" 
                                                fieldname="${fieldname}"/>
                            </c:if>
                          
		                </c:when>
			            <c:when test="${formElement.multivalue == 'multifield'}">
                            <s:layout-render name="/layout/components/ingest/sub_components/multifield.jsp" 
                                            mdElement="${mdElement}" 
                                            formElement="${formElement}" 
                                            fieldname="${fieldname}"/>
			            </c:when>
    	                <c:otherwise>
			                <sd:text name="${fieldname}[${mdElement}].value" class="textsearch required-${formElement.required}" readonly="${formElement.readonly}"/>
		                </c:otherwise>
	                </c:choose>
	                    <s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}" />
	                    <s:hidden name="${fieldname}[${mdElement}].multivalueType" value="${formElement.multivalue}" />
	                    <s:hidden name="${fieldname}[${mdElement}].generated" value="${formElement.generated}" />
	            </c:otherwise>
	        </c:choose>
        </div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>