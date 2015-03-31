<%@ include file="/layout/taglibs.jsp" %>
<%-- Mandatory checks --%>
<c:if test="${empty user || user.getHighestAccessRight() < 30}"><c:redirect url="/" /></c:if>
<c:if test="${empty actionBean}"><c:redirect url="workspace.jsp" /></c:if>

<s:useActionBean var="ingestaction" beanclass="fi.mamk.osa.stripes.IngestAction"/>
<%-- <s:useActionBean var="basketaction" beanclass="fi.mamk.osa.stripes.BasketAction"/> --%>
<c:set var="index" value="${param.index}"/>
<c:set var="pid" value="${ingestaction.pid}" />
<c:set var="accessKey" value="${ingestaction.onkiAccessKey}" />
<c:if test="${param.index != 'mass'}">
    <c:set var="tabs" value="${gui.getGUITabs(index)}" />
    <c:set var="names" value="${gui.getGUIElements(index)}" />
</c:if>
<c:if test="${param.index eq 'mass'}">
    <c:set var="tabs" value="${actionBean.customTabs}" />
    <c:set var="names" value="${actionBean.customForm}" />
</c:if>

<c:set var="previousUrl" value="${actionBean.isFromSearch()}" />

<c:set var="fieldname" value="allFedoraBeans[0].captureData.metaDataElements" />
<c:set var="bean" value="${actionBean.allFedoraBeans[0].captureData.metaDataElements}" scope="request"/>

<fmt:message key="message.downloadtargz" var="downloadtargz"/>
<fmt:message key="message.downloadzip" var="downloadzip"/>
<fmt:message key="button.delete" var="deletebutton"/>
<fmt:message key='tooltip.button.addNew' var="addNewButtonTooltip"/>
<fmt:message key="message.addtobasket" var="addtobasket"/>
<fmt:message key="message.removefrombasket" var="removefrombasket"/>
<fmt:message key="tooltip.original.shorthelp" var="showoriginal"/>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="${gui.subHeader}"/>
    </s:layout-component>
    
    <s:layout-component name="html_head">
    
        <script src="js/tree_modified.jquery.js"></script>
        <script src="js/jsp-js-functions/js-tab-functionality.js"></script>
        <script src="js/jsp-js-functions/onki-autocomplete.js"></script>
        <link rel="stylesheet" href="css/jqtree.css">
        <style type="text/css" media="screen">
    .transparent { background:rgba(255,255,255,0.8); }
