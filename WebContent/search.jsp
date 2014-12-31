<%@ include file="/layout/taglibs.jsp" %>

<!-- Specifies the base name of the resource bundle that is to be loaded. -->
<fmt:setBundle basename="fi.mamk.osa.core.messages"/>
<s:useActionBean var="ingestaction" beanclass="fi.mamk.osa.stripes.IngestAction"/>
<c:set var="source" value="${param.src}"/>

<c:choose>
<c:when test="${empty user}">
	<fmt:setLocale value="en_US" scope="session" />
</c:when>
<c:otherwise>
	<fmt:setLocale value="${user.locale}" scope="session" />
</c:otherwise>
</c:choose>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}" />
	</s:layout-component>
    
    <s:layout-component name="html_head">
     <link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
    <script src="js/jsp-js-functions/clearform.js"></script>
    
       <script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
    <script src="js/datatables-plugins/fnLengthChange.js"></script>
    <script>
		var facetname, value, keys, facetlabel, src, oTable;
	    var resultsLoading = false;
		
	    var searchFacetResult;
	    
		var headers        = [];
		var aoColumns      = [];
		var rows           = ${gui.searchResults.rows};
		var cols           = ${gui.searchResults.getColumnNamesJson()};
		var displayOptions = [
		                      Math.ceil((rows / 20) / 10) * 10, 
		                      Math.ceil((rows / 10) / 10) * 10, 
		                      Math.ceil((rows / 5) / 10) * 10, 
		                      Math.ceil((rows / 2) / 10) * 10, 
		                      rows
		                      ];
		    
		$(function(){		
			$( "#startDate" ).datepicker();
			
			$( "#endDate" ).datepicker();
			
		    $("label#breadcrumb").html("<fmt:message key='link.advancedsearch'/>");
		    document.title = "<fmt:message key='link.advancedsearch'/> | " + document.title;
		    src = "${source}";
		
		    //check if values are recieved from advancedbrowse
			if (window.location.search.indexOf("fromAdvBrowse") != -1){
		                    
		                    
				  $.ajax({
		                url: "Search.action?&search_fromAdvBrowse=",		            
		                success: function(data) {
		                   createLayout();
		                
		                    $.ajax({
		                        url: "Search.action?getAjaxSearchFacets=",
		                        dataType: "json",
		                        success: function(facets) {   
		                            facetAjaxSuccess(facets, "#facets-window");
		                            $("#searchcontainer").hide("fast");
		                            $("#searchresultcontainer").show("fast");
		                            createShowAll($(".facets-window-container").find(".facet"));
		                        }
		                    });
		                }
		            });
				  $("#searchresult-header-bar").append("<button id = 'return'>Return to browse menu</button>");
		   }
		    
			$("#return").click(function(e){
				window.location.href ="AdvancedBrowse.action?keepSearch=";
		    });
		    
		    
		    // If used main page search field
		    if (src && src == "main") {
                createLayout();
                $.ajax({
                    url: "Search.action?getAjaxSearchFacets=",
                    dataType: "json",
                    success: function(facets) {
                        facetAjaxSuccess(facets, "#facets-window");
                        $("#searchcontainer").hide("fast");
                        $("#searchresultcontainer").show("fast");
                        createShowAll($(".facets-window-container").find(".facet"));
                    }
                });
		    }
		    
		    if(src && src == "login"){
		    	initSearch();
		    }
		    
		    $("[name=search]").click(function(e){
		    	e.preventDefault();
		    	$("#searchresultcontainer").removeClass("searchcontainer-open");
		        initSearch();
		    });
            
            $("#editsearch").click(function() {
            	$("#searchcontainer").show("fast");
            	$("#searchresultcontainer").addClass("searchcontainer-open");
            	scrollToTarget("#wrapper");
            });
		    
            $("#newsearch").click(function() {
            	scrollToTarget("#wrapper");
            	$("#searchcontainer").show("fast", function(){
	            	$("#searchresultcontainer").hide("fast");
            	});
            	clearForm($("#searchform")[0]);
            });
            
            $("#showrecords-container").on("click",".showrecords", function() {
            	if (!resultsLoading) {
	                $("#showrecords-container .showrecords").removeClass("showrecords-selected");
	                $(this).addClass("showrecords-selected");
	                oTable.fnLengthChange(parseInt(this.firstChild.data, 10)); // Get the value from clicked element's text
            	}
            });
            
            $("#resultDiv").on("click", ".basketaction", function(e){
            	var $this = $(this);
            	var action = $(this).attr("data-action");
                var pid = this.id;
                $this.prop("src", "img/ajax-loader-arrow.gif");
                $.ajax({
                    url: "Basket.action?"+action+"=",
                    data: {pidArray: [pid]},
                    dataType: "text",
                    success: function(data, statusText) {
                    	var icon = (action == "addToBasket") ? "basket_delete.png" : "basket_put.png";
                        setTimeout(function(){
                        	$this.prop("src", "img/icons/silk/"+icon);
                        },1000);
                    	updateBasketSize();
                    }
                });
                var actionVal = (action == "addToBasket") ? "deleteFromBasket" : "addToBasket";
                $this.prop("title", (action == "addToBasket") ? "<fmt:message key='message.removefrombasket'/>" : "<fmt:message key='message.addtobasket'/>");
                $this.attr("data-action", actionVal);
            });
            
            // Keeps facet window on screen when scrolling down
            $(window).scroll(function(){
            	var $rDiv       = $("#resultDiv"),
            	    $slc        = $("#searchresult-left-container"),
            	    width       = parseInt($slc.css("width"),10),
            	    marginRight = parseInt($slc.css("margin-right"),10),
            	    margin      = width + marginRight,
            	    padding     = 10; // 'floating' class 'top' property's value for smooth effect
            	if ($rDiv.is(":visible")) {
            		if ($(window).scrollTop() > $rDiv.offset().top - padding) {
            			$slc.addClass("floating");
            			// Prevents result table from overlapping the facet window
            			$rDiv.css("margin-left", margin);
	            	} else {
	            		$slc.removeClass("floating");
	            		$rDiv.css("margin-left", "0px");
	            	}
            	}
            });
            
		    // Expands facet list
		    $(".facets-window-container").on("click", ".showallfacets", function() {
		        $(this).parent().toggleClass("facet-open");
		        if ($(this).parent().hasClass("facet-open")) {
		            $(this).html("<fmt:message key='facets.showless'/> &#x25B2;");
		        } else {
		            $(this).html("<fmt:message key='facets.showall'/> &#x25BC;");
		        }
		    });
		    
		    // Adds/removes facets
            $(".facets-window-container").on("click", "a", function(e){
                e.preventDefault();
                var removeFilter = false;
                removeFilter = $(this).data("removefilter");
                var facetName = $(this).attr("id").split("|")[0];
                var facetValue = $(this).attr("id").split("|")[1];
                
                $.ajax({
                    url: "Search.action?previousSearch=1&removeFilter="+removeFilter+"&filter["+facetName+"]="+facetValue+"&ajax=true&search=",
                    type: "POST",
                    success: function(data) {
                        createLayout();
                        $.ajax({
                            url: "Search.action?getAjaxSearchFacets=",
                            dataType: "json",
                            success: function(facets) {
                                facetAjaxSuccess(facets, "#facets-window", function(){
	                                createShowAll($(".facets-window-container").find(".facet"));
                                });
                            }
                        });
                    }
                });
            });
		    
		    $("#clearfacets").click(function(){
		    	$this = $(this);
		        $.ajax({
		            url: "Search.action?previousSearch=1&removeAll=true&ajax=true&search=",
		            type: "POST",
		            success: function(data) {
		            	createLayout();
		                $this.hide();
                        $.ajax({
                            url: "Search.action?getAjaxSearchFacets=",
                            dataType: "json",
                            success: function(facets) {
                                facetAjaxSuccess(facets, "#facets-window", function(){
                                    createShowAll($(".facets-window-container").find(".facet"));
                                });
                            }
                        });
		            }
		        });
		    });
		    
		    // Selects all checkboxes inside clicked fieldset
		    $(".content-types legend").click(function(){
		    	var $checkboxes = $(this).parent().find("input[type='checkbox']");
		    	$checkboxes.prop("checked", !$checkboxes.prop("checked"));
		    });

		}); // DOM Ready
		
		
		function initSearch() {			
            $.ajax({
                url: "Search.action?ajax=true&search=",
                data: $("#searchform").serialize(),
                type: "POST",
                success: function(data) {
                    createLayout();
                    $.ajax({
                        url: "Search.action?getAjaxSearchFacets=",
                        dataType: "json",
                        success: function(facets) {   
                            facetAjaxSuccess(facets, "#facets-window");
                            $("#searchcontainer").hide("fast");
                            $("#searchresultcontainer").show("fast");
                            createShowAll($(".facets-window-container").find(".facet"));
                        }
                    });
                }
            });
		}
		
		/** Function to create a list of available facets for search that has been done
        *
        * @param  data                Object containing the facets returned by server
        * @param  element             Selector of an element to append the facets to
        * @param  callback            Optional callback function
        */		
		function facetAjaxSuccess(data, element, callback){
            $(element).empty();
            var resultCount      = data["resultcount"];      <%-- The number of results --%>
			var availableFacets  = data["facets"];           <%-- Available facets for the search --%>
			var activeFacets     = data["facetfilters"];     <%-- Currently active facet filters --%>
			var freetextQuery    = data["freetext"];         <%-- Freetext query, if any --%>
			var fields           = data["fields"];           <%-- Fields used in initial search, and their values --%>
			
            var $facet_container = "";
            var $facet = "";
            var $facet_name = "";
            var $facet_link = "";
           
            <%-- Number of results --%>
            $("#resultcount").html(resultCount);
            
            <%-- Search parameters used in initial search (fields, free text, etc) --%>
            var searchparams = [];
            searchparams.push(freetextQuery, fields);
            $("#searchparameters").empty();
            for (var d = 0; d < searchparams.length; d++) {
            	if (!jQuery.isEmptyObject(searchparams[d])) {
            		processActiveFacets(searchparams[d], "#searchparameters", false);
            	}
            }
            <%-- Show clear facets button if any facet is selected--%>          
            ((JSON.stringify(activeFacets) != '{"PID":"OSA\\\\:*","c.type":"*"}') && (!jQuery.isEmptyObject(activeFacets)) ? $("#clearfacets").show() : $("#clearfacets").hide());
            
            <%-- Available facets for the search --%>
            var facetLength = Object.keys(availableFacets).length;
            for(var i = 0; i < facetLength; i++)
            {
                facetname = Object.keys(availableFacets)[i]; // c.type, c.creator etc
                keys = availableFacets[facetname];
                $facet_container = $("<div class='facet-container'></div>");
                $facet = $("<div class='facet'></div>");
                $facet_name = $("<label class='facetname'>"+messagebundle.facets[facetname]+"</label>");
                $facet.append($facet_name); // Appends facet header (c.type, c.title etc) to facet container
                
                var keysLength = Object.keys(keys).length;
                for(var j = 0; j < keysLength; j++)
                {
                    amount = keys[Object.keys(keys)[j]]; // Amount
                    facetlabel = Object.keys(keys)[j];   // Label
                    
                    var checked = false;
                    
                    var filterLength = (typeof activeFacets[facetname] != "undefined") ? activeFacets[facetname].length : 0;
                    for (var activeFilterIndex = 0; activeFilterIndex < filterLength; activeFilterIndex++) {
                    	var activeFilter = activeFacets[facetname][activeFilterIndex];
                    	if(activeFilter == facetlabel) {
                            checked = true;
                        }
                    }
                    
                    // If facet type is c.type, get localized message from messagebundle-object, otherwise use original
                    var facetLabelLocal = localize(facetname, facetlabel);
                    
                    if(checked) {
                    	$facet_link = $("<a id='"+facetname+"|"+facetlabel+"' class='facetlink selectedfacet removefacet' data-removefilter='true'>"+facetLabelLocal+" ("+amount+")</a>");
                    } 
                    if(!checked) {
                    	var classname = (facetlabel == "empty") ? "undefined-facet" : "";
                    	$facet_link = $("<label class='facetlink'>"+
                    			          "<a href='#' data-removefilter='false' id='"+facetname+"|"+facetlabel+"'>"+
                    			             "<span class='"+classname+"'>"+facetLabelLocal+" ("+amount+")</span>"+
                    			          "</a>"+
                    			        "</label>");
                    }
                    $facet.append($facet_link); // Appends links to list
                }
                $facet_container.append($facet); // Appends lists to their containers
	            $(element).append($facet_container); // Appends facet containers to facet window
            }
            $(element).append($("<div class='clearfix'></div>"));
            
            // Optional callback function
            if(callback && typeof(callback) === "function") {
            	callback();
            }
        }
        
        /** Function to create a list of active facets and/or fields used in initial search
        *
        * @param  data                Data object to process
        * @param  element             Selector of an element to append the given data to 
        * @param  createLink          Boolean flag to create link. If false, only label will be created.  
        */
		function processActiveFacets(data, element, createLink){
            var filterKeys = Object.keys(data); <%-- Array of keys, c.type, c.creator etc --%>
            var fkLength = filterKeys.length;
            for (var s = 0; s < fkLength; s++) {
                var filtername = filterKeys[s]; <%-- c.type, c.creator etc --%>
                $(element).append(messagebundle.facets[filtername]);

                var filters = data[filterKeys[s]]; <%-- Array of values --%>
                var fLength = filters.length;
                for (var p = 0; p < fLength; p++) {
                    var filtervalue = filters[p] <%-- Value --%>
                    var filtervalueLocal = localize(filtername, filtervalue);
                    if (createLink) {
	                    $(element).append($("<label class='facetvalue'>"+
	                    		              "<a href='#' class='removefacet' id='"+filtername+"|"+filtervalue+"' data-removefilter='true'>"+
	                    		                 filtervalueLocal+
	                    		              "</a>"+
	                    		            "</label>"));
                    } else {
                    	$(element).append($("<label class='facetvalue'>"+filtervalueLocal+"</label>"));
                    }
                }
            }
		}
		
		function localize(type, value) {
            var localized = "";
            if (type == "c.type") {
            	localized = (value == "empty") ? messagebundle.facets[value] : messagebundle[value];
            } else {
            	localized = (value == "empty") ? messagebundle.facets[value] : value;
            }
            return localized;
        }
		
        // Creates "Show all" button after a facet list if the list has more content that it can show
		function createShowAll(elementSelector) {
            var $elements = elementSelector;
            $elements.each(function(){
                $element = $(this);
                var offsetHeight = $element.prop("offsetHeight");
                var scrollHeight = $element.prop("scrollHeight");
                if (offsetHeight < scrollHeight) {
                    var label = $("<span class='showallfacets pointerCursor'><fmt:message key='facets.showall'/> &#x25BC;</span>");
                    $element.after(label);
                } else {
                    // If facet list does not have many items, decrease the height
                    $element.parent().css("height", "auto");
                }
            });
		}
	
		<%-- 
		  Indexes:
		      [0] = Column name matching Solr field.
		      [1] = Table header's messagebundle key to use for translated headers.
		      [2] = Boolean flag indicating whether table can be sorted by this column.
		      [3] = Width of the column.
		--%>
        <c:forEach var="columns" items="${gui.searchResults.headerKeys}" varStatus="loop">
            headers.push("<fmt:message key ="${columns[1]}" />");
            var sWidth = "${columns[3]}";
            var bSortable = "${columns[2]}";
            aoColumns.push({"bSortable": (bSortable === "true") ? true : false, "sWidth":sWidth});
        </c:forEach> 
        
		function createLayout(){
	      $('#resultDiv #processing').nextAll().remove();
	      
    	  $("#showrecords-container div").empty();
	      for (var p = 0; p < displayOptions.length; p++) {
	    	  var selected = (displayOptions[p] == rows) ? " showrecords-selected" : "";	    	
		      $("<label/>",{
		    	  "class":   "showrecords pointerCursor" +selected,
		    	  "text" :   displayOptions[p]
		      }).appendTo($("#showrecords-container div"));
	      }
	      var $resulttable = $("<table/>", {"id": "searchresulttable"});
	      var $thead = $("<thead/>");
	      var $tr = $("<tr/>");
	      for(var n = 0;n<cols.length;n++){
	    	  $("<th/>", {"text": headers[n]}).appendTo($tr);
	      }
	      $tr.appendTo($thead);
	      $thead.appendTo($resulttable);
	     
	      $('#resultDiv').append($resulttable);
	      oTable = $('#searchresulttable').bind("processing", function(e, oSettings, bShow){
                  if (bShow) { $("#processing").fadeIn(); resultsLoading = true;} // Binds function to "processing" event to check whether datatables is loading content or not,
                  else { $("#processing").fadeOut(); resultsLoading = false;}     // and show/hide loading indicator according to that
              }).dataTable({
		        "bAutoWidth":         false,
		        "bJQueryUI":          true,
		        "iDisplayLength":     ${gui.searchResults.rows},
                "aaSorting":          [${gui.searchResults.defaultSort}],
		        "sPaginationType":    "full_numbers",
		        "bProcessing":        false,
		        "bServerSide":        true,
		        "sAjaxSource":        "Search.action?manage=",
        		"sDom":               '<"H"ip>t<"F"ip>', // Modifies Datatables components that will be shown.
		        "oLanguage": {
		            "sSearch":          '<fmt:message key="datatable.filter" />',
		            "sProcessing":      '<fmt:message key="datatable.loading" />',
		            "sInfo":            '<fmt:message key="datatable.showing" />',
		            "sInfoFiltered":    '<fmt:message key="datatable.filtered" />',
		            "sZeroRecords":     '<fmt:message key="datatable.empty" />',
		            "sInfoEmpty":       '<fmt:message key="datatable.empty" />',
		            "oPaginate": {
		                "sNext":        '>',
		                "sPrevious":    '<',
		                "sFirst":       '<<',
		                "sLast":        '>>',
		            }
		        },
		        "aoColumns" : aoColumns
		        });
		    }
	</script>
    </s:layout-component>
   
    <s:layout-component name="content">
    
		
        <s:messages />
        <h1 class="largeheader-nomargin"><fmt:message key="link.advancedsearch"/></h1><div class="header-hr"></div>
        
        <s:form action="/Search.action" id="searchform" focus="search.freetext">
        
	        <div id="searchcontainer">
	            <c:forEach var="field" items="${gui.searchConfiguration.fieldValues}" varStatus="loop">
	            	<c:if test="${field.name == 'freetext'}">
	                    <div class="freetext">
		                   <fmt:message key="search.freetext" />
		                   <div><s:text name="search.freetext" id="freetext-searchbox"/></div>
						</div>
					</c:if>
									
                   <div class="text">
	                   <fmt:message key="search.${field.name}" /><br/>
	                   <s:text name="search.fields[${field.name}].value[0]" size="30"/>
                   </div>
	
	                <c:if test="${field.name == 'c.type'}">
	                 	<div class="option">
	                 		<s:useActionBean var="searchaction" beanclass="fi.mamk.osa.stripes.SearchAction"/>
	                 		<c:set var="contenttypes" value="${searchaction.contentTypes}"/>
		                 	<c:if test="${!empty contenttypes}">
			               		<fmt:message key="search.${field.name}" /> 
			               		<span data-tooltip="<fmt:message key="tooltip.search.types"/>"><img src="img/icons/silk/help.png" class="helpicon smallicon"/></span><br/>			                   	
			                   	<c:forEach var="category" items="${contenttypes}">
                                    <c:if test="${!empty category}">
	                                    <fieldset id="${category.key}" class="content-types form-section">
	                                        <legend class="pointerCursor"><fmt:message key="link.add.${category.key}"/></legend>
											<c:forEach var="item" items="${category.value}">
	                                            <s:checkbox name="search.fields[${field.name}].value" id="id${item.key}" value="${item.key}"/>
	                                            <s:label for="id${item.key}"><fmt:message key="link.add.${item.key}"/></s:label>
	                                            <br/>
							                </c:forEach>
                                        </fieldset>
						            </c:if>
			                   	</c:forEach>
			                </c:if>
	           			</div>
	                </c:if>
	         	</c:forEach>				    
	        
		        <div class="submit-search-form">
		            <s:submit name="search"><fmt:message key="button.search" /></s:submit>
		            <s:reset name="clear"><fmt:message key="button.clear" /></s:reset>
		        </div>
	        </div> <%--#searchcontainer --%>
		</s:form>
		
        <div id="searchresultcontainer">
	        <div id="searchresult-left-container" class="floatLeft">
		        <button id="editsearch"><fmt:message key="button.editsearch"/></button>
                <button id="newsearch"><fmt:message key="button.newsearch"/></button>
				<div class="facets-window-container">
	                <div id="searchparameters-container">
	                    <label class="facets-header"><fmt:message key="facets.searchparameters"/></label>
	                    <div id="searchparameters"></div>
	                </div>
	                <div id="activefacets-container">
	                    <label class="facets-header"><fmt:message key="facets.activefacets"/></label>
	                    <div id="activefacets"></div>
	                </div>
	                <label class="facets-header"><fmt:message key="facets.header"/></label>
	                <div id="facets-window"></div>
                    <label id="clearfacets" class="pointerCursor"><fmt:message key="link.removefacets"/></label>
				</div> <%-- .facets-window-container --%>
			</div> <%-- #searchresult-left-container --%>
			
	        <div id="resultDiv" class="result-container floatLeft">
				<div id="searchresult-header-bar">
				    <div id="amount" class="item">
				        <h2 id="numofresults" class="largeheader-nomargin"><fmt:message key="header.numofresults"/> <span id="resultcount"></span></h2>
				    </div>
				    <%-- <div id="sort" class="item"></div> --%>
				    <div id="showrecords-container" class="item">
				        <span class="bold largeheader-nomargin"><fmt:message key="datatable.paging.showrecords"/></span>
				        <div class="displayInlineBlock"></div>
				    </div>
				    <div class="clearfix"></div>
				</div> <%-- #searchresult-header-bar --%>
				
				<%-- Processing indicator for Datatables --%>
				<div id="processing">
				    <div id="processing-content">
						<h3 class="largeheader"><fmt:message key="search.active.header"/>...</h3>
						<img src="img/ajax-loader-blue.gif"/>
						<p><fmt:message key="search.active.pleasewait"/>.</p>
						<p><fmt:message key="search.active.info"/>.</p>
				    </div>
				</div>
	        </div> <%-- #resultDiv --%>
	        <div class="clearfix"></div>
        </div>
	</s:layout-component>
</s:layout-render>