<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
    <s:layout-component name="multifield">
        <div class="multifield-container">
			<c:if test="${fn:length(bean[mdElement].values) > 0}">
			    <c:set var="loopItems" value="${bean[mdElement].values}"/>
			    <c:set var="hasMultifields" value="true"/>
			</c:if>
			<c:if test="${fn:length(bean[mdElement].values) == 0}">
			    <c:set var="loopItems" value="0"/>
			    <c:set var="hasMultifields" value="false"/>
			</c:if>
                  
			<c:forEach var="multifieldPid" items="${loopItems}" varStatus="loop">
                <div class="multifieldElem positionRelative">
                
					<%-- Create delete button only if has multiple fields and is not first field (delete button would overlap tooltip image). User can empty the field with backspace --%>
					<c:if test="${hasMultifields && loop.index > 0}">
					    <img src="img/icons/silk/cancel.png" class="helpicon pointerCursor delete-multifield-element" name="${mdElement}" title="<fmt:message key="button.delete"/>"/>
					</c:if>
					
					<%-- Normal text fields --%>
					<c:if test="${formElement.fieldType == 'textfield'}">
					    <sd:text name="${fieldname}[${mdElement}].values[${loop.index}]" 
					             class="textsearch required-${formElement.required}" 
					             readonly="${formElement.readonly}" />
					</c:if>

                    <%-- Relation fields with hidden id field and visible name field --%>
					<c:if test="${formElement.fieldType == 'relation'}">
						<%-- Visible name --%>
						<s:text id="relationNAME_${mdElement}${loop.index}" 
								name="browsename" 
								value="${actionBean.getRelationName(multifieldPid)}" 
								class="textrelation required-${formElement.required}" /> 
						
						<%-- Hidden id (goes to Fedora) --%>
						<s:hidden id="relationID_${mdElement}${loop.index}" name="${fieldname}[${mdElement}].values[${loop.index}]"/>
						<input type="hidden" id="${mdElement}" value="${mdElement}" />
						
						<%-- For browse buttons' titles --%>
						<fmt:message key="button.browse" var="browse"/>
						<fmt:message key="capture.${mdElement}" var="mdElem"/>
						
						<button id="browse_${mdElement}${loop.index}"
								name="${mdElement}" 
								class="browseBtn sub-form-button" 
								title="${browse} '${fn:toLowerCase(mdElem)}'">...
						</button>
					</c:if>
                </div>
			</c:forEach>
            
	        <label id="add-multifield_${mdElement}" class="pointerCursor add-multifield-element">
	            <img src="img/icons/silk/add_blue.png" class="smallicon"/> <fmt:message key="button.add.${mdElement}" />
	        </label>
        </div> <%-- multifield-container --%>
        
        <div class="clearfix"></div>
    </s:layout-component>
</s:layout-definition>