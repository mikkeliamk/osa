<%@ include file="/layout/taglibs.jsp" %>

<!-- Not used until developed further -->
<c:redirect url="/" />

<!-- Specifies the base name of the resource bundle that is to be loaded. -->
<c:set var="source" value="${param.src}"/>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
	<style type="text/css">
	.facet-table{
	padding: 10px;	
	border: 1px solid balck;
	table-layout: fixed;
	}
	.facet-table tbody td{
	border: 10px solid white;	
	text-align: left;
	vertical-align: top;
	width:10%;
	}
	.option{
    color: rgb(100, 100, 100);
    }
	
	</style>
		<jsp:include page="${gui.subHeader}" />
	</s:layout-component>
    
    <s:layout-component name="html_head">
    <link rel="stylesheet" href="css/core.css">
   
    <script src="js/jsp-js-functions/clearform.js"></script>
    <script src="js/datatables-plugins/fnLengthChange.js"></script>    
        <s:messages/>      
        <s:errors globalErrorsOnly="true"/>
    <script>
    <fmt:message key="button.advBrowse.search" var="searchMessage"/>    
    var searchMessage = "${searchMessage}";
    
 	var facetCount;				//number of facet fields
 	var facetNames;				//names of facet fields
 	var facetValues;
 	var facetOptions;			//options (values) of facet fields
 	var facetItems; 			//all items for selected facet fields    
 	var resultsCount;			//number of results for selected search
 	var availableVectors; 		//all facets to be displayed in the selection box
 	 
 	var currentLine = [];		//current lines of all lists
 	var id = [];		
 	var allValues = [];
 	var selectedFacet = [];
	var selectedValue = [];
	var length = 20;			//length to be displayed initially
	var loadLimit = 40;			//length to prolong the column of items
	
    $(function(){ 
    	loadFacets();    	 
    });
	
 	//load facet fields
  	function loadFacets(){	    		
  			$.ajax({    
  	  			url: "AdvancedBrowse.action?getAjaxBrowseFacets=",
  	            contentType: "application/json",
  	            dataType: "json",   
  	            success: function(jsonFacets) {             	
  	            	facetNames = jsonFacets["FacetNames"];
  	    			facetCount = jsonFacets["FacetNumber"];  	    		
  	            	loadFacetOptions();
  	             }
  	         });	
  	}  	
 	
  	//load facet values
  	function loadFacetOptions(){	  		
  		$.ajax({    
  			url: "AdvancedBrowse.action?getAjaxBrowseOptions=",
            contentType: "application/json",
            dataType: "json",   
            success: function(options) {  
            	facetOptions = options;  
            	showOptions(facetOptions);
            	}
         });		
  	}  	
  	
  	//display facet table
    function showOptions(facetOptions){    	
    	if(facetOptions["NumberOfResults"]!= null){    		
  			$("#searchRedirect").html(searchMessage + " - "+facetOptions["NumberOfResults"]);
  		}    	
    	//make the list of selected facet filters
    	if((facetOptions["getFacetFilters"]!= '"[]"') && (facetOptions["getFacetFilters"]!= null)){    		
    		$("#searchedFacet").html("");
    		var formated = jQuery.parseJSON(facetOptions["getFacetFilters"]).replace(/\[/g, '');   		
    		
    		var items = formated.split("],");  
    		
    		for( var i=0; i< items.length; i++){
    			items[i] = items[i].replace(/\]/g,'');
    			var entries = items[i].split('=');     		
    			if(entries[1].split(',').length >= 2){
    				var values = entries[1].split(',');    				
    				for(var j = 0; j< values.length; j++){     				
    					$("#searchedFacet").append("<button class ='filterName' name='"+entries[0]+"' id='"+values[j]+'_'+entries[0]+"'>"+entries[0].slice(entries[0].indexOf(".")+1,entries[0].length).toUpperCase()+" "+values[j].toUpperCase()+"</button>   "); 
    				}
    			}
    			else{
          			$("#searchedFacet").append("<button class ='filterName' name='"+entries[0]+"' id='"+entries[1]+'_'+entries[0]+"'>"+entries[0].slice(entries[0].indexOf(".")+1,entries[0].length).toUpperCase()+" "+entries[1].toUpperCase()+"</button>   "); 
          		}   
    			
    		}
  		}	
    	
    	//display table
    	var limit;
   		$(".facet-table").html(" ");
       	$(".facet-table").append("<tr>");    
       	var r = 0;        
       	for (var i=0; i <= facetCount; i++){    
       		for (var j=0; j<= facetCount; j++){    
       			if (facetNames[i] == Object.keys(facetOptions)[j]){ //insert values in correct box      
   	    			var name = facetNames[i].slice(facetNames[i].indexOf('.')+1, facetNames[i].length);
   	    			$(".facet-table tbody").append( "<td id='col"+i+"' name='"+facetNames[i]+"'> <lable >"+name.toUpperCase()+"</lable><br><br>");    	    			
   	    			 allValues[facetNames[i]]=facetOptions[facetNames[i]];
   	    			var thisValue = allValues[facetNames[i]].split(',');    	
   	    			
   	    			
   	    			if (thisValue.length >length){
   	    				limit = length;
   	    			}
   	    			else{
   	    				limit = thisValue.length;
   	    			}  
   	    			
   	    			for (var k = 0; k < limit; k++){ 
   	    				//alert(thisValue[k]);
   	    				thisValue[k]=format(thisValue[k]);
   	    				var value = thisValue[k].replace("=","");
   	    				value = value+"  (result)";
   	    				$(".facet-table tbody td#col"+i).append( "<li><a href='#' class ='option' id='"+thisValue[k]+"'>"
   	    	    						+ value+"</a></li><br>" ); 
   	    				if((k == limit-1) && (limit == length)&& (thisValue.length != length)){    //if it's the last line	   	
   	    					$(".facet-table tbody td#col"+i).append( "<button class='option' id='loadMore'>Load more</button>" );  
   	    				}
   	    			}
   	    	    	currentLine[i] = limit;
   	    	    	r++;
   	    	    	//new row
   	    	    	if (r == 7){
   	    	    		r = 0;
   	    	    		$(".facet-table").append("</td></tr><tr>");
 	    	    	}
    	    	}  
   			}    
   		} 
       	removeSelected();
    }
  
  	//event to update the facet list and prolong it
    $(document).on('click', ".option", function() {     	
    	if($(this).attr("id") == "loadMore"){		//prolong the list
        	var columnId = $(this).parent().attr("id");
        	$(this).remove();
        	
        	var idValue = columnId.replace ( /[^\d.]/g, '' );
        	id.push(idValue);
        	if(id.length > 1){
        		id.splice(0, 1);
        		}
        	var thisValue =	allValues[facetNames[id]].split(',');      
      		if (currentLine[id] + loadLimit <= thisValue.length){	//if there're more values that can be displayed			
      			for (var k = currentLine[id]; k < currentLine[id] + length; k++){   
      				thisValue[k]=format(thisValue[k]);
    				var value = thisValue[k].replace("="," (");
    				value = value+" result)";
      	  			$(".facet-table tbody td#"+columnId).append( "<li><a href='#' class ='option' id='"+thisValue[k]+"'>"
      						+value+"</a></li><br>" );      	  		
      	  			if(k == currentLine[id] + length-1){    	//if it's the last value	      	  		
    					$(".facet-table tbody td#"+columnId).append( "<br><button class='option' id='loadMore'>Load more</button>" );  
    					currentLine[id] = currentLine[id] + length;
    					break;
    				}
      	  		}
      		}
      		else{      											// if all the values to the end can be displayed
      			for (var k = currentLine[id]; k < thisValue.length; k++){  	  	        				
      				thisValue[k]=thisValue[k].replace( /[\{\}.]/g," ");
      				thisValue[k]=thisValue[k].trim();
      				var value = thisValue[k].replace("="," (");
    				value = value+" result)";
      	  		$(".facet-table tbody td#"+columnId).append( "<li><a href='#' class ='option' id='"+thisValue[k]+"'>"
  						+value+"</a></li><br>" );      	  		
      	  		}
      			$(".facet-table tbody td#"+columnId).append("<button class='option' id='hideAll'>Hide all</button>");
      		}
        }
        
        else if($(this).attr("id") == "hideAll"){
      
        	var columnId = $(this).parent().attr("id");        
        	var allitems = $( ".facet-table tbody td#"+columnId).find("li");
        	var emptyLines = $( ".facet-table tbody td#"+columnId).find("br");
        	
        	for (var i=length; i<allitems.length; i++){
        	
        		allitems[i].remove();
        		emptyLines[i+1].remove();
        	}
        	$(this).remove();
        	$(".facet-table tbody td#"+columnId).append( "<button class='option' id='loadMore'>Load more</button>" ); 
        }
    	
        else {			//update facets
        	//$(".facet-table").html(" ");
            var optionId = $(this).attr("id");           
            selectedValue.push(optionId);
            
             var selectedFacetName = $(this).closest('td').attr("name");  
             if (optionId.indexOf("=")!= -1){        	
              	optionId = optionId.slice(0, optionId.search("="));
              	optionId = optionId+"_"+selectedFacetName;
              }   
            
             selectedFacet.push(selectedFacetName);
             $.ajax({    
       			url: "AdvancedBrowse.action?getAjaxUpdatedOptions=",       
       			method: "POST",
       			data: {selectedValue:  optionId.slice(0, optionId.search('_')), selectedFacet: selectedFacetName, action:"addFilter"},
       			success: function(facetOptions){        				
       				showOptions(facetOptions);       				
       			}
              });   
        }    
    });
  	
  	function removeSelected(){
  		for (var z = 0; z < selectedFacet.length; z++){
  			var linkToRemove = '.facet-table tbody td[name="'+selectedFacet[z]+'"] > li  a[id="'+selectedValue[z]+'"]';  		
  			$(linkToRemove).parent().next().remove();
  			$(linkToRemove).parent().remove();  
  		}
  	}
  	
  	function format(value){
  		if (value.split("-").length >= 2){
  			value=value.replace(value.lastIndexOf("-")," ");   
		}
		else{
			value=value.replace( /[\{\}.]/g," ");   
		}		
  		value=value.trim();		
		return value;
  	}
  	
    //event to delete the selected facet filter
  	$(document).on('click', ".filterName", function() {
  	    var ItemID = $(this).attr("id");
  		var FacetName = $(this).attr("name");
  		selectedFacet.splice(selectedFacet.indexOf(FacetName.trim()),1);
  		selectedValue.splice(ItemID.slice(0,ItemID.indexOf('_')).trim(),1);
  	  	$(this).remove();
  		$.ajax({    
    		url: "AdvancedBrowse.action?getAjaxUpdatedOptions=",       
    		method: "POST",
    		data: {selectedValue: ItemID.slice(0,ItemID.indexOf('_')).trim(), selectedFacet: FacetName.trim(), action: "deleteFilter"},
    		success: function(facetOptions){
    			showOptions(facetOptions);
    		}
        });
  		
  	});
  	
  	//clear current search
  	$(document).on('click', "#clearSearch", function() {
  		selectedFacet = [];
  		selectedValue = [];  		
  		$.ajax("AdvancedBrowse.action?clearSearch=" );
  		$("#searchedFacet").html('');
  		
  	    //location.reload();
  		loadFacets();
  	});
  	
  	//redirect to search
  	$(document).on('click', "#searchRedirect", function() {
  		window.location.href ="search.jsp?fromAdvBrowse=";
  	});
  
    </script>
    </s:layout-component>    
    
    <s:layout-component name="content">
    <button id="searchRedirect" ><fmt:message key="button.advBrowse.search"/></button>   <button id="clearSearch" >Clear current search</button><br><br>
    <big id="searchedFacet"></big>
    <div class = "facets-selection-block">
    </div>
 	<div class = "facets-container">
 	<table class = "facet-table"></table> 	
 	</div>
    
 	<s:layout-component name="view-container">
	</s:layout-component>	
        
	</s:layout-component>
</s:layout-render>