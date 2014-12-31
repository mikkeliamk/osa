<%@ include file="/layout/taglibs.jsp" %>

<%-- Check if user logged in and user has sufficient role--%>
<c:if test="${empty user || user.getHighestAccessRight() < 40}"><c:redirect url="/" /></c:if>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
	</s:layout-component>
    <s:layout-component name="html_head">
    <script src="js/jsp-js-functions/js-tab-functionality.js"></script>
    <script src="js/jsp-js-functions/contentmodels-json.js"></script>
    <script src="js/jsp-js-functions/datatables-multiselect.js"></script>
    <script src="js/datatables-plugins/fnReloadAjax.js"></script>
    <script>
    var uploader = "";
    function showDisposalListDatatables() {
        $('#disposaltable').dataTable({
            "bAutoWidth": false,
            "bJQueryUI": false,
            "sDom": 't',
            "sAjaxSource": "Disposal.action?showDisposalListDatatables=",
            "fnInitComplete": function(oSettings, json) {
                $('.datatables-td-container').each(function(){
                    // Set the div's height to its parent td's height. Workaround for 100% div height
                    $(this).height($(this).closest('td').height());
                });
              },
            "aoColumns": [
                           {"bSortable": false, "sWidth": '10px', "sClass": 'vertical-middle-td'},
                           {"bSortable": false, "sClass": 'shortCol centered-td'},
                           {"bSortable": false, "sWidth": 'auto'}
                         ]
        });
    }
    
    function setDatatablesDefaults(callback) {
    	$.extend(true, $.fn.dataTable.defaults, {
            "sPaginationType":  "full_numbers",
            "iDisplayLength":   ${gui.searchResults.rows},
            "bProcessing":      false,
            "bServerSide":      true,
            "oLanguage": {
             "sSearch":          '<fmt:message key="datatable.filter" />',
             "sProcessing":      '<fmt:message key="datatable.loading" />',
             "sInfo":            '<fmt:message key="datatable.showing" />',
             "sInfoFiltered":    '<fmt:message key="datatable.filtered" />',
             "sZeroRecords":     '<fmt:message key="datatable.empty" />',
             "sInfoEmpty":       '<fmt:message key="datatable.empty" />',
             "sEmptyTable":      '<fmt:message key="datatable.empty" />',
             "oPaginate": {
                 "sNext":        '>',
                 "sPrevious":    '<',
                 "sFirst":       '<<',
                 "sLast":        '>>'
             }
         }
       });
   	   if(callback && typeof(callback) === "function") {
		   callback();
	   }
    }
    
    $(function(){
    	$("label#breadcrumb").html("<fmt:message key='link.workspace'/>");
    	document.title = "<fmt:message key='link.workspace'/> | " + document.title;
    	
    	// Remove file forms from sessionStorage
        if(getURLParameter("rmst") == "true") {
            sessionStorage.removeItem("fileForm");
            sessionStorage.removeItem("attFileForm");
        }
    	
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
    	
        $("#disposaltable").on("click", ".disposalaction", function(){
        	$this = $(this);
        	var action = $this.data("action");
            var pid = this.id.split("|")[0];
            var name = this.id.split("|")[1];
            var type = "img/icons/silk/"+types[pid.split(":")[1].split("-")[0]];
            var msg = (action == "deleteFromFedora") ? "<fmt:message key='message.disposemethod.deletefromfedora'/>" : "<fmt:message key='message.disposemethod.deletefromdisposallist'/>";
            $(".confirmtext").html(msg+": <br/>");
            $(".confirmtext").append("<span class='deletelistitem'><img src='"+type+"' class='smallicon'/> "+ name+"</span>");
            
            $("#dialog").dialog('option', 'buttons', {
                  "<fmt:message key='button.confirm'/>": function () {
                    $.ajax({
                        url: "Disposal.action?"+action+"=",
                        data: {pidArray: [pid]},
                        complete: function(data, statusText) {
                        	$this.closest('tr').css("background","#FF3700");
                            $this.closest('tr').fadeOut(400, function(){
                                $(this).remove();
                            });
                        }
                    });
                    $(this).dialog("close");
                  },
                  "<fmt:message key='button.cancel'/>": function () {
                    $(this).dialog("close"); 
                  }
                });
            $("#dialog").dialog("open");
        });

        $(".datatable-tools button").click(function(){
            var action = (this.id == "delete") ? "deleteFromFedora" : "deleteFromDisposalList";
            var pidArray = [];
            var trArray = [];
            $(this).closest(".tab_content").find(".dataTable input:checkbox:checked").map(function(){
                trArray.push($(this).closest("tr"));
                pidArray.push(this.value);
            }).get();
            
            $.ajax({
                url: "Disposal.action?"+action+"=",
                data: {pidArray: pidArray},
                complete: function(data, statusText) {
                	if (statusText == "true") {
			            var aLength = trArray.length;
	                    for (var i = 0; i < aLength; i++) {
	                        trArray[i].fadeOut(400, function(){
	                            $(this).remove();
	                        });
	                    }
                	}
                }
            });
        });
        
		uploader = new qq.FileUploader({
		    element: document.getElementById('xml-uploader'),
		    action: "Ingest.action?watchUpload=",
		    params: {watchedFile : "ROOT"},
		    inputName: "fileToUpload",
		    multiple: true,
		    maxConnections: ${gui.queueSize},
		    onSubmit: function(id, fileName){
		    	uploadqueue++;
		    },
		    onComplete: function(id, fileName, responseJSON){
		    	uploadqueue--;
		    	
		    	if(responseJSON["success"] != false) {
		    		var escaped = fileName.split(".").join("\\.").split("(").join("\\(").split(")").join("\\)");
			    	$(".qq-upload-list").find(".file-"+escaped).delay(2000).hide("fade", function() {
			    		$(this).remove();
			    	});
		    	}
		    	if(uploadqueue == 0){
		    		showWatchTable(activeTable);
		    	}
		    },
	        dragText: "<fmt:message key='upload.drag'/>",
	        uploadButtonText: "<fmt:message key='upload.filebutton'/>",
	        cancelButtonText: "<fmt:message key='upload.cancel'/>",
	        failUploadText: "<fmt:message key='upload.failed'/>",
		}); 
	}); // DOM ready

	</script>
	<%-- Must be run after the DOM ready so file uploader component is initialized --%>
    <script src="js/jsp-js-functions/workspace.js"></script>
	</s:layout-component>
	
    <s:layout-component name="content">
		<div id="dialog">
			<div class="confirmicon"></div>
			<div class="confirmtext"></div>
		</div>
		<s:messages/>
				
	    <h1 class="largeheader-nomargin"><fmt:message key="link.workspace" /></h1><div class="header-hr"></div>
	    <div id="tabs_wrapper">
		    <div id="tabs_container">
		        <ul id="tabs">
		            <li class="tab-item active" title="<fmt:message key="link.addobject"/>"><a href="#ingest-manual"><fmt:message key="link.add.object"/></a></li>
		            <li class="tab-item" title="<fmt:message key="link.addobjectauto"/>"><a href="#ingest-auto"><fmt:message key="link.add.batch"/></a></li>
		            <li class="tab-item" title="<fmt:message key="link.enquiry"/>"><a href="#enquiry"><fmt:message key="link.enquiry"/> (0)</a></li>
		            <s:useActionBean var="disposalAction" beanclass="fi.mamk.osa.stripes.DisposalAction"/>
		            <li class="tab-item" title="<fmt:message key="link.disposal"/>"><a href="#disposal"><fmt:message key="link.disposal"/> 
	                    <c:set var="disposalListSize" value="${disposalAction.getDisposalListSize()}"/>
			            <c:if test="${disposalListSize != 0}">
				            <span class="red">(${disposalListSize})</span>
			            </c:if>
			            <c:if test="${disposalListSize == 0}">
		                    (${disposalListSize})
		                </c:if>
	                </a></li>
		        </ul>
		    </div>
		    
		    <div id="tabs_content_container" class="workspace-tabs_content_container">
	            <div id="ingest-manual" class="tab_content">
	                <h2 class="largeheader"><fmt:message key="link.add.object"/></h2>
	            </div> <%-- Ingest tab --%>
	            
	            <div id="ingest-auto" class="tab_content">
	                <h2 class="largeheader"><fmt:message key="link.add.batch"/></h2>
	                <div id="watchtabcont">
		               <span data-table="import" data-tabid="importtab" class="tab-item watchtabctrl activetab"><fmt:message key="general.import"/></span>
		               <span data-table="ingest" data-tabid="ingesttab" class="tab-item watchtabctrl"><fmt:message key="general.ingest"/></span>
		               <span data-table="queue" data-tabid="queuetab" class="tab-item watchtabctrl"><fmt:message key="general.queue"/></span>
		               <span data-table="log" data-tabid="logtab" class="tab-item watchtabctrl"><fmt:message key="general.log"/></span>
	                </div>
					<span id="controls">
					    <label class="smallsizelabel"><fmt:message key="button.search"/></label>
					    <span data-tooltip="<fmt:message key="tooltip.workspace.filter"/>"><img src="img/icons/silk/help.png" class="smallicon helpicon"/></span> 
	                    <input type="text" id="filterbox" size="30"/>
					</span>

                    <%-- NOTICE:
						If you make changes to tables, make sure you also update tableSettings in workspace.js to match the columns of the tables.
						Also the table ID must match the tableSettings individualizing property
                   	--%>   
	                <div class="workspacetab" id="importtab">
	                	<div id="workflowtable-loader-import" class="workflowtable-loader">
	                		<img src="img/ajax-loader-blue.gif"/>
	                		<span class="worflowtable-loader-text"><fmt:message key="datatable.loading"/></span>
	                    </div>
	                    <div id="xml-uploader"></div>
			            <table id="import" class="watchtable">
			                <thead>
			                    <tr>
			                        <th><input class="headerBox" type="checkbox" title="<fmt:message key="button.select.all"/>" /></th>
			                        <th><fmt:message key="capture.fileName"/></th>
			                        <th><fmt:message key="capture.type"/></th>
			                        <th><fmt:message key="capture.size"/></th>
			                        <th><fmt:message key="capture.metadata"/></th>
			                        <th><fmt:message key="capture.filepath"/></th>
			                    </tr>
			                </thead>
			            </table>
	                </div> <%-- Import table --%>
	                
	                <div class="workspacetab" id="ingesttab">
	                	<div id="workflowtable-loader-ingest" class="workflowtable-loader">
	                		<img src="img/ajax-loader-blue.gif"/>
	                		<span class="worflowtable-loader-text"><fmt:message key="datatable.loading"/></span>
                        </div>
                        <p id="foldercontrols">
                            <label class="smallsizelabel"><fmt:message key="general.folder"/></label>
                            <span data-tooltip="<fmt:message key="tooltip.workspace.watched.folderlist"/>"><img src="img/icons/silk/help.png" class="smallicon helpicon" alt="[?]"/></span>
                            <select id="selectedFolder" class="selectElement">
                                <option value="ROOT">${user.cn}</option>
                                <option value="addnew"><fmt:message key="general.action.addnew"/></option>
                            </select>
                        </p>
                        <p id="multiactionCtrl">
                             <label class="smallsizelabel"><fmt:message key="general.action"/></label>
                             <span data-tooltip="<fmt:message key="tooltip.workspace.watched.fileactions"/>"><img src="img/icons/silk/help.png" class="smallicon helpicon" alt="[?]"/></span>
                             <select id="actionToPerform" class="selectElement">
                                <option value=""><fmt:message key="general.default"/></option>
                                <option value="add"><fmt:message key="general.action.ingest"/></option>
                                <option value="edit"><fmt:message key="general.action.editmetadata"/>...</option>
                                <option value="move"><fmt:message key="general.action.move"/></option>
                                <option value="remove"><fmt:message key="button.remove"/></option>
                            </select>
                        </p>	      
                        
                        <table id="ingest" class="watchtable">
                            <thead>
                                <tr>
                                    <th><input class="headerBox" type="checkbox" title="<fmt:message key="button.select.all"/>" /></th>
                                    <th><fmt:message key="capture.fileName"/></th>
                                    <th><fmt:message key="capture.type"/></th>
                                    <th><fmt:message key="capture.size"/></th>
                                    <th><fmt:message key="capture.metadata"/></th>
                                    <th><fmt:message key="capture.isPartOf"/></th>
                                    <th><fmt:message key="capture.filepath"/></th>
                                </tr>
                            </thead>
	                    </table>
	                </div> <%-- Ingest table --%>

                    <div class="workspacetab" id="queuetab">
                    	<div id="workflowtable-loader-queue" class="workflowtable-loader">
                    		<img src="img/ajax-loader-blue.gif"/>
                    		<span class="worflowtable-loader-text"><fmt:message key="datatable.loading"/></span>
                        </div>
                        <table id="queue" class="watchtable">
                            <thead>
                                <tr>
                                    <th><fmt:message key="capture.fileName"/></th>
                                    <th><fmt:message key="capture.type"/></th>
                                    <th><fmt:message key="capture.size"/></th>
                                    <th><fmt:message key="capture.metadata"/></th>
                                    <th><fmt:message key="capture.status"/></th>
                                </tr>
                            </thead>
                        </table>
                    </div> <%-- Queue table --%>
	                
	                <div class="workspacetab" id="logtab">
	                	<div id="workflowtable-loader-log" class="workflowtable-loader">
	                		<img src="img/ajax-loader-blue.gif"/>
	                		<span class="worflowtable-loader-text"><fmt:message key="datatable.loading"/></span>
	                    </div>
						<p>
							<label class="smallsizelabel"><fmt:message key="general.action"/></label>
							<span data-tooltip="<fmt:message key="tooltip.workspace.watched.fileactions"/>"><img src="img/icons/silk/help.png" class="smallicon helpicon" alt="[?]"/></span>
							<select id="logfileactions" class="selectElement">
								<option value=""><fmt:message key="general.default"/></option>
								<option value="remove"><fmt:message key="button.remove"/></option>
							</select>
						</p>
						
	                    <table id="log" class="watchtable">
	                        <thead>
	                            <tr>
	                            	<th><input class="headerBox" type="checkbox" title="<fmt:message key="button.select.all"/> "/></th>
	                                <th><fmt:message key="capture.fileName"/></th>
	                                <th><fmt:message key="capture.type"/></th>
	                                <th><fmt:message key="capture.size"/></th>
	                                <th><fmt:message key="capture.modified"/></th>
	                            </tr>
	                        </thead>
	                    </table>
	                </div> <%-- Log table --%>
					
	                <div id="metadatadiv">
	                    <div class="floatRight">
		                    <h3 class="largeheader"><fmt:message key="header.editingmetadatafor"/>:</h3>
	                        <div id="filesToEdit"></div>
	                    </div>
	                    <div class="floatLeft">
		                    <s:form id="dataFields" action="/Ingest.action">  
		                        <p>
			                        <label class="smallsizelabel"><fmt:message key="capture.type"/></label>
		                            <span data-tooltip="<fmt:message key="tooltip.workspace.selectType"/> "><img src="img/icons/silk/help.png" alt="[?]" class="smallicon helpicon"/></span>
			                        <select name="getMetaDataFiles" id="type"></select>
			                        <s:hidden name="index" id="indexField"/>
		                        </p>
		                    </s:form>
		                    <label id="redirect-loader" class="displayNone"><fmt:message key="message.redirect.form"/> <img src="img/ajax-loader-arrow.gif"/></label>
	                    </div>
	                <img src="img/icons/silk/cross.png" id="metadataexit" class="pointerCursor" title="<fmt:message key="button.close"/>" />
	                </div>
	            </div> <%-- Auto ingest tab --%>
	            
	            <div id="enquiry" class="tab_content">
	                <h2 class="largeheader"><fmt:message key="link.enquiry"/></h2>
	                Enquiries.
	            </div> <%-- Enquiry tab --%>
	            
	            <div id="disposal" class="tab_content">
	                <h2 class="largeheader"><fmt:message key="link.disposal"/></h2>
	                <div class="datatable-tools">
	                    <input type="checkbox" class="select" title="<fmt:message key="button.select.all"/>">
	                    <button id="delete" disabled><fmt:message key="button.selected.delete"/></button>
	                    <button id="cancel" disabled><fmt:message key="button.selected.delete.fromdisposal"/></button>
	                </div>
	                <table id="disposaltable">
			            <thead>
			                <tr>
			                    <%-- Placeholders, these are not visible --%>
	                            <th>1</th><th>2</th><th>3</th>
			                </tr>
			            </thead>     
	                </table>
	            </div> <%-- Disposal tab --%>
	            
	        </div>
			<div class="clearfix"></div>
		</div>
	</s:layout-component>
</s:layout-render>
	