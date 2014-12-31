<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
	<s:layout-component name="nested">
		<!-- Create the first instance of nested element fields and a button for more instances. -->
		<div class="nested-element-container" id="${mdElement}-div">
			<s:label for="${mdElement}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}" class="${formElement.multivalue == 'multifield' ? 'verticalAlignTop' : ''}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<%-- For browse buttons' titles --%>
			<fmt:message key="button.browse" var="browse"/>
			<fmt:message key="capture.${mdElement}" var="mdElem"/>
			
			<c:if test="${pid != null}">
                <c:set var="nestedChildren" value="${actionBean.getNestedChildren(mdElement)}"/>
			</c:if>
			<c:set var="nestedElements" value="${formElement.nestedFormElements}"/>
			
			<c:set var="nestStyleClass" value="${(formElement.nestingStyle == 'singlerow') ? 'singlerow-nest' : ''}" />
			<c:set var="multifieldClass" value="${(formElement.multivalue == 'multifield') ? 'multifieldElem' : ''}" />
			
			<%-- TODO   Try to implement solution that does not use copied logic, i.e don't copy-paste forEach loops --%>
            <c:if test="${nestedChildren != null}">
				<c:forEach items="${nestedChildren}" var="nestedChild" varStatus="loop">
				<div class="nestedElement positionRelative ${nestStyleClass} ${multifieldClass}">
				    <%-- Only draw delete button if element is multifield --%>
					<c:if test="${formElement.multivalue == 'multifield'}">
                        <fmt:message key="button.delete" var="title"/>
                        <img src="img/icons/silk/cancel.png" class="helpicon pointerCursor delete-nested-element" name="${mdElement}" title="${title}"/>
					</c:if>
					
					<c:forEach items="${nestedElements}" var="nestedElement">
                        <%-- Wrap each element inside p-tags if nesting style is multirow --%>
						${(formElement.nestingStyle == 'multirow') ? '<p>' : ''}
							<c:if test="${nestedElement.fieldType == 'relation'}">
			                    <fmt:message key="capture.${nestedElement.name}" var="placeholder"/>
								<c:set var="relationname" value="${nestedElement.name}" />
								
								<!-- relation name in visible field -->
								<sd:text id="relationNAME_${nestedElement.name}${loop.index}" 
							             name="browsename" 
							             value="${actionBean.getNestedRelationName(mdElement, relationname, loop.index)}" 
									     class="nestedInput textrelation required-${nestedElement.required}" 
									     placeholder="${placeholder}" />
									     
								<!-- Relation pid in hidden field, this is what goes to fedora/solr -->
								<s:hidden id="relationID_${nestedElement.name}${loop.index}" 
								          class="nestedInput" 
								          name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value" />
							    
							    <s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />
							    <button id="browse_${nestedElement.name}${loop.index}" name="${nestedElement.name}" class="browseBtn sub-form-button" title="${browse} '${fn:toLowerCase(mdElem)}'">...</button>
							    <input type="hidden" id="${nestedElement.name}" value="${mdElement}" /> 
							</c:if>
							<c:if test="${nestedElement.fieldType == 'select'}">
							    <s:select name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value" class="nestedInput selectsearch required-${nestedElement.required}">
							          	<s:option value=""><fmt:message key="general.default"/></s:option>
							        <c:forEach items="${nestedElement.enumValues}" var="enumValue">
							           <s:option value="${enumValue}"><fmt:message key="capture.${nestedElement.name}.${enumValue}"/></s:option>
							        </c:forEach> 
							    </s:select>			
							    <s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />                                    
							</c:if>
							<c:if test="${nestedElement.fieldType == 'textfield'}">
						       <sd:text name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value" 
								class="nestedInput textsearch required-${nestedElement.required}" placeholder="${placeholder}"/>
								<s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />
							</c:if>
                        ${(formElement.nestingStyle == 'multirow') ? '</p>' : ''}
					</c:forEach>
				</div>
				</c:forEach>
			</c:if>
			
			<c:if test="${nestedChildren == null || pid == null}">
                <c:set var="length" value="${fn:length(nestedElements) - 1}"/>
                
                <div class="nestedElement positionRelative ${nestStyleClass} ${multifieldClass}">
	                <c:forEach items="${nestedElements}" var="nestedElement" varStatus="loop">
                        ${(formElement.nestingStyle == 'multirow') ? '<p>' : ''}
	                    <c:if test="${nestedElement.fieldType == 'relation'}">
	                        <fmt:message key="capture.${nestedElement.name}" var="placeholder"/>
                            <c:set var="relationname" value="${nestedElement.name}" />
                            <!-- Relation pid in hidden field, this is what goes to fedora/solr -->
                            <s:hidden id="relationID_${nestedElement.name}${loop.index}" 
                                      name="allCaptureElements[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value"/>
                            
                            <!-- relation name in visible field -->
                            <sd:text id="relationNAME_${nestedElement.name}${loop.index}" 
                                        name="browsename" value="${actionBean.getNestedRelationName(mdElement, relationname, loop.index)}" 
                                        class="textrelation required-${nestedElement.required}" 
                                        placeholder="${placeholder}" />
                                        
                            <s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />
                            <button id="browse_${nestedElement.name}${loop.index}" name="${nestedElement.name}" class="browseBtn sub-form-button" title="${browse} '${fn:toLowerCase(mdElem)}'">...</button>
                            <input type="hidden" id="${nestedElement.name}" value="${mdElement}" /> 
	                    </c:if>
	                    <c:if test="${nestedElement.fieldType == 'select'}">
                            <s:select name="${fieldname}[${mdElement}].nestedElements[${loop.index-length}][${nestedElement.name}].value" class="nestedInput selectsearch required-${nestedElement.required}">
                                <s:option value=""><fmt:message key="general.default"/></s:option>
                                <c:forEach items="${nestedElement.enumValues}" var="enumValue">
                                <s:option value="${enumValue}"><fmt:message key="capture.${nestedElement.name}.${enumValue}"/></s:option>
                                </c:forEach> 
                            </s:select>         
                            <s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index-length}][${nestedElement.name}].name" value="${nestedElement.name}" />                                    
	                    </c:if>
	                    <c:if test="${nestedElement.fieldType == 'textfield'}">
	                        <fmt:message key="capture.${nestedElement.name}" var="placeholder"/>
                            <sd:text name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value" 
                                     class="nestedInput textsearch required-${nestedElement.required}" 
                                     placeholder="${placeholder}" />
                                     
                            <s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />
                        </c:if>
                        ${(formElement.nestingStyle == 'multirow') ? '</p>' : ''}
	                </c:forEach>
                </div>
			</c:if>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}" />
            <s:hidden name="${fieldname}[${mdElement}].multivalueType" value="${formElement.multivalue}" />
            <c:if test="${formElement.multivalue == 'multifield'}">
                <label id="add_${mdElement}" class="pointerCursor add-nested-element">
                    <img src="img/icons/silk/add_blue.png" class="smallicon"/> <fmt:message key="button.add.${mdElement}" />
                </label>
            </c:if>
            <div class="clearfix"></div>
		</div>
    </s:layout-component>
</s:layout-definition>
