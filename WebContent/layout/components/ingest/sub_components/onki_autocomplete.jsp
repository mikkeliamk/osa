<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
    <s:layout-component name="autocomplete">
        <fmt:message key="onki.autocomplete.placeholder" var="placeholder"><fmt:param value="${fn:toUpperCase(source)}"></fmt:param></fmt:message>
        <fmt:message key="onki.autocomplete.toggle" var="toggleAutocomplete"/>
        
        <div class="onki-autocomplete-container displayInlineBlock positionRelative">
	        <button class="toggle-onki-search-form sub-form-button" id="toggle_${mdElement}" title="${toggleAutocomplete}"><fmt:message key="button.add.${mdElement}"/></button>
        	
        	<%-- TODO implement reading existing values to the div --%>
	        <div id="autocomplete_selected_${mdElement}">
	        	<c:forEach items="${bean[mdElement].nestedElements}" var="ontoTerm" varStatus="loop">
	        		<c:if test="${ontoTerm != null}">
		        		<span class="onki_selected_term ellipsis" title="${ontoTerm['name'].value}">
		        			${ontoTerm['name'].value}
		        			<span class="onto-delbtn" onclick="deleteTerm(this.parentNode);">X</span>
				        	<c:forEach items="${formElement.nestedFormElements}" var="nestedElement">
			        			<s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].value" value="${ontoTerm[nestedElement.name].value}" />
			        			<s:hidden name="${fieldname}[${mdElement}].nestedElements[${loop.index}][${nestedElement.name}].name" value="${nestedElement.name}" />
				        	</c:forEach>
		        		</span>
	        		</c:if>
	        	</c:forEach>
	        </div>
	        
	        <div id="onki-autocomplete-form_${mdElement}" class="onki-autocomplete-form">
	        	<img src="img/icons/silk/cross.png" class="close-onki-form pointerCursor" alt="[x]" title="<fmt:message key="button.close"/>"/>
	        	
				<input type="text" id="autocomplete_${mdElement}" class="onki-autocomplete textsearch" autocomplete="off" 
					   data-onto="${source}" 
					   data-fieldname="${fieldname}[${mdElement}]" 
					   onkeyup="getOntologyData(this)" 
					   placeholder="${placeholder}"/>
					   
				<!-- onblur="hideOntologyData(event, this)"  -->

				<div id="onto_results_amount_${mdElement}" class="onto_results_amount">
					<fmt:message key="header.numofresults"/> <span></span>.
					<fmt:message key="header.showingresults"/> <span></span>.
				</div>
				<div id="onto_results_${mdElement}" class="onto_results positionRelative"></div>
			</div>
        </div> 
        <div class="clearfix"></div>
    </s:layout-component>
</s:layout-definition>