</style>
        <script type="text/javascript">
		    
		<%-- ----------------------------------------
		 * 					CONTENT
		 *	Search capitalized header for shortcut
		 * ----------------------------------------
		 * TREE VIEW INITIALIZATION
		 *		Code for initializing the tree view and fetching the content
		 *
		 * BREADCRUMB NAVIGATION AND PAGE TITLE
		 *		Sets breadcrumb navigation created by server
		 *		Sets page title
		 *
		 * INHERITANCE & TREE VIEW LISTING
		 *		Functions for inheritance feature
		 *		Events & functions for tree view functionality (open/close, select)
		 *
		 * TEXT FIELD-SPECIFIC OPERATIONS
		 *		Functionalities bind to text fields, e.g checking if field is needed when losing focus
		 *		Autocomplete for relation fields
		 * 
		 * BUTTON & SELECT ACTIONS
		 *		Actions bind to button and select elements that are not included in the above parts
		 *		Deleting object, attachments, downloading object, inserting/removing object from the basket
		 *
		 * PAGE FUNCTIONALITY & EVENT MODIFICATIONS
		 *		Functions that modify and alter the way page operates
		 *		F.ex. disabling certain keys from triggering something
		 *		Notify user when leaving page with unsaved changes
		 *
		 * FILE UPLOAD (multi-upload)
		 *		Initialization and functionality of file upload component
		 *
		 * ---------------------------------------------------------------------
		--%>
		
		<%-- Initializes object containing loop indexes for multifield elements --%>
		var loopIndex = {};
		<c:forEach var="elem" items="${names}">
            <c:if test="${elem.value.multivalue != 'false' && elem.value.multivalue != 'delimited'}">
                <c:if test="${pid == null}">
                    loopIndex["${elem.value.name}"] = 0;
                </c:if>
                <c:if test="${pid != null}">
	                <c:if test="${(elem.value.fieldType == 'nested' && elem.value.multivalue == 'multifield') || (elem.value.fieldType == 'linked' && elem.value.multivalue == 'multifield')}">
	                    loopIndex["${elem.value.name}"] = "${fn:length(bean[elem.value.name].nestedElements)}";
	                </c:if>
	                <c:if test="${elem.value.fieldType != 'nested' && elem.value.multivalue == 'multifield' && elem.value.fieldType != 'linked'}">
	                    loopIndex["${elem.value.name}"] = "${fn:length(bean[elem.value.name].values)}";
	                </c:if>
	            </c:if>
            </c:if>
        </c:forEach>
        
		// Global variables
		var currentpid                = "${pid}";
		var contentType               = "${index}";
		var userOrganization          = "${user.organization.name}";
		var selected_tab              = "";
		var browseval;
		var browsename                = "";
		var selectedrelation;
		var selectedrelationnameid    = "";
		var selectedPidType           = "";
		var attachmentcount           = 0;
		var filecount                 = 0;
		var indicatorRow;
		var initForm                  = "";
		var formSubmit                = false;
		var nestedLoopIndex           = 0;
		var openedNodes               = {};
		var insertURL				  = "Ingest.action?index=&create=";
		var selectedObjectType        = "";
		
		var listCollectionsJson;
		// Translated messages for index url parameter
		// Used in breadcrumb navigation and page title
		var ingestMessagebundle = {
	            "action":           "<fmt:message key='ingest.action'/>",
	            "agent":            "<fmt:message key='ingest.agent'/>",
	            "audio":            "<fmt:message key='ingest.audio'/>",
	            "basecollection":   "<fmt:message key='ingest.basecollection'/>",
	            "groupcollection":  "<fmt:message key='ingest.groupcollection'/>",
	            "unitcollection":   "<fmt:message key='ingest.unitcollection'/>",
	            "document":         "<fmt:message key='ingest.document'/>",
	            "drawing":          "<fmt:message key='ingest.drawing'/>",
	            "event":            "<fmt:message key='ingest.event'/>",
	            "image":            "<fmt:message key='ingest.image'/>",
	            "map":              "<fmt:message key='ingest.map'/>",
	            "movingimage":      "<fmt:message key='ingest.movingimage'/>",
	            "physicalobject":   "<fmt:message key='ingest.physicalobject'/>",
	            "place":            "<fmt:message key='ingest.place'/>",
	            "mass":             "<fmt:message key='ingest.mass'/>",
        };

		var pidType = {
			"F": "actionId",
			"W": "agentId",
			"E": "eventId",
			"L": "placeId"	
		};
		
		var createFileUploadFor = ["audio", "document", "drawing", "image", "map", "movingimage"];
		
		// Pads given date item with a leading zero if it is under 10
		function padZero(d) {
			if(d < 10) {
				d = "0"+d;
			}
			return d;
		}
		
		/** Function to add validation to given elements, used with nested elements where all linked elements must have value.
		*
		* @param  element             Element(s) to search for value
		* @param  AddValidationTo     Element(s) that must have value if other given element(s) have value 
		*/
		function validateNestedElements(elements, AddValidationTo) {
			for (var i = 0; i < elements.length; i++) {
				// Loop through given elements
	            elements[i].each(function(){
	                if($(this).val() != "") {
	                	// If element has value, check if other elements linked to it does not have values
	                	for (var j = 0; j < AddValidationTo.length; j++) {
	                		if($(this).parents().eq(1).find(AddValidationTo[j]).val() == "") {
	                			// If they don't have values, add validation
	                            $(this).parents().eq(1).find(AddValidationTo[j]).prop("required", true);
	                        }
	                	}
	                }
	            });
			}
        }
        
		function getOrigThumb(){
			var $thumbContainer = $("#orig-thumbnail-container");
			
			// If editing existing object
			if(currentpid != "") {
				$thumbContainer.show("fade").css("display", "inline-block");
				$thumbContainer.append("<img src='img/ajax-loader-bar.gif' id='origloader'/>");
	 			$.ajax({
					url: "Ingest.action?checkIfHasOriginal=",
					data: {pid: currentpid},
					success: function(data, textStatus, jqXHR) {
						$("#origloader").remove();
						var hasOrig = data.split("|")[0];
						// If object has original file
						if(hasOrig == "true") {
							var hasThumb = data.split("|")[1];
							var tooltip  = "${showoriginal}";
							var imgIcon  = '<img src="img/icons/silk/page_save.png" class="smallicon" alt="original" title="'+tooltip+'"/>';
							var html     = '<s:link href="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&showFile="><img src="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&hasThumbnail='+hasThumb+'&getFileThumb=" class="thumbnail-image" alt="original" title="'+tooltip+'"/></s:link>';
							var htmlIcon = '<s:link href="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&showFile=">'+imgIcon+'</s:link>';
							
							$(".download-container").append(htmlIcon);
							$thumbContainer.append(html);
							$("#original p").first().after("<fmt:message key='header.originalexists'/>");
						}
						// If has not
						else {
							$thumbContainer.append("<fmt:message key='header.nooriginalfound'/>");
							$thumbContainer.find("img").remove();
							for(var i = 0; i < createFileUploadFor.length; i++){
								// If current type of form equals the one in list, show upload button
								if(contentType == createFileUploadFor[i]) {
									$("#file-uploader").show("fade");
								}
							}
						}
					}
	           	});
			}
			// if creating a new object
			else {
				for(var j = 0; j < createFileUploadFor.length; j++){
					// If current type of form equals the one in list, show upload button
					if(contentType == createFileUploadFor[j]) {
						$("#file-uploader").show("fade");
					}
				}
			}
		}
		
		function getAttachmentThumbs(){
			var $attThumbContainer = $("#att-thumbnail-container"); 
			
			$attThumbContainer.append("<img src='img/ajax-loader-bar.gif' id='attloader'/>");
			$.ajax({
				url: "Ingest.action?checkIfHasAttachments=",
				data: {pid: currentpid},
				dataType: "json",
				success: function(data) {
					$("#attloader").remove();
					// Object has some attachments
					if(!jQuery.isEmptyObject(data)) {
						var html = "";
						for(var datastream in data) {
							if(data.hasOwnProperty(datastream)){
								// value = boolean hasThumbnail 
								var value = data[datastream];
								html = "<span id='attspan_"+datastream+"' class='attimgcontainer'>";
								html += '<s:link href="Ingest.action?pid='+currentpid+'&dsName='+datastream+'&showFile="><img src="Ingest.action?pid='+currentpid+'&dsName='+datastream+'&hasThumbnail='+value+'&getFileThumb=" class="thumbnail-image" alt="'+datastream+'"/></s:link>';
	                            html += "<img src='img/icons/silk/cancel.png' class='deleteattachmentbtn pointerCursor' id='"+datastream+"' title='${deletebutton}' alt='[x]'/>";
	                            html += "</span>";
							}
							$attThumbContainer.append(html);
						}
			    	}
					// Object does not have any attachments
					else {
						$attThumbContainer.append("<fmt:message key='header.noattachmentfound'/>");
					}
				},
				error: function() {
					$("#attloader").remove();
				}
           	});
		}
		
        /** Marks fields with inconsistant values after opening multiple objects on the form.
        *
        */
        function getWorkflowValues() {
            var fieldName = "allFedoraBeans[0].captureData.metaDataElements";
            <c:forEach var="field" items="${actionBean.inconsistantFields}">
                var field = "${field}";
                var $field = $("[name='"+fieldName+"["+field+"].value']");
                $field.css({"border": "solid 1px #EDB600", "box-shadow": "0px 0px 5px #EDB600"})
                      .prop({"title": "<fmt:message key='error.inconsistant.metadata'/>", "disabled":true, "autocomplete":"off"});
            </c:forEach>
        }
	
		function getInheritance(ispartOf){
			var model = contentType;
			$('.manualinherit').remove();
			
			$.ajax({
			    data: {'pid': ispartOf, 'cmodel' : model, 'index' : "${index}"},
			    url: "Ingest.action?getInheritedElements=",
			    global: false,     // this makes sure ajaxStart is not triggered
			    dataType: 'json',
			    success: function( data ) {
			        for(var i=0; i<data.length; i++){
			            var element = data[i].name;
			            var elementname = $('input[value='+element+']').attr("name");
			            if(elementname != undefined){
                            var inputtagname = elementname.replace(".name", ".value");
			                if(data[i].defaultInheritance == true){
			                    if(data[i].behaviour == "replaceAll"){
			                        $('input[name="'+inputtagname+'"]').val(data[i].value);
			                    }else if(data[i].behaviour == "replaceEmpty"){
			                        if($('input[name="'+inputtagname+'"]').val() == ""){
			                            $('input[name="'+inputtagname+'"]').val(data[i].value);
			                        }
			                    }else if(data[i].behaviour == "replaceExisting"){
			                        if($('input[name="'+inputtagname+'"]').val() != ""){
			                            $('input[name="'+inputtagname+'"]').val(data[i].value);
			                        }
			                    }else if(data[i].behaviour == "incremental"){
			                    	var value = $('input[name="'+inputtagname+'"]').val();
			                    	if (value != "") value = value+" ";
			                    	value = value + data[i].value;
			                    	$('input[name="'+inputtagname+'"]').val(value);
			                    }
			                }
			                if(data[i].manualInheritance == true){
			                    //create inherit-button next to element
			                    $('input[name="'+inputtagname+'"]').after(' <button behaviour="'+data[i].behaviour+'" value="'+data[i].value+'" class="manualinherit" id="inherit_'+inputtagname+'"><fmt:message key="button.inherit"/></button>');
			                }
			                
			            } else {
			            	
                            if(data[i].fieldType == "relation"){
                            	var valueArray = data[i].value.split(";");
                            	var visibleValueArray = data[i].visibleValue.split(";");
                            	var $container = "";
                            	
                            	console.log(loopIndex[data[i].name]);
                            	loopIndex[data[i].name] += valueArray.length;
                            	for(var x=0; x<valueArray.length; x++){
                            		if($("input#relationID_" +data[i].name + x).length > 0){
                            			$container = $("input#relationID_" +data[i].name + x).closest(".multifield-container");
                            			$("input#relationID_" +data[i].name + x).val(valueArray[x]);
                                        $("input#relationNAME_" +data[i].name + x).val(visibleValueArray[x]);
                            		} else {
                            			var $div = $("<div/>", {
			                            				"class" : "multifieldElem positionRelative",
			                            			});
		                                $("<input/>", {
		                                	id: "relationID_" + data[i].name + x,
		                                	type: "hidden",
		                                	name: "allFedoraBeans[0].captureData.metaDataElements["+data[i].name+"].values["+x+"]",
		                                	value: valueArray[x],
		                                }).appendTo($div);

		                                $("<input/>", {
		                                	"class": "textrelation",
                                            id: "relationNAME_" + data[i].name + x,
                                            type: "text",
                                            value: visibleValueArray[x],
                                        }).appendTo($div);
		                                
	                                	$("<button/>", {
                                            "class": "browseBtn sub-form-button",
                                            id: "browse_" + data[i].name + x,
                                            name: data[i].name,
                                            text: "...",
                                        }).appendTo($div);
                                        
		                                $container.find(".add-multifield-element").before($div);
                            		}
                            	}
                            }
                        }
			        }          
			    }
			});
		}
	      
		/* ************************
		 * TREE VIEW INITIALIZATION
		 * ************************ */
		
		function initalizeTree(data, container){						
			var $tree = $(container);		
			var relId = selectedrelationnameid;
			var autoOpen = (relId == "actionId") ? false : 0;
			//$tree.tree('loadData', data);
			if(container == "#filteredFound"){
				autoOpen = true;
			}
			
			
			$tree.tree({
		    	data: [data],
				autoOpen: autoOpen,
				slide: false,
			   	onCreateLi: function(node, $li) {
			   		
			   	   var type = node.type;
			       $li.find('.jqtree-title').before('<span class="icon"><img src="img/icons/silk/'+types[type]+'"/></span>');
			       if (type == "C" || type == "F" && node.children.length > 0) {$li.find('.jqtree-title').after(" ("+node.children.length+")");}
			  	},
			  	onCanSelectNode: function(node) {			  	
			  		selectedPidType = node.id.split(":")[1].split("-")[0];
			  		if(node.id === currentpid) {
			  			$(".errorlabel").show(0).delay(1000).fadeOut(800);
			  			return false;
			  		}
			  		// If browsing relations that fall into if's conditions, disabled selecting other than the correct type
			  		else if((relId == "actionId" || relId == "agentId" || relId == "eventId" || relId == "placeId")) {
			  			if(selectedPidType == "C") {
			  				return false;
			  			}
			  			return true;
			  		}
			  		else {
			  			return true;
			  		}
			  	}
			});
	  		
			$tree.bind('tree.select',function(event) {
		    	if (event.node != null) {
		    		if (selectedrelationnameid == "isPartOf") {checkIsPartOf(event.node.getLevel()-1, event.node.id);}		    		
			    	browseval = event.node.id;
			    	browsename = event.node.name;
			    	
			    	//get type
			    	$.ajax({
						url: "Ingest.action?getCollectionType=",
						data: {newParent: event.node.id},
						success: function(msg) {
							selectedObjectType = msg;
						}
					});
		    	}
		    });
			
			
			$tree.bind('tree.open', function(event) {
		    	$("#ajaxloader").hide();
		    	// Only load from server if opening node that has not been previously opened
		    	if (openedNodes[event.node.id] != true) {
					for(var i1 = 0, childLen = event.node.children.length; i1 < childLen; i1++){
						var childNode = event.node.children[i1];
						var childType = childNode.type;
					    if (childType == "C" || childType == "F" && childNode.children.length > 0) {
					        // Show right content according to button clicked, e.g. only show collections or agents or places etc
                           $tree.tree('loadDataFromUrl', 
                            		   'Ingest.action?listCollections=&selectedrelationname='+selectedrelationnameid+'&rootpid='+childNode.id+'&index='+contentType, childNode
                            );
					    }
				    }		
					
		    	}	
		    	// Set node state to opened
		    	
                openedNodes[event.node.id] = true;
	    	});
			
			
			// Make popup draggable
			$("#browsingDiv").draggable({
				containment: "#wrapper",
				cancel : '#browseTree, #relationTerm' // prevent dragging from tree
			});
			
			 
			
		} // initalizeTree
      
		function checkIsPartOf(nodelevel, parentpid, callback) {	
			$("#browsingDiv .errorlabel").hide();
			$.ajax({
				url: "Ingest.action?checkIsPartOf=",
				data: {nodeLevel: nodelevel, index: contentType, selectedrelationname: selectedrelationnameid, newParent: parentpid},
				success: function(msg) {
					if (callback && typeof(callback) === "function") {
						   callback(msg);
					}
					if (msg != "allowed") {
						$("#browsingDiv .errorlabel").text(msg).show();
					}
				}
			});
		} // checkIsPartOf
		
		function showThumbAfterUpload(filename, thumbnailContainer) {
			var $thumbContainer = thumbnailContainer;
			var filenameUtf8 = encodeURI(JSON.stringify(filename));
			
			$("<img/>", {id: "thumb_"+encodeURI(filename), 
			             title: filename,
			             src: "Ingest.action?watchedFile="+filenameUtf8+"&showThumbAfterUpload="})
             .load(function() {
	           	 if (this.complete) {
	           		 $thumbContainer.show("fade").css("display", "inline-block");
	           		 $(this).appendTo($thumbContainer);
	           	 }
            });
		}
		
		/* DOM READY */
    	$(function() {
    		// Remove file forms from sessionStorage
    		if(getURLParameter("rmst") == "true") {
    			sessionStorage.removeItem("fileForm");
   				sessionStorage.removeItem("attFileForm");
    		}
    		
   			getWorkflowValues();
   			
    		// Base properties for dialog module
    		$("#dialog").dialog({
			    autoOpen: false,
			    modal: true,
			    draggable: false,
			    resizable: false,
			    minWidth: 500,
			    maxHeight: 600,
			    title: "<fmt:message key='header.confirmationrequired'/>",
			    closeText: "<fmt:message key='button.close'/>"
			});
    		  
   			getOrigThumb();
    		if(currentpid != ""){
   				getAttachmentThumbs();
   			}
    		
    		// Serialize the whole form at the page load, so it can be compared on page unload
            initForm = $("form[name=ingest]").serialize();
    		
    		// Set isPartOf-relation
    	    if (contentType == "place" || contentType == "event" || contentType == "action" || contentType == "agent") {
    			$("#relationNAME_isPartOf").val(ingestMessagebundle[contentType]);
    			$("#relationNAME_isPartOf").attr("readonly", "readonly");
    			$("#browse_isPartOf").attr("disabled", "disabled");
    		}
    		
	        /* ************************************
			 * BREADCRUMB NAVIGATION AND PAGE TITLE
			 * ************************************ */

	        if(!currentpid || contentType == "mass" ) {
	        	// Retrieve translated message from message object
	        	// contentType = index, e.g. document
	        	if (contentType == "mass") {
		            $("label#breadcrumb").html("<a href='workspace.jsp'><fmt:message key='link.workspace'/></a> &raquo; "+ingestMessagebundle[contentType]);
		            document.title = ingestMessagebundle[contentType]+" | " + document.title;
	        	} else {
	        		$("label#breadcrumb").html("<a href='workspace.jsp'><fmt:message key='link.workspace'/></a> &raquo; <fmt:message key='link.add'/> "+ingestMessagebundle[contentType].toLowerCase());
                    document.title = "<fmt:message key='link.add'/> " +ingestMessagebundle[contentType].toLowerCase()+" | " + document.title;
	        	}
        	} else {
	        	// Generate breadcrumb via ajax call incase it takes time to construct it
	        	$("label#breadcrumb").html("<fmt:message key='message.creatingpath'/>");
	        	$.ajax({
	        		url: "Ingest.action?getObjectBreadcrumb=",
	        		data: {pid: currentpid},
	        		dataType: "html",
	        		success: function(path){
	                    $("label#breadcrumb").html(path);
	                    // Set currently open object's name to page title
	                    document.title = $("label#breadcrumb .italic").text()+" | "+document.title;
	        		},
	        		error: function(){
	        			$("label#breadcrumb").html("<fmt:message key='error.creatingpath'/>");
	        		}
	        	});
	        }

	        /* *******************************
			 * INHERITANCE & TREE VIEW LISTING
			 * ********************************/
	        
	        // Inherit by manually clicking inherit button
	        $(document).on("click",".manualinherit", function(e) {
	            e.preventDefault();
	            var id = this.id.split("_")[1];
	            var behaviour = $(this).attr("behaviour");
	            
	            // Replaces field's text no matter what
	            if(behaviour == "replaceAll"){
	                $('input[name="'+id+'"]').val($(this).val());
	            }else if(behaviour == "replaceEmpty"){
	            	// Only replaces if field is empty
	                if($('input[name="'+id+'"]').val() == ""){
	                    $('input[name="'+id+'"]').val($(this).val());
	                }
	            }else if(behaviour == "replaceExisting"){
	            	// Replaces if field has text
	                if($('input[name="'+id+'"]').val() != ""){
	                    $('input[name="'+id+'"]').val($(this).val());
	                }
	            }   
	        });

	        if(getURLParameter("isPartOf") != null){
	            var isPartofIndex = $("#isPartOf").val();
	            $("#relationID_"+isPartofIndex).val(getURLParameter("isPartOf"));
	            $("#relationNAME_"+isPartofIndex).val(decodeURIComponent(getURLParameter("isPartOfLabel")));
	            getInheritance(getURLParameter("isPartOf"));
	        }
        
	        if(getURLParameter("original_file") != null){
	            var file = getURLParameter("original_file");
	            $.post("Ingest.action?moveFile=", { watchedFile: file })
	            .done(function(data) {
	                $("#file-uploader").append("<input type='hidden' name='uploadedFile["+filecount+"]' value='"+getURLParameter("original_file")+"'>");
	                filecount++;
	            }); 
	        }
        
	        // Close modal window containing the tree view
	        $("#closeBrowsingDiv").click(function(e){
            	modalWindow(false);
	            $("#browsingDiv").hide();
            	// Reset search field 
            	$("#relationTerm").val("").trigger("input");
            	$("#clearFilter").hide();
	        });
	        
	        // Open modal window containing the tree view
	        $("form").on("click", ".browseBtn",function(e){
	            e.preventDefault();
	            openedNodes = {}; // Reset nodes' states
	        	$("#browseHeader h1").text(this.title);
	            $("#ajaxloader").show();
	            
	            selectedrelation = this.id.split("_")[1];
	            selectedrelationnameid = $(this).attr("name");
	            
	            $('#browseTree').unbind();
	            $('#selectBrowse').unbind();
	            
	            $('#selectBrowse').click(function(){
	                if (browsename != "" && browseval != "") {
	                    $("#relationID_"+selectedrelation).val(browseval);
	                    $("#relationNAME_"+selectedrelation).val(browsename);
	                    
	                    if (selectedrelationnameid == "isPartOf") {
	                        var isPartofIndex = $("#isPartOf").val();
	                        var isPartofValue = $("#relationID_"+isPartofIndex).val();	                       
	                        getInheritance(isPartofValue);
	                    }
	                }
	                modalWindow(false);
	                $("#browsingDiv").hide();
                    // Reset search field 
                    $("#relationTerm").val("").trigger("input");
                    $("#clearFilter").hide();
	            });
	            
	    	    // Store and retrieve the tree view content to/from sessionStorage 
			    if(false){ // Disabled caching for treeviews because Solr handles these effeciently.
			        store = true;   
	                        
					if(sessionStorage.getItem("browsetree"+selectedrelationnameid)){
					    $.getJSON("Ingest.action?getContextTime=",{ajax: 'true'}, function(j){
					        var storageTime = sessionStorage.getItem("ctxtime");
					        if(j > storageTime){
					            $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid, index: contentType}, function(n){
					                sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(n));
					                sessionStorage.setItem("ctxtime",new Date().getTime());
					                listCollectionsJson = n;
					                initalizeTree(n, "#browseTree");
					             });
					        }else{
					            // TODO: check this
					            if (sessionStorage.getItem("browsetree"+selectedrelationnameid) != null) {
					                initalizeTree(JSON.parse(sessionStorage.getItem("browsetree"+selectedrelationnameid)));
					            } else {
					                $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid, index: contentType}, function(n){
					                    sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(n));
					                    sessionStorage.setItem("ctxtime",new Date().getTime());
					                    listCollectionsJson = n;
					                    initalizeTree(n, "#browseTree");
					                });
					            }
					        }
					    });    
					}else{ // if sessionStorage
				    	$.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid, index: contentType}, function(j){
					        sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(j));
					        sessionStorage.setItem("ctxtime",new Date().getTime());
					        listCollectionsJson = j;
					        initalizeTree(j, "#browseTree");
					    });
					}	      
				/* IN USE */
				} else { // if typeof
				    $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid, index: contentType}, function(j){
				    	listCollectionsJson = j;
				    	initalizeTree(j, "#browseTree");
				    }).fail(function() {
				    	$("#ajaxloader").hide();
				    	$("#ajaxerror").show();
				    });
			        modalWindow(true);
			        $("#browsingDiv").show();
				}     
			});
	        
	        // Browse tree view filtering
	        $("#relationTerm").on("input",function(){
	    		var relationSearchTerm = this.value;
	    		var html = "";
	    		var delayLength = 500; // ms
	    		$(".jqtree_common").css( "background-color", "" );
	    		if($(this).val().length >= 3) { // Only fire if inputted 3 or more characters
    			
	    			// Custom function to make a delay for given time
		    		delay(function(){
		    			$("#relationFilterLoader").show("fade");
		    			$("#clearFilter").show("fade"); 	// Show clear-button
		    			$("#filteredRelation").empty(); 	// Clear list element before every load so it wont become messy
		    			// Hides the div containing the tree, after animation has ended, display div containing filtered results
		    			$("#browseTree").hide("fade",function(){
		    				$("#filteredRelationContainer").show();	
		    			});
					   
					    
		    			$.ajax({
		    				url: "Search.action?relationSearch=",
		    				data: {relationSearchTerm: encodeURI(relationSearchTerm), facetName: selectedrelationnameid, rootpid: ':root'},
		    				dataType: "json",
		    				success: function(data) {
		    					$("#relationFilterLoader").hide("fade");
		    					$("#filteredFound").show();
		    				//	$('#filteredFound').tree('loadData', data);
		    					initalizeTree(data, "#filteredFound");			    					
		    					// $(".jqtree_common:contains("+relationSearchTerm+")").css( "background-color", "rgb(1,1,30) " );
		    						
		    					
		    				},
		    				error: function() {
		    					$("#relationFilterLoader").attr("src", "img/icons/silk/error.png");
		    				}
		    			});
		    		},delayLength);
	    			
    			} else {
	    			
	    			// Custom function to make a delay for given time
	    			delay(function(){
	    				// Hides the div containing filtered results, after animation has ended, display div containing the tree
	    				// and empty filtered results and label containing the number of results
	    				$("#relationFilterLoader").hide("fade");
	    				$("#clearFilter").hide("fade");
	    				$("#filteredFound").empty();
	    				$("#relationTerm").val("");	    	        	
	    				$("#filteredRelationContainer, #filteredFound").hide("fade",function(){
	    					$("#filteredRelation, #filteredFound span").empty();
	    					$("#filteredFound").hide();
		    				$("#browseTree").show();
		    						    			});
		    		 },delayLength);
	    		}
	    	});
	        // Clear filter field and results
	        $("#clearFilter").click(function(){
	        	$("#relationTerm").val("");
	        	$(this).hide("fade");
	        	$("#relationFilterLoader").hide("fade");
	        	$("#filteredFound").empty();
	        	$("#filteredRelationContainer, #filteredFound").hide("fade",function(){
					$("#filteredRelation, #filteredFound span").empty();
					$("#filteredFound").hide();
    				$("#browseTree").show();    				
	        	});
	        });
	        
	        $("#filteredRelation").on("click", "li", function(){
	        	var selectedPid = this.id;
	        	var selectedName = $.trim($(this).text()); // Using jQuery's $.trim()-method instead of native js trim-method to support IE 9>...
		  		// Block user from selecting same object that is currently being edited
	        	if(selectedPid === currentpid) {
		  			$(".errorlabel").show(0).delay(1000).fadeOut(800);
		  			return false;
		  		} else {
		  			// Remove highlight class from all matching elements
		        	$("#filteredRelation li").removeClass("selectedListItem");
		        	// Add highlight class to clicked element
		        	$(this).addClass("selectedListItem");
		        	browsename = selectedName; 
		        	browseval = selectedPid;
		  		}
	        });
	        
			/* ******************************
			 * TEXT FIELD-SPECIFIC OPERATIONS
			 * ****************************** */
	        
			// Insert and remove loader icon from relation fields
			$("form").on("keyup", ".textrelation:not([readonly])", function(e){
		    	indicatorRow = this.id.split("_")[1];
			    if(this.value.length > 1) {
			        $(this).addClass("fieldloaderwithimg");
			    }else {
			    	$(this).removeClass("fieldloaderwithimg");
			    }
			    // Backspace
			    if (e.keyCode == 8 && this.value == "") {
			    	$("#relationID_"+indicatorRow).val("");
			    }
			});
                
			// Checks relation on focus loss
			// If given pid is same as the currently open object, notify user
			$("form").on("blur", ".textrelation:not([readonly])", function(){
				$(this).removeClass("fieldloaderwithimg");
			    var rowid = this.id.split("_")[1];
			    var $relerrorimg = $(".errorimg"+rowid);
			    
			    if(currentpid) // currentpid = object that is open at that moment
			    {
			        if($(this).val() == currentpid)
			        {
			            $(this).addClass("fielderror");
			            $(this).prop("title","<fmt:message key='error.relationitself'/>");
			            // Draw icon only if there isn't one already
			            if($relerrorimg.length == 0) {
			            	// To draw icon after the right field
			                $("#browse_"+rowid).after(" <img src='img/icons/silk/error.png' class='errorimg"+rowid+"' title='<fmt:message key='error.relationitself'/>'/>");
			            }
			            $(".disable-on-error").attr("disabled","disabled");
			        } else {
			            $(this).removeClass("fielderror");
			            $(this).removeAttr("title");
			            // Remove error icon after the right button
			            $("#browse_"+rowid).next(".errorimg"+rowid).remove();
			            $(".disable-on-error").removeAttr("disabled");
			        }
			    }
			});
            
			// Display error icon on required field if the field is empty when losing focus
			// and disable edit/add buttons
			$(".required-true").blur(function(){
			
			
		        $("#formError").remove();
			    if(this.value == "")
			    {
			        $(this).addClass("fielderrorwithimg");
			        $(this).prop("title","<fmt:message key='error.cannotbeempty'/>");
			        $(".disable-on-error").attr("disabled","disabled");
			        $("#button-row input[type=submit]").last().after(" <span id='formError' class='errorlabel'><fmt:message key='error.fixerrors'/></span>");
			
			    
			      $("#tabs li a[href=#"+$(this).closest(".tab_content").attr('id')+"]").attr("class","errorlabel");
		
			    } else {
			    	// If field has text when losing focus, hide error icon
			    	// and enable edit/add buttons
			    	$("#tabs li a[href=#"+$(this).closest(".tab_content").attr('id')+"]").removeClass("errorlabel");
			        $(this).removeClass("fielderrorwithimg");
			        $(this).removeAttr("title");
			        $(".disable-on-error").removeAttr("disabled");
			    }
			});
                               
			$(".textrelation").autocomplete({
			    source: function( request, response ) {
			        var store = false;
			        var term = request.term;

			        delay(function(){
						$.ajax({
						    timeout: 35000,
						    data: {'term': term},
						    url: "Ingest.action?findRelations=",
						    global: false,     // this makes sure ajaxStart is not triggered
						    dataType: 'json',
						    success: function( data ) {
						    	// Remove loader icon from the corresponding field
						    	$("#relationNAME_"+indicatorRow).removeClass("fieldloaderwithimg");
						        if(store){
						            sessionStorage.setItem(term, JSON.stringify(data));
						            sessionStorage.setItem("ctxtimeAuto",new Date().getTime());
						        }
						        response(data);
						        return;                               
						    }
						});
				    }, 500);
			    },
			    minLength: 2,
			    select: function( event, ui ) {
			    	event.preventDefault();
			    	var name = ui.item.label.split("/");
			    	var pid = ui.item.value;
			    	$("#relationNAME_"+indicatorRow).val(name[name.length-1]); // Get the last index of array (object name)
			    	$("#relationID_"+indicatorRow).val(pid);
			    }
			});
			
			/* ***********************
			 * BUTTON & SELECT ACTIONS
			 * ***********************/
						 
			// Deletes attachment file
			$('#att-thumbnail-container').on("click", ".deleteattachmentbtn", function(e){
				var dsid = $(this).attr("id");
	            $(".confirmtext").empty();
	            
	            $(".confirmtext").html("<fmt:message key='message.deleteattachment'/>");
	            $("#dialog").dialog('option', 'buttons', {
	            	// Confirm button
					"<fmt:message key='button.confirm'/>": function () {
					      $.ajax({
					    	  // dsid = datastream id; attachment, original etc
					          data: {pid : currentpid, deleteAttachment : dsid},
					          url: "Ingest.action?removeAttachment=",
					          global: false,     // this makes sure ajaxStart is not triggered
					          success: function( data ) {
					        	  // Removes correct thumbnail from the list
					              $("#attspan_"+dsid).fadeOut(function(){
					            	  $(this).remove();
					              });
					          }
					      });
					      $(this).dialog("close");
					      
					      // If there are no attachments after delete, hide background div
					      if($(".attimgcontainer").length <= 1) {
					    	  $("#att-thumbnail-container").append("<fmt:message key='header.noattachmentfound'/>");
					      }
					},
					// Cancel button
					"<fmt:message key='button.cancel'/>": function () {
					      $(this).dialog("close"); 
					}
	            });
	
	            $("#dialog").dialog("open");
	        });
			
			// Object deletion functionality
            $("#delete").on("click", function(e){
				e.preventDefault();
				
				var $delBtn  = $(this);
				var origText = this.value;
				
				var objectsPid = currentpid;
				var childrenArray = [];

				var deletemsg   = "<fmt:message key='message.disposemethod.delete'/>";
				var disposemsg  = "<fmt:message key='message.disposemethod.dispose'/>";
				var deletedmsg  = "<fmt:message key='message.deleted'/>";
				var disposedmsg = "<fmt:message key='message.disposed'/>";
				var deniedmsg   = "<fmt:message key='message.deletedenied'/>";
                // Needed to make translated messages work
				var deleteStart = "<fmt:message key='message.delete.start'/>"; //"The following object ";
                var noChildren = "<fmt:message key='message.delete.nochildren'/>"; //" which has no children will be deleted";
                var deleteChildren = "<fmt:message key='message.delete.children'/>"; //" and its child objects will be deleted";                  
                var deleteParent_notAllowed = "<fmt:message key='message.delete.parentnotallowed'/>"; //" cannot be deleted while it has child objects";
                var deleteParent_leaveOrphanes = "<fmt:message key='message.delete.leaveorphanes'/>"; //" will be deleted and its child objects will become orphaned";
				
				// Change button text to inform user that something is happening
				$delBtn.val("<fmt:message key='message.working'/>");
				// Fetchs to be deleted objects from server
				// Takes config into account
				$.ajax({
			        url: "Disposal.action?ajaxGetItems=",
			        data: {pid: objectsPid},
			        dataType: "json",
			        global: false,
			        success: function(data){
			        	// Change button text back to default
			            $("#delete").html("<fmt:message key='button.delete'/>");
			        	// Reset element content
			            $(".confirmtext").empty();
			        	// Change info message according to dispose method
			        	
			            if (data[0].method == "dispose") {
			            	$(".confirmtext").html(disposemsg + ":<br/>");
			            } else {
			            	$(".confirmtext").html(deletemsg + ":<br/>");
			            }
			        	
			        	if (data[0].deletionMsg == "noChildren"){
                            $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + noChildren + "<br/>");
                        }
                        else if(data[0].deletionMsg == "deleteChildren"){
                            $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + deleteChildren + ":<br/>");
                        }
                        else if(data[0].deletionMsg == "deleteParent_leaveOrphanes"){
                            $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + deleteParent_leaveOrphanes + ":<br/>");
                        }
                        else if(data[0].deletionMsg == "deleteParent_notAllowed"){
                            $(".confirmtext").append("<i>" + data[0].name + "</i> " + deleteParent_notAllowed + ":<br/>");
                        }

			            if (data[0].deletionMsg != "deleteParent_notAllowed"){
			            	
			            	for(var i = 0; i < data.length; i++) {
	                            // Displays list of about-to-be-deleted objects
	                            $(".confirmtext").append("<img src='img/icons/silk/"+types[data[i].type]+"' class='smallicon' alt='"+types[data[i].type]+"'/>"+" <span class='deletelistitem'>"+data[i].name+"</span><br/>");
	                            // Makes an array of pids that will be deleted
	                            childrenArray.push(data[i].id);
	                        }
				            // Does the actual deletion
				            $("#dialog").dialog('option', 'buttons', {
				            	// Confirm button
				            	"<fmt:message key='button.confirm'/>": function () {
				                	$(".ui-dialog-buttonpane").css("background", "url(img/ajax-loader-bar.gif) no-repeat scroll 20px 10px");
				                	$(".ui-dialog-buttonpane").css("padding-left", "50px");
				                	$(".ui-dialog-buttonpane").html("<fmt:message key='message.working'/>");
				                	$.ajax({
	                 					url: "Disposal.action?ajaxDeleteItems=",//"Ingest.action?ajaxDeleteItems=",
										data: {childrenPids: childrenArray},
										global: false,
										success: function(data){
											$(".ui-dialog-content").hide();
											$(".ui-dialog-buttonpane").css("background", "none");
											$(".ui-dialog-buttonpane").css("padding-left", "0.4em");
											$(".ui-dialog-buttonpane").css("border-width", "0px");
											if(data == "deleted") {$(".ui-dialog-buttonpane").html("<fmt:message key='general.objects'/> " + deletedmsg + " <fmt:message key='message.redirect.viewpage'/>");}
											else if(data == "denied") {$(".ui-dialog-buttonpane").html(deniedmsg);}
											else {$(".ui-dialog-buttonpane").html("<fmt:message key='general.objects'/> " + disposedmsg + " <fmt:message key='message.redirect.viewpage'/>");}
									  	},
										complete: function () {
											// Redirect to view-page after 2 seconds
						      				setTimeout(function (){ window.location.href = "browse.jsp"; }, 2000);
										}
				                	});
				              	},
				              	// Cancel button
				              	"<fmt:message key='button.cancel'/>": function () {
				                	$(this).dialog("close"); 
				                	$delBtn.val(origText);
				              	}
				            });
			            } else {
            	
			            	$("#dialog").dialog('option', 'buttons', {
			            		// Cancel button
                                "<fmt:message key='button.cancel'/>": function () {
                                    $(this).dialog("close"); 
                                    $delBtn.val(origText);
                                }
			            	});
			            }
			
			            $("#dialog").dialog("open");
			        }
			    });
			});
                  
          	$(".add-nested-element").click(function(e){
          		var $this       = $(this);
          		var mdElement   = $this.attr("id").split("_")[1];
          		var html        = "";
          		loopIndex[mdElement]++;
          		
          		$.ajax({
          			url: "Ingest.action?getNestedChildrenJson=",
          			data: {mdElement: mdElement, index: contentType},
          			dataType: "json",
          			success: function(children) {
          				var fieldname = "${fieldname}";
          				html += '<s:form partial="true" action="#">';
          				html += '<div class="nestedElement positionRelative multifieldElem">';
          				html += '<img src="img/icons/silk/cancel.png" class="helpicon pointerCursor delete-nested-element" name="'+mdElement+'" title="<fmt:message key="button.delete"/>"/>';
          				for(var i = 0; i < children.length; i++){
          					var child = children[i];
          					if (child.fieldType == "relation") {
          						html += '<p><sd:text name="browsename" id="relationNAME_'+child.name+loopIndex[mdElement]+'" class="nestedInput textrelation" placeholder="'+child.placeholder+'"/> ';
          						html += '<button id="browse_'+child.name+loopIndex[mdElement]+'" name="'+child.name+'" class="browseBtn sub-form-button" title="'+child.placeholder+'">...</button>';
          						html += '<s:hidden name="'+fieldname+'['+mdElement+'].nestedElements['+loopIndex[mdElement]+']['+child.name+'].value" id="relationID_'+child.name+loopIndex[mdElement]+'" class="nestedInput"/>';
          					    html += '<s:hidden name="'+fieldname+'['+mdElement+'].nestedElements['+loopIndex[mdElement]+']['+child.name+'].name" value="'+child.name+'" /></p>';
          					}
          					else if (child.fieldType == "select") {
          						html += '<p><s:select name="'+fieldname+'['+mdElement+'].nestedElements['+loopIndex[mdElement]+']['+mdElement+'Role].value" class="nestedInput selectsearch">';
          						html += '<s:option value=""><fmt:message key="general.default"/></s:option>';
          						for (var j = 0; j < child.enumValues.length; j++) {
          							html += '<s:option value="'+child.enumValues[j]+'">'+child.enumValuesLocal[j]+'</s:option>';
          						} // for
          						html += '</s:select>';
          						html += '<s:hidden name="'+fieldname+'['+mdElement+'].nestedElements['+loopIndex[mdElement]+']['+child.name+'].name" value="'+child.name+'" /></p>';
          					}
           				} // for
           				html += '</div>';
          				html += '</s:form>';
          				$this.before(html); // Insert generated HTML before the clicked button
          			} // success
          		}); // ajax
          	});
          	
          	$("form").on("click", ".delete-nested-element", function(e){
          		var mdElement = this.name;
                if ($("#"+mdElement+"-div").find(".nestedElement").length == 1) {
                	// Last delete button clears fields assosiated with the button and removes the button
                    $(this).parent().find(".nestedInput").val("");
                    $(this).remove();
                } else {
                	// Otherwise it removes the fields
                    $(this).parent().remove();
                }
          	});
          	
          	$("form").on("click", ".delete-multifield-element", function(e){
          	    $(this).parent().remove();
          	});
          	
          	$(".add-multifield-element").click(function(e){
          	    var html                 = ""; 
          		var $addMultifieldBtn    = $(this);
          	    var mdElement            = this.id.split("_")[1];
          	    var fieldname            = "${fieldname}";
          	    loopIndex[mdElement]++;
          	    
          	    $.getJSON("Ingest.action?getMultifieldElement=",{index: contentType, mdElement: mdElement}, function(element){
	          	    html += '<s:form partial="true" action="#">';
	          	    html += '<div class="multifieldElem positionRelative">'
          	    	html += '<img src="img/icons/silk/cancel.png" class="helpicon pointerCursor delete-multifield-element" name="'+mdElement+'" title="<fmt:message key="button.delete"/>"/>';
          	    	
          	    	if (element.fieldType == "relation") {
          	    		html += '<s:text name="browsename" id="relationNAME_'+element.name+loopIndex[mdElement]+'" value="" class="textrelation"/> ';
                        html += '<s:hidden name="'+fieldname+'['+mdElement+'].values['+loopIndex[mdElement]+']" id="relationID_'+element.name+loopIndex[mdElement]+'"/>';
                        html += '<button id="browse_'+element.name+loopIndex[mdElement]+'" name="'+element.name+'" class="browseBtn sub-form-button" title="'+element.placeholder+'">...</button>';
          	    	}
          	    	else if (element.fieldType == "textfield") {
          	    		html += '<s:text name="'+fieldname+'['+element.name+'].values['+loopIndex[mdElement]+']" class="textsearch"/> ';
          	    	}
	          	    html += '</div>';
	          	    html += '</s:form>';
	          	    
	          	    $addMultifieldBtn.before(html);
          	    }); // getJSON
          	});
          	
          	// Decade & century selections for time range
          	var selectId = "";
	       	$(".openTimeRangeSelect").click(function(e){
	       		e.preventDefault();
	       		$(this.parentNode.parentNode).find("select").remove();
	       		
	       		var $timerangeSelect = $("<select/>");
	       		$timerangeSelect.attr("id", "timeRangeSelect");
	       		
	       		selectId = this.id;
	         	var currYear = new Date().getFullYear();

	         	$timerangeSelect.append("<option value=''><fmt:message key='general.default'/></option>");
	         	
	         	if (this.id == "centuries") {
	         		for (var c = currYear; c > 1300; c -= 100) {
		        	    var cFloor = Math.floor(c / 100) * 100;
		        	    $timerangeSelect.append("<option value='"+cFloor+"'>"+cFloor+"</option>");
		        	}
	         	}
	         	else if (this.id == "decades") {
		        	for (var d = currYear; d > 1300; d -= 10) {
		        		var dFloor = Math.floor(d / 10) * 10;
		        		$timerangeSelect.append("<option value='"+dFloor+"'>"+dFloor+"</option>");
		        	}
	         	}
	         	$timerangeSelect.appendTo($(this).parent());
	       	});
          	
        	$(".timeRangeSelectContainer").on("change", "select", function(){
        	    var year = parseInt(this.value);
        	    var dBegin = new Date(year, 0, 1);
        	    var dEnd = "";
        	    if(selectId == "decades") {
        	    	dEnd = new Date((year+9), 11, 31);	
        	    } else {
        	    	dEnd = new Date((year+99), 11, 31);
        	    }
        	
        	    var beginString = dBegin.getFullYear()+"-"+padZero((dBegin.getMonth()+1))+"-"+padZero(dBegin.getDate());
        	    var endString = dEnd.getFullYear()+"-"+padZero((dEnd.getMonth()+1))+"-"+padZero(dEnd.getDate());
        	    
        	    $("#temporalBegin").val(beginString);
        	    $("#temporalEnd").val(endString);
        	    
        	    $(this).hide();
        	});
        	// Hide div containing the selects when clicking outside of it
        	$(document).mouseup(function(e){
        	    hideElementOnFocusLoss($("#timeRangeSelect"), e);
   			});
        	
        	//add element to basket
        	  $(".download-container").on("click", ".basketaction", function(e){
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
         	/* *****************************************
			 * PAGE FUNCTIONALITY & EVENT MODIFICATIONS
			 * ***************************************** */

            var openMethod = "tab"; //"tab" or "popup"
            //open popup for object insert type selection
        	$("#createNewPopup").on("click", function() {
        		var type;	
        		if(selectedObjectType != ""){
        			type = selectedObjectType;
        		}
        		else{
        			type = "basecollection";
        		}
        			
        		var address = [insertURL.slice(0, insertURL.indexOf("index=")+"index=".length), type, insertURL.slice(insertURL.indexOf("index=")+"index=".length)].join('');
        		window.open(address, '_blank');
			});
            
        	//hide fprm if button is pressed
        	$("#uploadedFormForPopup").on('click', $("#uploadedFormForPopup").find("button"), function() {          
                $("#uploadedFormForPopup").dialog('close');
        	});
        	 
        	 //ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix
            $( "#closeBrowsingDiv" ).click(function() {
        	   $("#uploadedFormForPopup").dialog('open');
        	});
        	 
	        // To disable enter key functionality if not in textarea, so pressing it does not open treeview window
	        $('form').bind("keypress", function (e) {
	        	var elem = e.srcElement || e.target;
	            if (e.keyCode == 13 && elem.tagName.toLowerCase() !== "textarea") return false;
	        });
            
			// Enable select-field containing the object type at form submit,
			// otherwise it will not post the value
			$("form[name=ingest]").on("submit", function(){
			    $(this).find("select").removeAttr("disabled");
			    $(this).find("input[type=checkbox]").removeAttr("disabled");
			});
          	
			$(".object-container, .download-container").on("click", "a", function() {
				formSubmit = true;
			});
			
	      	// If submitting form, do not warn user about leaving page
            $("#button-row input, #button-row button").click(function(e){
                formSubmit = true;
                // To add validation to nested elements
                var $textfield = $(".nestedElement").find("input[type='text']");
                var $select = $(".nestedElement").find("select");
                validateNestedElements([$textfield, $select], ["select", "input[type='text']"]);
            });
            
            // To prompt user before leaving page if form has changed and neither add nor edit button has been clicked
            window.onbeforeunload = function() {
                var finalForm = $("form[name=ingest]").serialize();
                if(initForm != finalForm && formSubmit === false){
                    return "Do you really want to leave?";
                }
            }

            /****************************
             * FILE UPLOAD (multi-upload)
             *************************** */
             
         	// Create file uploader only if matching element is in DOM
			// Otherwise it will stop js execution below this point
			if($("#file-uploader").length > 0)
			{
			    var uploader = new qq.FileUploader({
			        element: document.getElementById('file-uploader'),
			        //listElement: $(".qq-uploader")[0],
			        action: "Ingest.action?xhrUpload=",
			        inputName: "fileToUpload",
			        multiple: false,
			        maxConnections: ${gui.getQueueSize()}, // # of files to upload at once, aka "the queue"
			        onComplete: function(id, fileName, responseJSON){
			            $("#file-uploader").append("<input type='hidden' id='uploadedFile' name='uploadedFile["+filecount+"]' value='"+encodeURI(JSON.stringify(fileName))+"'>");
			            filecount++;
	         			// Disable file input
			            $("#file-uploader .qq-upload-button input").attr("disabled", "disabled");
			         	// Add disabled-style class to button
	         			$("#file-uploader .qq-upload-button").addClass("qq-upload-button-disabled");
			         	// Create delete-button and 
			            $("#file-uploader .qq-upload-button").after("<button id='qq-delete-orig-button' name='"+encodeURI(fileName)+"'><fmt:message key='button.delete'/></button>");
			        	// Set current uploaded files form to sessionStorage for later retrieval
			         	sessionStorage.setItem("fileForm", JSON.stringify($("#file-uploader").html()));
			         	showThumbAfterUpload(encodeURI(fileName), $("#orig-thumbnail-container"));
			        },
			        dragText: "<fmt:message key='upload.drag'/>",      
			        uploadButtonText: "<fmt:message key='upload.filebutton'/>",
			        cancelButtonText: "<fmt:message key='upload.cancel'/>",
			        failUploadText: "<fmt:message key='upload.failed'/>",
			    }); 
			}
			
         	// Retrieve file form from sessionStorage if it is set and display it on page
         	// Used f.ex during validation errors that reload the page
            if(sessionStorage.getItem("fileForm") != null){
            	$("#file-uploader").html(JSON.parse(sessionStorage.getItem("fileForm")));
            }
         	
			// Create file uploader only if matching element is in DOM
			// Otherwise it will stop js execution below this point
			if($("#attachment-uploader").length > 0)
			{
				var html = "";
			    var attachment_uploader = new qq.FileUploader({
			        element: document.getElementById('attachment-uploader'),
			        //listElement: $(".qq-uploader")[0],
			        action: "Ingest.action?xhrUpload=",
			        multiple: true,
			        inputName: "attachmentToUpload",
			        maxConnections: ${gui.getQueueSize()}, // # of files to upload at once, aka "the queue"
			        onComplete: function(id, fileName, responseJSON){
			        	// Creates input fields for changing attachment's filename and buttons for deleting attachments before submitting form
			        	$("#attachment-uploader").append("<input type='hidden' name='uploadedAttachment["+attachmentcount+"]' value='"+encodeURI(JSON.stringify(fileName))+"' />");
			        	$("#attachment-uploader").append("<input type='text' id='id-"+encodeURI(fileName)+"' name='attachmentNames["+encodeURI(JSON.stringify(fileName))+"]' value='"+fileName+"' /> ");
			            $("#attachment-uploader").append("<button class='qq-delete-attachment-button' name='"+encodeURI(fileName)+"'><fmt:message key='button.delete'/></button><br/>");
			            attachmentcount++;
			            sessionStorage.setItem("attFileForm", JSON.stringify($("#attachment-uploader").html()));
			        },
			        dragText: "<fmt:message key='upload.drag'/>",
			        uploadButtonText: "<fmt:message key='upload.attachmentbutton'/>",
			        cancelButtonText: "<fmt:message key='upload.cancel'/>",
			        failUploadText: "<fmt:message key='upload.failed'/>",
			    });
			}
			
			// Retrieve file form from sessionStorage if it is set and display it on page
         	// Used f.ex during validation errors that reload the page
            if(sessionStorage.getItem("attFileForm") != null){
            	$("#attachment-uploader").html(JSON.parse(sessionStorage.getItem("attFileForm")));
            }
			
			// Removes original file from uploaded files list and from server,
			// so user can upload a new one
			$("#file-uploader").on("click","#qq-delete-orig-button",function(e){
				e.preventDefault();
				var file = $(this).attr("name");
				var $thumbContainer = $("#orig-thumbnail-container");
				
				$.ajax({
					url: "Ingest.action?removeFileFromUpload=",
					data: {fileToDelete: file},
					dataType: "text",
					success: function(data){
						// Remove the hidden file input where previously uploaded file is
						$("#uploadedFile").remove();
						// Remove list of uploaded files and delete button
						$("#file-uploader .qq-upload-list li, #qq-delete-orig-button").remove();
						// Enable file input so user can upload a new file
			            $("#file-uploader .qq-upload-button input").removeAttr("disabled");
						// Remove disabled-style class from button
			            $("#file-uploader .qq-upload-button").removeClass("qq-upload-button-disabled");
						// Reset the file count
			            filecount = 0;
						// Delete saved form from sessionStorage
			            sessionStorage.removeItem("fileForm");
						
						var escaped = file.split(".").join("\\.").split("(").join("\\(").split(")").join("\\)");
			            $thumbContainer.find("[id='thumb_"+escaped+"']").remove();
			            if ($thumbContainer.find("img").length <= 0) {
			            	$thumbContainer.hide("fade");
			            }
					}
				});
			});
			
			$("#attachment-uploader").on("click", ".qq-delete-attachment-button", function(e){
				e.preventDefault();
				var attachmentFile = $(this).attr("name").replace(".","\.");
				var $this = $(this);
				var $line = $("#attachment-uploader .qq-upload-list li");
				var $hiddenInput = $("#attachment-uploader input[type=hidden]");
				$.ajax({
					url: "Ingest.action?removeFileFromUpload=",
					data: {fileToDelete: attachmentFile},
					dataType: "text",
					success: function(data){
						if(data == "true") {
							// Loop through every matching element and search their classes for matching string
							$line.each(function(){
								if($(this).hasClass("file-"+attachmentFile)){
									// If has matching string as class, remove it from list
									$(this).remove(); // li-element
								}
							});
							
							$hiddenInput.each(function(){
								if(this.value == encodeURI(JSON.stringify(attachmentFile))) {
									$(this).remove(); // hidden input
								} 
							});
							// Remove text input that matches pressed button, escaping dots in string
							// Without escaping, jQuery selector would intepret string after dot as a class, not a part of id
							var escaped = attachmentFile.split(".").join("\\.").split("(").join("\\(").split(")").join("\\)");
							$("#attachment-uploader").find("[id='id-"+escaped+"']").remove();
							$this.remove(); // Button
 							$this.next().remove(); // Following br-tag
							// If all of the uploaded attachment files were deleted before submitting form, delete form from sessionStorage
							if($line.length <= 1){
								sessionStorage.removeItem("attFileForm");
							} else {
								// Otherwise set current form to sessionStorage for retrieval
								sessionStorage.setItem("attFileForm", JSON.stringify($("#attachment-uploader").html()));
							}
						}
					}
				});
			});			
        }); // DOM ready
        </script>
    </s:layout-component>
    
    <s:layout-component name="content">
        <s:messages/>
        
	    <h1 id="mainheader" class="largeheader-nomargin">
	        <c:if test="${empty pid}">
	            <c:set var="elem" value="title"/>
		        <fmt:message key="link.add-${index}"/>
	        </c:if>
	        <c:if test="${!empty pid}">
		        <c:set var="elem" value="${(empty bean['title'].value) ? 'preferredName' : 'title'}"/>
                <!-- Displays object's title or preferred name -->
	            ${bean[elem].value}
	        </c:if>
	    </h1>
	    <label class="pagehelp"><img src="img/icons/silk/help.png" class="smallicon" alt="[?]"/> <fmt:message key="pagehelp.${index}"/></label><br/>
	    
	    <div class="ingest-header-container">
		    <c:if test="${previousUrl == true}">
		        <s:link href="Search.action?previousSearch=1&search=" class="previousSearchLink"> &laquo; <fmt:message key="link.lastsearch"/></s:link>&nbsp;&nbsp;&nbsp;
		    </c:if>
	        <div class="download-container floatRight">
			    <c:if test="${!empty pid && index != 'mass' && user.getHighestAccessRight() >= 50}">
			        <s:link href="Ingest.action?pid=${pid}&plainView=true&view=" class="previousSearchLink">
	                    <img src="img/icons/silk/page.png" class="smallicon" title="<fmt:message key="link.view"/>" alt="<fmt:message key="link.view"/>"/>
			        </s:link>
					<osa:show requiredLevel="20" objectId="${pid}">
                        <s:link href="Workflow.action?pid=${pid}&downloadObject="><img src="img/icons/silk/compress.png" alt="tar.gz" class="smallicon" title="${downloadtargz}"/></s:link>
                    </osa:show>
                    <osa:show requiredLevel="20" objectId="${pid}">
                        <s:link href="Workflow.action?pid=${pid}&downloadObject="><img src="img/icons/silk/compress.png" alt="zip" class="smallicon" title="${downloadzip}"/></s:link>
                    </osa:show>
                    <c:if test="${!user.isAnonymous()}">
	                    <c:set var="exists" value="${basketaction.checkIfExists(pid)}"/>
	                    <c:if test="${exists}">
	                        <img src="img/icons/silk/basket_delete.png" id="${pid}" data-action="deleteFromBasket" class="basketaction pointerCursor smallicon" title="${removefrombasket}" alt="[-]"/>
	                    </c:if>
	                    <c:if test="${!exists}">
	                        <img src="img/icons/silk/basket_put.png" id="${pid}" data-action="addToBasket" class="basketaction pointerCursor smallicon" title="${addtobasket}" alt="[+]"/>
	                    </c:if>
                    </c:if>
			    </c:if>
		    </div>
	    </div>
	    <div class="header-hr"></div>
	
	    <s:form name="ingest" action="/Ingest.action" focus="${fieldname}[${elem}].value"> <%-- Set focus to title field on page load --%>
	        <s:hidden name="index" value="${index}" />
	        
	        <!-- Create tabs to the form -->
	        <div id="tabs_wrapper">
				<c:if test="${(index == 'audio' 
	                             || index == 'document' 
	                             || index == 'drawing' 
	                             || index == 'image'
	                             || index == 'map' 
	                             || index == 'movingimage')}">
	                <div id="orig-thumbnail-container" class="object-container"></div>
	            </c:if>
	            <c:if test="${param.metadata == 1}">
	                <div id="metadatafiles" class="object-container">
	                    <c:forEach var="metadatafile" items="${actionBean.watchedFiles}">
	                        ${metadatafile}<br/>
	                    </c:forEach>
	                </div>
	            </c:if>
	            <div id="tabs_container">
	                <ul id="tabs">
	                    <c:forEach items="${tabs}" var="tab" varStatus="loop">
	                        <c:if test="${loop.index == 0}">
	                            <li class="tab-item active"><a href="#${tab}"><fmt:message key="capture.${tab}"/></a></li>
	                        </c:if>
	                        <c:if test="${loop.index != 0}">
	                            <li class="tab-item"><a href="#${tab}"><fmt:message key="capture.${tab}"/></a></li>
	                        </c:if>
	                    </c:forEach>
	                </ul>
	            </div>
	            
	            <div id="tabs_content_container" class="ingest-tabs_content_container">
	               <c:forEach items="${tabs}" var="currenttab" varStatus="looptab">
		                <div id="${currenttab}" class="tab_content">
		                <c:forEach items="${names}" var="currentelement" varStatus="loop">
		           	
		           			<%-- Set to variables so code looks more neat --%>
		                	<c:set var="mdElement" value="${currentelement.key}"/>
		                	<c:set var="formElement" value="${currentelement.value}"/>
		                    <fmt:message key="tooltip.${mdElement}.shorthelp" var="shorthelp"/>
		                    
		                    <c:if test="${formElement.tab == currenttab}">
		                    	<%-- 
		                    		Renders different kind of form elements by using stripe's layout-render component.
		                    		Supplies the renderer with element-specific jsp files that contain the markup.
		                    		Pass values to renderer via attributes
		                    	--%>
	                            <s:layout-render name="/layout/components/ingest/${formElement.fieldType}.jsp" 
		                            mdElement="${mdElement}" 
		                            formElement="${formElement}" 
		                            pid="${pid}" 
		                            shorthelp="${shorthelp}" 
		                            pageIndex="${index}" 
		                            fieldname="${fieldname}"
		                        />
		                    </c:if>
		                </c:forEach>
		                </div>
	                </c:forEach>
	                <div class="header-hr"></div>
	                
	                <%-- Check visible buttons --%>
	                <p id="button-row">
	                
		                <%-- If creating new object --%>
		                <c:if test="${pid == null}">
	                        <%-- If doing mass edit --%>
		                    <c:if test="${index eq 'mass'}">
	                            <s:submit name="edit" class="disable-on-error"><fmt:message key="button.update.mass"/></s:submit>
	                        </c:if>
	                        
	                        <%-- If doing normal ingest --%>
	                        <c:if test="${index != 'mass'}">
	                        
	                            <%-- If adding metadata through workflow --%>
		                        <c:if test="${param.metadata == 1}">
		                            <s:submit name="addMetaDataFiles"><fmt:message key="button.add.metadatafiles"/></s:submit>
		                        </c:if>
		                        
		                        <%-- If doing normal ingest --%>
		                        <c:if test="${param.metadata != 1}">
			                       <s:submit name="add" class="disable-on-error"><fmt:message key="button.add"/></s:submit>
			                       <s:submit name="addNew" class="disable-on-error" title="${addNewButtonTooltip}"><fmt:message key="button.addNew"/></s:submit>
			                    </c:if>
		                    </c:if>
		                </c:if>
		                
		                <%-- If updating existing object --%>
		                <c:if test="${pid != null}">
		                    <s:submit name="edit" class="disable-on-error"><fmt:message key="button.update"/></s:submit>
		                    <c:if test="${user.getHighestAccessRight() >= 50}">
		                	    <s:submit name="delete" id="delete"><fmt:message key="button.delete"/></s:submit>
		                	</c:if>
		                    <s:submit name="showVersionHistory"><fmt:message key="button.showVersionHistory"/></s:submit>
		                </c:if>
	                </p>
	            </div>
	            <div class="clearfix"></div>
	        </div>
        </s:form>     
	        
        <div id="browsingDiv" class="popup-window">
            <img src="img/icons/silk/cross.png" id="closeBrowsingDiv" class="pointerCursor" title="<fmt:message key="button.close"/>"/>
            <div id="browsingDivContent">
                <div id="browseHeader">
                    <h1 class="largeheader-nomargin"></h1>
                    <label class="errorlabel displayNone"><fmt:message key="error.relationitself"/></label><br/>
                    <div id="relationTermContainer">
                        <input type="text" id="relationTerm" placeholder="<fmt:message key="message.typetofilter"/>"/>
                        <img src="img/icons/silk/cancel.png" id="clearFilter" class="pointerCursor displayNone" alt="[x]" title="<fmt:message key="button.clear"/>"/>
                        <img src="img/ajax-loader-arrow.gif" id="relationFilterLoader" class="displayNone"/><br/>
                    </div>
                  
                </div>
                
                <div id="filteredRelationContainer">
                    <ul id="filteredRelation"></ul>
                </div>
                <div id="browseTree">
                   <span id="ajaxerror" class="displayNone errorlabel"><img src="img/icons/silk/error.png" class="smallicon"/> <fmt:message key="error.creatingtree"/></span>
                   <img src="img/ajax-loader-arrow.gif" id="ajaxloader" class="displayNone" />
                </div>
                 <div id="filteredFound" class="displayNone" ><fmt:message key="header.numofresults"/> <span></span></div>
                <div id="browseFooter">
                    <button id="selectBrowse"><fmt:message key="button.select"/></button> <button id="createNewPopup"><fmt:message key="button.create"/></button>
                </div>
            </div>
        </div>
        
        <div id="selectType"></div>
          <div id="uploadedFormForPopup" ></div>
      
        <div id="dialog">
            <div class="confirmicon"></div>
            <div class="confirmtext"></div>
        </div>
    </s:layout-component>
</s:layout-render>
