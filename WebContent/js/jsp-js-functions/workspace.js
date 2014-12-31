var $wTable;

var files 			= {};
var fileTypes 		= new Array();
var folders 		= new Array();
var visibleEntries 	= new Array();
var autoRefresh		= ["import", "queue"];
var activeTab 		= "importtab";
var activeTable 	= "import";
var selectedFolder 	= "ROOT";
var sfolder 		= "ROOT";
var sorting 		= "filename";
var direction 		= "asc";
var currentsort 	= "filename_asc";
var currenttable 	= "#watchedFolders";
var uploadqueue 	= 0;
var interval 		= "";
var intervalLength	= 5000; // in ms

//Set the name of the hidden property and the change event for visibility
var hidden, visibilityChange; 
if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support 
  hidden = "hidden";
  visibilityChange = "visibilitychange";
} else if (typeof document.mozHidden !== "undefined") {
  hidden = "mozHidden";
  visibilityChange = "mozvisibilitychange";
} else if (typeof document.msHidden !== "undefined") {
  hidden = "msHidden";
  visibilityChange = "msvisibilitychange";
} else if (typeof document.webkitHidden !== "undefined") {
  hidden = "webkitHidden";
  visibilityChange = "webkitvisibilitychange";
}

/* **************************
	IF YOU MAKE CHANGES TO THE MARKUP DEFINING THE TABLES, MAKE SURE YOU MAKE CORRESPONDING CHANGES TO THESE ASWELL
 	cols-array MUST have the same amount of objects as there are table columns
 **************************/
var tableSettings = {
		"import": {
			"defaultSorting": [1, "asc"],
			"showLoader": true,
			"cols": [
					{"bSortable": false, "sWidth": 	'10px'},
	                {"bSortable": true,  "sWidth": 	'auto'}, 
	                {"bSortable": true, "sWidth": 	'10px'}, 
	                {"bSortable": true,  "sWidth": 	'10px'}, 
	                {"bSortable": false, "sWidth": 	'30px'}, 
	                {"bSortable": true,  "sWidth": 	'auto'}
                ]
		}, 
		"ingest": {
			"defaultSorting": [1, "asc"],
			"showLoader": true,
			"cols": [
					{"bSortable": false, "sWidth": 	'10px'},
		            {"bSortable": true,  "sWidth": 	'auto'}, 
		            {"bSortable": true, "sWidth": 	'10px'}, 
		            {"bSortable": true,  "sWidth": 	'10px'}, 
		            {"bSortable": false, "sWidth": 	'20px'}, 
		            {"bSortable": false, "sWidth": 	'auto'},
		            {"bSortable": true,  "sWidth": 	'auto'}
	            ]
		},
		"queue": {
			"defaultSorting": [0, "asc"],
			"showLoader": true,
			"cols": [
	                    {"bSortable": true,  "sWidth":  'auto'}, 
	                    {"bSortable": true, "sWidth":  '10px'}, 
	                    {"bSortable": true,  "sWidth":  '10px'}, 
	                    {"bSortable": false, "sWidth":  '30px'}, 
	                    {"bSortable": false, "sWidth":  '30px'}
	            ]
		},
        "log": {
            "defaultSorting": [4, "desc"],
            "showLoader": true,
            "cols": [
                 	{"bSortable": false, "sWidth":  '10px'},
                    {"bSortable": true,  "sWidth":  'auto'},
                    {"bSortable": true,  "sWidth":  '200px'}, 
                    {"bSortable": true,  "sWidth":  '30px'}, 
                    {"bSortable": true,  "sWidth":  '100px'},
                ]
        }
			
};

/** Deletes files from watched folder
 * 
 * @param filesToRemove		Array of file paths.
 * @param rows				Array of checkboxes whose parent row will be deleted.
 * @param fromQueue			Optional boolean flag indicating that files will be deleted from queue. 
 * 							Defaults to false if no value is given.
 */
function deleteRow(filesToRemove, rows, queue){
	if (typeof queue === "undefined") {queue = false;}
	var rowLen = rows.length;
	$.ajax({
		url: "Ingest.action?removeFromWatchedFolder=",
		data: { watchedFiles: filesToRemove, fromQueue: queue},
		global: false,
		dataType: "json",
		success: function(success) {
		},
		complete: function() {
			listFolders();
			showWatchTable(activeTable);
		}
	});
}

/** Inserts files to queue
 * 
 * @param filesToQueue	Array of file paths
 * @param row			Array of checkboxes whose parent row will be deleted
 */
function addToQueue(filesToQueue, rows){
	var rowLen = rows.length;
	$.ajax({
		url: "Ingest.action?addToQueue=",
		data: { watchedFiles: filesToQueue},
		global: false,
		success: function(data) {
			if (data == "true") {
				for (var i = 0; i < rowLen; i++ ) {
					rows[i].closest("tr").remove();
				}
			}
		},
		complete: function() {
			listFolders();
		}
	});
}

