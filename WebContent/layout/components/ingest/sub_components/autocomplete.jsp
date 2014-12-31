<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
    <s:layout-component name="autocomplete">
        <fmt:message key="onki.autocomplete.placeholder" var="placeholder"><fmt:param value="${fn:toUpperCase(source)}"></fmt:param></fmt:message>
        <div class="onki-autocomplete-container displayInlineBlock">
        	<%-- TODO implement reading existing values to the div --%>
	        <div id="autocomplete_selected_${mdElement}">
	        	<c:forEach items="${bean[mdElement].values}" var="ontoTerm">
	        		<span class="onki_selected_term" title="${ontoTerm}">
	        			${ontoTerm}
	        			<span class="onto-delbtn" onclick="deleteTerm(this.parentNode);"> X</span>
	        			<s:hidden name="${fieldname}[${mdElement}].values" value="${ontoTerm}" />
	        		</span>
	        	</c:forEach>
	        </div>
			<input type="text" id="autocomplete_${mdElement}" class="onki-autocomplete textsearch" autocomplete="off" 
				   data-onto="${source}" 
				   data-fieldname="${fieldname}[${mdElement}].values" 
				   onblur="hideOntologyData(event, this)" 
				   onfocus="showOntologyData(this)" 
				   onkeyup="getOntologyData(this)" 
				   placeholder="${placeholder}"/>
				   
			<div id="onto_results_amount_${mdElement}" class="onto_results_amount">
				<fmt:message key="header.numofresults"/> <span></span>.
				<fmt:message key="header.showingresults"/> <span></span>.
			</div>
			<div id="onto_results_${mdElement}" class="onto_results positionRelative"></div>
        </div> 
        <div class="clearfix"></div>
    </s:layout-component>
</s:layout-definition>