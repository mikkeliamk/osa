var openedDialogs = {};
var onkiOpen = false;

$(function() {
	$(".toggle-onki-search-form").click(function(e) {
		e.preventDefault();
		$("#onki-autocomplete-form_"+this.id.split("_")[1]).toggle();
	});
	$(".close-onki-form").click(function() {
		$(this.parentNode).hide();
	});
});

/** Gets terms from an ontology vocabulary by search term.<br/>
 *  Which ontology vocabulary is used, is defined in search fields data-onto-attribute and is then passed to the ONKI API
 * 
 * @param searchfield	Reference to the used input field. Used to get field's attributes' values
 */
function getOntologyData(searchfield) {
	var delayLength = 800; // ms
	if (searchfield.value.length > 0) {
		$(searchfield).addClass("fieldloaderwithimg");
		delay(function(){
			var fieldId = searchfield.id.split("_")[1];
			var xhr = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
				
			var q = searchfield.value;
			var o = searchfield.getAttribute("data-onto");
			
			var url = "Ingest.action?ontologyId="+o+"&term="+q+"&getOnkiHttp=";
			
			xhr.onreadystatechange = function () {
					if (xhr.readyState==4) {
						var jsonData = xhr.responseText;
						
						var resultContainer = document.getElementById("onto_results_"+fieldId);
						var resultAmount 	= document.getElementById("onto_results_amount_"+fieldId);
						
						try {
							var searchResults = JSON.parse(jsonData);
						
							if (searchResults.metadata.containingHitsAmount > 0) {
							
								var resultList		= document.createElement("select");
								resultList.multiple = true;
								resultList.id 		= "onto-resultlist_"+fieldId;
								resultList.setAttribute("class", "combo");
//								resultList.setAttribute("onblur", "hideOntologyData(event, this)");
					
								for (i in searchResults.results) {
									var resultEntry 	= searchResults.results[i];
									var resultString 	= "";
								
									if (resultEntry.altLabel) {
										resultString = resultEntry.altLabel + " (" + resultEntry.label + ")";
									} else {
										resultString = resultEntry.label;
									}
								
									var resultListEntry 		= document.createElement("option");
									resultListEntry.value 		= resultEntry.uri;
									resultListEntry.title 		= resultString;
									resultListEntry.innerHTML 	= resultString;
									resultListEntry.setAttribute("onclick","selectTerm(this, '"+fieldId+"', '"+searchfield.getAttribute("data-fieldname")+"');");
								
									resultList.appendChild(resultListEntry);
								}
								resultAmount.getElementsByTagName("span")[1].innerHTML	= "1 - "+searchResults.metadata.containingHitsAmount;
								
								resultContainer.innerHTML 	  = "";
								resultContainer.appendChild(resultList);
								resultContainer.style.display = "block";
							} // If results amount > 0
							else {
								resultContainer.style.display = "none";
								resultAmount.getElementsByTagName("span")[1].innerHTML	= "0 - "+searchResults.metadata.containingHitsAmount;
							}
							
							resultAmount.getElementsByTagName("span")[0].innerHTML	= searchResults.metadata.containingHitsAmount + searchResults.metadata.moreHitsAmount;
							resultAmount.style.display 	= "block";
						}
						catch (e) {
						
						}
						$(searchfield).removeClass("fieldloaderwithimg");
					}
				}
			
			xhr.open("GET", url, true);
			xhr.send(null);
		}, delayLength);
	}
}

function selectTerm(selectedOption, fieldId, fieldname) {
	loopIndex[fieldId]++;
	var selectedResult			= document.createElement("span");
	selectedResult.className 	= "onki_selected_term ellipsis";
	selectedResult.innerHTML 	= selectedOption.innerHTML;
	selectedResult.title	 	= selectedOption.innerHTML;
	
	var delBtn			= document.createElement("span");
	delBtn.innerHTML	= "X";
	delBtn.className	= "onto-delbtn";
	delBtn.setAttribute("onclick","deleteTerm(this.parentNode);");
	
	// Selected ontology term's URI and name in hidden fields
	var NAME_VALUE_hiddenElement 	= document.createElement("input");
	var NAME_NAME_hiddenElement 	= document.createElement("input");
	var URI_VALUE_hiddenElement 	= document.createElement("input");
	var URI_NAME_hiddenElement 		= document.createElement("input");

	// Name
	NAME_VALUE_hiddenElement.type  = "hidden";
	NAME_VALUE_hiddenElement.name  = fieldname+".nestedElements["+loopIndex[fieldId]+"]['name'].value";
	NAME_VALUE_hiddenElement.value = selectedOption.innerHTML;
	
	NAME_NAME_hiddenElement.type  = "hidden";
	NAME_NAME_hiddenElement.name  = fieldname+".nestedElements["+loopIndex[fieldId]+"]['name'].name";
	NAME_NAME_hiddenElement.value = "name";
	
	// Uri
	URI_VALUE_hiddenElement.type 	= "hidden";
	URI_VALUE_hiddenElement.name 	= fieldname+".nestedElements["+loopIndex[fieldId]+"]['uri'].value";
	URI_VALUE_hiddenElement.value 	= selectedOption.value;
	
	URI_NAME_hiddenElement.type  = "hidden";
	URI_NAME_hiddenElement.name  = fieldname+".nestedElements["+loopIndex[fieldId]+"]['uri'].name";
	URI_NAME_hiddenElement.value = "uri";
	
	selectedResult.appendChild(NAME_VALUE_hiddenElement);
	selectedResult.appendChild(NAME_NAME_hiddenElement);
	selectedResult.appendChild(URI_VALUE_hiddenElement);
	selectedResult.appendChild(URI_NAME_hiddenElement);
	selectedResult.appendChild(delBtn);
	
	document.getElementById("autocomplete_selected_"+fieldId).appendChild(selectedResult);
	
	// Removes the list of results after user selects entry from the list and clears the search field's value
	document.getElementById("onto_results_"+fieldId).removeChild(document.getElementById("onto-resultlist_"+fieldId));
	document.getElementById("autocomplete_"+fieldId).value 					= "";
	document.getElementById("onto_results_amount_"+fieldId).style.display 	= "none";
	document.getElementById("onki-autocomplete-form_"+fieldId).style.display 	= "none";
}

function deleteTerm(selectedTerm) {
	selectedTerm.parentNode.removeChild(selectedTerm);
} 

function hideOntologyData(event, element) {
	var fieldId 	= element.id.split("_")[1];
	var resultList 	= document.getElementById("onto-resultlist_"+fieldId);
	var explicitOriginalTarget = event.explicitOriginalTarget; 
	
	console.log("explicit:");
	console.log(event);
	console.log("range:");
	console.log(event.rangeParent);
	
	if (explicitOriginalTarget != null && explicitOriginalTarget != element) {
		if (explicitOriginalTarget != resultList) {
			if (explicitOriginalTarget != "option" && explicitOriginalTarget.parentElement != resultList) {
				document.getElementById("onki-autocomplete-form_"+fieldId).style.display = "none";
			}
		}
	}
}