function startWorkflow(path, count){
	//TODO create workflow;
	$("#watchrow_"+count).remove();
}

function listFolders(){
	$("#selectedFolder").empty();
	folders = new Array();
	$.ajax({
		url: "Ingest.action?listWatchedFolders=",
		global: false,
		dataType: "json",
		success: function(data) {
			for(var i = 0;i<data.length;i++){
				folders.push(data[i]["name"]);
				$("#selectedFolder").append('<option '+(selectedFolder === data[i]["name"] ? 'selected="selected"' : '')+ 'value="'+data[i]["name"]+'">'+data[i]["name"]+'  ('+data[i]["count"]+')</option>');
			}
			$("#selectedFolder").append('<option value="addnew">'+messagebundle.button.addnew+'...</option>');
		}
	});
}

/** Redirect to a given tab after page load
 * 
 * @param tab	Link element's href value without the #-sign. Link elements are in li-elements that create the actual tabs<br/>
 * 					<i><b>Note:</b> this only works with tabs that are created with a parent UL-element with an ID of "tabs".</i>
 */
function redirectToTab(tab, subTab) {
	$("#tabs").find("[href=#"+tab+"]").trigger("click");
}

/** Handles interval setting and clearing based on page visibility. If page is visible (tab is active), application sets interval, otherwise clears it.<br/>
 *  Uses JavaScripts' Page Visibility API. If browser does not support the API, application does not clear interval.
 * 
 */
function handleInterval() {
	interval = window.setInterval(function(){
		refreshTable(activeTable);
	}, intervalLength);
	
	if (typeof hidden !== "undefined") {
		document.addEventListener(visibilityChange, function(){
			if (!document[hidden]) {
				interval = window.setInterval(function(){
					refreshTable(activeTable);
				}, intervalLength);
			} else {
				window.clearInterval(interval);
			}
		}, false);
	}
}

/** Refreshes workflow tables automatically
 * 
 * @param tableId	Table id without #-sign
 */
function refreshTable(tableId) {
	var $table = $("#"+tableId);
	// Check if current table ID is in array that defines tables that should be auto-refreshed
	for (var i = 0, len = autoRefresh.length; i < len; i++) {
		if (tableId == autoRefresh[i]) {
			// If table is initialized as DataTables component, use reload function, otherwise use showWatchTable function
			if ($.fn.DataTable.fnIsDataTable($table[0])) {
				$table.dataTable().fnReloadAjax("Ingest.action?watchedFile="+selectedFolder+"&watchTable="+tableId.toUpperCase()+"&getWatchedFolderContent=");
			} else {
				showWatchTable(tableId);
			}
		}
	}
}

/** Creates Datatables component from given table
 * 
 * @param tableId	Id of the table without #-character
 */
function showWatchTable(tableId) {
    $wTable = $('#'+tableId).bind("processing", function(e, oSettings, bShow) {
    	if (tableSettings[tableId].showLoader) {
    		if (bShow) {$('#workflowtable-loader-'+tableId).show();}
    		else {$('#workflowtable-loader-'+tableId).fadeOut();}
    	}
	    }).dataTable({
	    	"aaSorting":	[tableSettings[tableId].defaultSorting],
	    	"bAutoWidth": 	false,
	        "bJQueryUI": 	true,
	        "bDestroy": 	true,
	        "sDom": 		'<"H"ip>t<"F"ip>',
	        "sAjaxSource": 	"Ingest.action?watchedFile="+selectedFolder+"&watchTable="+tableId.toUpperCase()+"&getWatchedFolderContent=",
	        "aoColumns": 	tableSettings[tableId].cols
	    });
}

/******************
 * DOM READY
 ******************/

$(function(){
	setDatatablesDefaults();
	$("#" + activeTab).show();
	
	getContentModels("all", function(cmodels){
		var html = "";
		for (category in cmodels) {
			html += "<div class='"+category+" add-button-container'>";
			html += "<h3 class='largeheader'>"+messagebundle[category]+"</h3>";
			var items = cmodels[category];
			for (item in items) {
				// Disable action button
				if (items[item] == "action") {
					html += '<div class="add-button-div disabled"><span>'+messagebundle[items[item]]+'</span></div>';
				} else {
					html += '<a href="Ingest.action?index='+items[item]+'&create="><div class="add-button-div"><span>'+messagebundle[items[item]]+'</span></div></a>';
				}
			}
			html += "</div>";
		}
    	$("#ingest-manual").append(html);
	}, $("#ingest-manual"));
	
	// Load content only when entering the tab
	$("#tabs li a, #tabs li").click(function(e){
		var tabname = "";
		if (e.target.localName == "li") {
			tabname = e.target.children[0].attributes[0].nodeValue;
		} else {
			e.stopPropagation();
			tabname = e.target.attributes[0].nodeValue;
		}
		switch (tabname) {
			case "#ingest-auto":
				showWatchTable(activeTable);
				handleInterval();
				listFolders();
				break;
			case "#disposal":
				// Only load if table is not yet initialized as a DataTable
				var $table = $("#disposaltable")[0];
				if (!$.fn.DataTable.fnIsDataTable($table)) {
					showDisposalListDatatables();
				}
				break;
		}
	});
	
	if(getURLParameter("tab") != null ) {
    	redirectToTab(getURLParameter("tab"), getURLParameter("subtab"));
    }
	
	$(".watchtabctrl").click(function(){
		$('.workspacetab').hide();
		$('.watchtabctrl').removeClass('activetab');
		$(this).addClass("activetab");
		var activetab = $(this).data("tabid");
		activeTable = $(this).data("table");
		$("#"+activetab).show();
		
		// Load table if not opening upload tab
		if (activeTable != "upload") {
			showWatchTable(activeTable);
		}
	});


	$(".watchtable").on("click","td:not(:first-child)", function(e) {
		e.stopPropagation();
		var $checkbox = $(this).parent().find(":checkbox");
		$checkbox.prop("checked", !$checkbox.prop("checked"));
	});
	
	
	$(document).on("click",".deletefolderbtn", function(e) {
		var folder = this.value;		
		var r=confirm(messagebundle.header.abouttodelete+":\n\""+folder+"\"\n"+messagebundle.button.confirm);
		if (r==true){
	    	$.ajax({
				url: "Ingest.action?deleteWatchedFolder=",
				data: { watchedFile: folder},
				global: false,
				success: function(data) {
					$("#selectedFolder").val("ROOT");
					selectedFolder = "ROOT";
					//showWatchTable("root");
					listFolders();
					$(".deletefolderbtn").remove();
				}
			});
		}
	});
	
	// Filters table
	$("#filterbox").keyup(function(){
		if (activeTable != "upload") {
			var filterval = this.value;
			delay(function(){
				$wTable.fnFilter(filterval);
			}, 800);
		}
	});
	
	// Toggles all checkboxes on current table page
	$(document).on("change",".headerBox", function(e) {
		if($(this).is(':checked')){
			$(".watchcheck").prop('checked',true);
		}else{
			$(".watchcheck").prop('checked',false);
		}
	});
	
	// Moves given files to selected folder
	$(document).on("change","#actionMove", function(e) {
		var filesToMove = [], rowArr = [];
		if(this.value != ""){
			if(this.value == "action-cancel" || this.value == ""){
				$("#actionToPerform").val("");
				$(this).remove();
			} else {
				var targetfolder = this.value;
				$(".watchcheck:checked").each(function() {
					var $this = $(this);
					var filename = $this.data("filepath");
					rowArr.push($this);
					filesToMove.push(filename);
				});
		    	$.ajax({
					url: "Ingest.action?moveToWatchedFolder=",
					data: { watchedFiles: filesToMove, toFolder: targetfolder},
					global: false,
					success: function(success) {
						if (success == "true") {
							var rowLen = rowArr.length; 
							for (var i = 0; i < rowLen; i++ ) {
								rowArr[i].closest("tr").remove();
							}
							listFolders();
						}
					}
				});
				$(this).remove();
				$("#actionToPerform").val("");
			}
		}
	});
	
	$("#actionToPerform").change(function(){
		var fileArr = [], rowArr = []; // Initialize arrays
		$("#actionMove").remove();
		if(this.value != "" && $(".watchcheck:checked").length > 0){
			var action = this.value;
			
			// Push selected rows to arrays
			$(".watchcheck:checked").each(function() {
				var $this = $(this);
				fileArr.push($this.data("filepath"));
				rowArr.push($this);
			});				
			
			switch(action){
			
				case "add":
					$.ajax({
					    type: "POST",
					    url: "Workflow.action?startIngestWorkflow=",
					    data: { filename: JSON.stringify(fileArr)},
						complete: function() {
							listFolders();
							showWatchTable(activeTable);
						}
					});
					break;
					
				case "remove":
					deleteRow(fileArr, rowArr);
					$(this).val("");
					break;
					
				case "move":
					if ($(".watchcheck:checked").length > 0) {
						var actionmove = "<select id='actionMove' class='selectElement'>";
						actionmove += "<option value=''>"+messagebundle.button.select+"</option>";
						for(var x = 0;x<folders.length;x++){
							if($("#selectedFolder").val() != folders[x]){
								actionmove += "<option value='"+folders[x]+"'>"+folders[x]+"</option>";	
							}
						}
						actionmove += "<option value=''></option>"+
									  "<option value='action-cancel'>"+messagebundle.button.cancel+"</option>";
						actionmove += "</select>";
						$("#multiactionCtrl").append(actionmove);
					}
					break;
					
				case "edit":
					$("#filesToEdit").empty();
					var files = 0;
					var path = "",
						filename = "";
					$(".watchcheck:checked").each(function() {
						files++;
						path = $(this).data("filepath");
						filename = $(this).data("filename");
						$("#filesToEdit").append("<p class='metadataentry' data-filepath='"+path+"' data-filename='"+filename+"' title='"+filename+"'>"+filename+"</p>");
					});	
					if(files > 0){
						modalWindow(true);
						getContentModels("documents", function(cmodels){
							var option = "";
							option += "<option value=''>"+messagebundle.button.select+"</option>";
							for (category in cmodels) {
								var items = cmodels[category];
								for (item in items) {
									option += "<option value='"+items[item]+"'>"+messagebundle[items[item]]+"</option>";
								}
							}
							$("#dataFields #type").html(option);
						});
						
						$("#metadatadiv").show('fast', function(){
							$("#dataFields [name='watchedFiles']").remove();
							var objects = [];
							$(".metadataentry").each(function(){
								var filename = $(this).data("filename");//+".metadata";
								$("#dataFields").append($("<input type='hidden' name='watchedFiles' value='"+filename+"'/>"));
								objects.push(filename);
							});
							$("#dataFields #type").change(function(){
								if (this.value != "") {
									$("#dataFields #indexField").val(this.value);
									$("#redirect-loader").show();
									$("#dataFields").submit();
								}
							}); 
						});
					}
					$(this).val("");
					break;
					
				default:
					alert("INVALID PARAMETER?");
			}
		} else {
			this.value = "";
		}
	});
	
	$("#logfileactions").change(function() {
		var fileArr = [];
		var selectElem = this;
		
		$(this).closest(".workspacetab").find(".watchcheck:checked").each(function() {
			var $this = $(this);
			fileArr.push($this.data("filepath"));
		});
		
		if (selectElem.value != "") {
			switch (selectElem.value) {
				case "remove":
					$.ajax({
						url: "Ingest.action?removeLogFile=",
						data: {watchedFiles: fileArr},
						dataType: "json",
						success: function(success) {
							
						},
						complete: function() {
							showWatchTable(activeTable);
							selectElem.value = "";
						}
					});
			}
		}
	});
	
	$("#selectedFolder").change(function() {
		$(".deletefolderbtn").remove();
		if(this.value === "addnew"){
			var foldername=prompt("Folder name:","new_folder");
			if (foldername!=null){
				$.post("Ingest.action?addWatchedFolder=", { watchedFile: foldername})
				.done(function(success) {
					if (success != "false") {
						listFolders();
					}
				});
			}
		}
		else if(this.value != null){
			selectedFolder = this.value;
			if(this.value != "ROOT"){
				$("#foldercontrols").append("<button class='deletefolderbtn' value='"+this.value+"' >"+messagebundle.button["delete"]+"...</button>");
			}
		    uploader.setParams({
		    	watchedFile: this.value
		     });
		    showWatchTable(activeTable);
		}
	});
	
	$("#queueActions").change(function(){
		var fileArr = [], rowArr = [];
		if ($(".watchcheck:checked").length > 0) {
			$(".watchcheck:checked").each(function() {
				var $this = $(this);
				fileArr.push($this.data("filepath"));
				rowArr.push($this);
			});	
			
			switch (this.value) {
				case "start":
					$.ajax({
					    type: "POST",
					    url: "Workflow.action?startMetadataWorkflow=",
					    data: { filename: JSON.stringify(fileArr)}
					});
					break;
				case "ingest":
					$.ajax({
					    type: "POST",
					    url: "Workflow.action?startIngestWorkflow=",
					    data: { filename: JSON.stringify(fileArr)}
					});
					break;
				case "cancel":
					deleteRow(fileArr, rowArr, true);
					break;
			}
		}
	});
	
	$("#metadataexit").click(function(){
		$("#metadatadiv").hide();
		modalWindow(false);
	});
	
	$("#metadatasave").click(function(){
		var objects = [];
		var datafields = $("#dataFields").serializeArray();	
		$(".metadataentry").each(function(){
			var filename = $(this).data("filename");
			objects.push(filename);
		});
		
		$.ajax({
		    type: "POST",
		    url: "Ingest.action?addMetaDataFiles=",
		    data: { watchedFile: JSON.stringify(objects), watchFields: JSON.stringify(datafields)},
		    success: function(data) {
		    	modalWindow(false);
		    	$("#metadatadiv").hide();
		    }
		});
	});
}); // DOM ready