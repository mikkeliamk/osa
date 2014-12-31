var browseval 	= "";
var browsename 	= "";
var i 			= 0;
var index 		= 0;
var acAddIndex 	= 0;
var openedNodes	= {};

$(function(){
	$("#add-row").click(function(e){
    	e.preventDefault();
    	addAccessrightEntry();
    });
	
	$("#closeBrowsingDiv").click(function(){
        $("#browsingDiv").hide();
        modalWindow(false);
    });
	
	$(".updatePopup form").on("blur", "input, select", function(){
        if (this.value == null) {
        	$(".popup-update-btn").prop("disabled", true);
        }
    });
	
	$(".form-section").on("click", ".deleteAccessRight", function(){
		$delBtn = $(this);
		// If clicked button is in update form
		if ($delBtn.hasClass("acUpdate")) {
			var $acUpdateDelBtn = $(".deleteAccessRight.acUpdate");
			$delBtn.parent().remove(); // Remove accessright entry
			// Check if only one accessright entry remains after deletion, prevent user from deleting the last one by removing the delete button
			if ($(".accessrightEntry").length == 1) {
				$acUpdateDelBtn.remove();
			}
		} else {
			$delBtn.parent().remove();
		}
	});
	
	$("#update-add-ac").click(function(e){
		e.preventDefault();
		addAccessrightToRoleUpdate();
	});
	
	$("form").on("click", ".browseBtn", function(e){
	    e.preventDefault();
		var lineId = this.id.split("_")[1];
		var $parent = $(this.parentNode);
		
	    $('#browseTree').unbind();
	    $('#selectBrowse').unbind();
	
	    $('#selectBrowse').click(function(){
	    	$("#browsingDiv").hide();
	    	modalWindow(false);
	    	$parent.find("#roleDirectoryID_"+lineId).val(browseval);
	    	$parent.find("#roleDirectoryID_"+lineId).blur();
	        
	        if (browsename != "") {
	        	$parent.find("#roleDirectoryNAME_"+lineId).val(browsename);
	        	$parent.find("#roleDirectoryNAME_"+lineId).blur();
	        }
	    });
	     
         $.getJSON("Ingest.action?listCollections=",{rootpid: ':root'}, function(j){
             initalizeTree(j);
             modalWindow(true);
             $("#browsingDiv").show();
         });

	});
});

/** Creates a table of users and their infos using DataTables plugin
 * 
 * @param reDraw	Boolean flag to indicate whether the table should be redrawn (refreshed). <b>Leave empty or false on initial load.</b>
 */
function showUserTable(reDraw) {
	if (reDraw && reDraw != "undefined") {
	    $('#usertable-loader').show();
	    oUserTable.fnReloadAjax("User.action?createUserTable=");
	    $('#usertable-loader').fadeOut();
	}
	else {
        oUserTable = $('#usertable').dataTable({
            "bAutoWidth": false,
            "bJQueryUI": true,
            "aaSorting":[[0, "desc"]],
            "sDom": 't',
            "sAjaxSource": "User.action?createUserTable=",
            "fnInitComplete": function(oSettings, json) {
                $('#usertable-loader').fadeOut();
            },
            "asStripeClasses": [], // Removes odd/even row styles
            "aoColumns": [
                           {"bSortable": false, "sClass": "vertical-top-td", "sWidth": "auto"},
                           {"bSortable": false, "sClass": "centered-td usergroup", "sWidth": "180px"},
                           {"bSortable": false, "sClass": "centered-td", "sWidth": "140px"}
                         ]
        });
	}
}

/** Creates a table of roles and their infos using DataTables plugin
 * 
 * @param reDraw	Boolean flag to indicate whether the table should be redrawn (refreshed). <b>Leave empty or false on initial load.</b>
 */
function showRoleTable(reDraw) {	
	if (reDraw && reDraw != "undefined") {
		oRoleTable.fnReloadAjax("Role.action?createRoleTable=");	    
	}
	else {
		oRoleTable = $('#roletable').dataTable({
		    "bAutoWidth": false,
		    "bJQueryUI": true,
		    "aaSorting":[[0, "desc"]],
		    "sDom": 't',
		    "sAjaxSource": "Role.action?createRoleTable=",
		    "asStripeClasses": [], // Removes odd/even row styles
		    "aoColumns": [
		                   {"bSortable": false, "sClass": "vertical-top-td centered-td", "sWidth": "130px"},
		                   {"bSortable": false, "sClass": "vertical-top-td"},
		                   {"bSortable": false, "sClass": "vertical-top-td", "sWidth": "200px"}
		                 ]
		});
	}
}
/** Creates a table of groups and their infos using DataTables plugin
 * 
 * @param reDraw	Boolean flag to indicate whether the table should be redrawn (refreshed). <b>Leave empty or false on initial load.</b>
 */
function showGroupsTable(reDraw) {
	if (reDraw && reDraw != "undefined") {
		oGroupTable.fnReloadAjax("Group.action?createGroupTable=");	    
	}
	else{
		oGroupTable = $('#grouptable').dataTable({
		    "bAutoWidth": false,
		    "bJQueryUI": true,
		    "aaSorting":[[0, "desc"]],
		    "sDom": 't',
		    "sAjaxSource": "Group.action?createGroupTable=",
		    "asStripeClasses": [], // Removes odd/even row styles
		    "aoColumns": [
		                   {"bSortable": false, "sWidth": "500px"}
		                 ]
		});
	}
	
}

function initalizeTree(data){
	var $tree = $('#browseTree');
	$tree.tree({
        data: [data],
        autoOpen: 0,
        slide: false,
        onCreateLi: function(node, $li) {
            var type = node.type;
            $li.find('.jqtree-title').before('<span class="icon"><img src="img/icons/silk/'+types[type]+'"/></span>');
            if (type == "C" || type == "F" && node.children.length > 0) {$li.find('.jqtree-title').after(" ("+node.children.length+")");}
        },
        onCanSelectNode: function(node) {
            return true;
        }
    });
	$tree.bind(
        'tree.select',
        function(event) {
            browseval = event.node.id;
            browsename = event.node.name;
    });
	$tree.bind(
        'tree.open',
        function(event) {
        	// Only load from server if opening node that has not been previously opened
	    	if (openedNodes[event.node.id] != true) {
	            for(var i1 = 0;i1 < event.node.children.length;i1++){
	            	var childNode = event.node.children[i1];
					var childType = childNode.type;
				    if (childType == "C" || childType == "F" && childNode.children.length > 0) {
						$tree.tree('loadDataFromUrl', 'Ingest.action?listCollections=&rootpid='+childNode.id, childNode);
					}
	            }
	    	}
	    	// Set node state to opened
            openedNodes[event.node.id] = true;
        }
	);
	
	// Make popup draggable
	$("#browsingDiv").draggable({
		containment: "#wrapper",
		cancel : '#browseTree' // prevent dragging from tree
	});
}

/** Inserts given node after reference node
 * 
 * @param referenceNode		Node after which new node will be inserted
 * @param newNode			New node to be inserted
 */
function insertAfter(referenceNode, newNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

function addAccessrightEntry() {
    i++;
    var publicityRow 		= document.createElement("p");
    var accessrightRow 		= document.createElement("p");
    var directoryRow 		= document.createElement("p");
    var recursiveRuleRow 	= document.createElement("p");
    
    var duplicater 			= document.getElementById("duplicater");
    var div 				= document.createElement("div");
    div.className 			= "form-section nested-form-section";
    var deleteButton		= document.createElement("img");
    deleteButton.src		= "img/icons/silk/cancel.png";
    deleteButton.alt		= "[-]";
    deleteButton.className 	= "deleteAccessRight pointerCursor helpicon";
    
    // PUBLICITY
    var publicityLabel 		= document.getElementById("publicityLabel").cloneNode(true);
    var publicitySelect 	= document.getElementById("publicitySelect").cloneNode(true);
    publicityLabel.removeAttribute("id");
    publicitySelect.removeAttribute("id");
    publicitySelect.name 	= "roleAccessrights["+i+"].publicityLevel";
    publicityRow.appendChild(publicityLabel);
    publicityRow.appendChild(publicitySelect);
    insertAfter(publicityLabel, document.createTextNode(" "));
    
    // ACCESSRIGHT
    var accessrightLabel 	= document.getElementById("accessrightLabel").cloneNode(true);
    var accessrightSelect 	= document.getElementById("accessrightSelect").cloneNode(true);
    accessrightLabel.removeAttribute("id");
    accessrightSelect.removeAttribute("id");
    accessrightSelect.name 	= "roleAccessrights["+i+"].accessRightLevel";
    accessrightRow.appendChild(accessrightLabel);
    accessrightRow.appendChild(accessrightSelect);
    insertAfter(accessrightLabel, document.createTextNode(" "));
    
    // DIRECTORY
    var directoryLabel 	= document.getElementById("roleDirectoryLabel").cloneNode(true);
    var directoryId		= document.getElementById("roleDirectoryID_0").cloneNode(true);
    var directoryName 	= document.getElementById("roleDirectoryNAME_0").cloneNode(true);
    var browseBtn		= document.getElementById("browse_0").cloneNode(true);
    directoryLabel.removeAttribute("id");
    directoryName.removeAttribute("name");
    directoryName.value	= "";
    directoryId.value	= "";
    browseBtn.id 		= "browse_"+i;
    browseBtn.name 		= "roleDirectoryButton"+i;
    directoryName.id	= "roleDirectoryNAME_"+i;
    directoryId.id 		= "roleDirectoryID_"+i;
    directoryId.name 	= "roleAccessrights["+i+"].accessRightPath";
    directoryRow.appendChild(directoryLabel);
    directoryRow.appendChild(directoryId);
    directoryRow.appendChild(directoryName);
    directoryRow.appendChild(browseBtn);
    browseBtn.parentNode.insertBefore(document.createTextNode(" "), browseBtn); // Adds a space before browse button field
    insertAfter(directoryLabel, document.createTextNode(" "));
    
    // Recursive rule
    var recursiveRuleLabel    = document.getElementById("roleRecursiveRuleLabel").cloneNode(true);
    var recursiveRuleSelect   = document.getElementById("roleRecursiveRule").cloneNode(true);
    recursiveRuleLabel.removeAttribute("id");
    recursiveRuleSelect.removeAttribute("id");
    recursiveRuleSelect.name  = "roleAccessrights["+i+"].recursiveRule";
    recursiveRuleRow.appendChild(recursiveRuleLabel);
    recursiveRuleRow.appendChild(recursiveRuleSelect);
    insertAfter(recursiveRuleLabel, document.createTextNode(" "));
    
    // ADDING CREATED ELEMENTS TO DOM
    div.appendChild(deleteButton);
    div.appendChild(publicityRow);
    div.appendChild(accessrightRow);
    div.appendChild(directoryRow);
    div.appendChild(recursiveRuleRow);
    duplicater.appendChild(div);
} //addAccessrightEntry()

/** Shows a popup window where selected user's info will be displayed in.
 * 
 * @param user	User object as a JSON returned by AJAX
 */
function showUpdateUserWindow(user) {
	$("#update-user-firstname").text(user["firstname"]);
	$("#update-user-lastname").text(user["lastname"]);
	$("#update-user-mail").text(user["mail"]);
	$("#update-user-language").text(user["language"]);
	$("#update-user-dn").val(encodeURI(JSON.stringify(user["dn"])));
	
	var rolesLen       = user["orgroles"].length;
	var groupsLen      = user["orggroups"].length;
	var userRolesLen   = user["contentroles"].length;
	
	$("#update-user-fedoraroles").children('option:not(:first)').remove();
	// Create list of roles and set matching elements selected
    for (var roleIndex = 0; roleIndex < rolesLen; roleIndex++) {
        var selected = false;
        for (var userRoleIndex = 0; userRoleIndex < userRolesLen; userRoleIndex++) {
            if (user["orgroles"][roleIndex] == user["contentroles"][userRoleIndex]) {
                selected = true;    // Set role selected in the list if it matches with a role that user currently has
			    }
		    }
		$("<option/>",{
			"selected"  : selected,
			"value"     : user["orgroles"][roleIndex],
			"text"      : user["orgroles"][roleIndex]
		}).appendTo($("#update-user-fedoraroles"));
    }
	
	// Create option list of groups and set matching element selected
	$("#update-user-usergroup").empty();
	for (var groupIndex = 0; groupIndex < groupsLen; groupIndex++) {
        $("<option/>",{
            "selected"  :(user["orggroups"][groupIndex] == user["group"]) ? true : false,
            "value"     : user["orggroups"][groupIndex],
            "text"      : user["orggroups"][groupIndex]
        }).appendTo($("#update-user-usergroup"));
    }
	
	modalWindow(true);
	$("#updateUser").show();           // Show popup after its content has been created
	$("#loadinguserdata").remove();    // Remove loading icon
} //showUpdateUserWindow()

/** Shows a popup window where selected role's info will be displayed in.
 * 
 * @param role	Role object as a JSON returned by AJAX
 */
function showUpdateRoleWindow(role) {
	$("#roleacs").empty();
	
	$("#update-role-name").val(role["name"]);
	$("#update-role-organization").text(role["organization"]);
	$("#update-role-dn").val(encodeURI(JSON.stringify(role["dn"])));
	
	var acsLen               = role["accessrights"].length;
	
	// Gets text from labels in add role-form
	var publicityText        = document.getElementById("publicityLabel").firstChild.data;
	var accessrightText      = document.getElementById("accessrightLabel").firstChild.data;
	var directoryText        = document.getElementById("roleDirectoryLabel").firstChild.data;
	var recursiveText        = document.getElementById("roleRecursiveRuleLabel").firstChild.data;
	
	// Create nested section per accessright
	for (var acsIndex = 0; acsIndex < acsLen; acsIndex++) {
		acAddIndex 	      		  = acsIndex;
		var accessrightEntry      = role["accessrights"][acsIndex];
		var $nestedSection        = $("<div/>",{"class": "form-section nested-form-section accessrightEntry"});
		
		// Clones existing selects from add role-form
		var $publicitySelect      = $(document.getElementById("publicitySelect").cloneNode(true));
		var $accessrightSelect    = $(document.getElementById("accessrightSelect").cloneNode(true));
		var $browseBtn            = $(document.getElementById("browse_0").cloneNode(true));
		var $recursiveSelect      = $(document.getElementById("roleRecursiveRule").cloneNode(true));
		
		// Rows
		var $pRow     = $("<p/>"),
	        $acRow    = $("<p/>"),
	        $dirRow   = $("<p/>"),
	        $recRow   = $("<p/>");
		
		// Labels
		var pubLabel = "<label class='mediumsizelabel'>"+publicityText+"</label> ",
	        acLabel  = "<label class='mediumsizelabel'>"+accessrightText+"</label> ",
		    dirLabel = "<label class='mediumsizelabel'>"+directoryText+"</label> ",
		    recLabel = "<label class='mediumsizelabel'>"+recursiveText+"</label> ";
		
		$pRow.append(pubLabel);
		$acRow.append(acLabel);
		$dirRow.append(dirLabel);
		$recRow.append(recLabel);
    
	    // Fields
	    $publicitySelect.removeAttr("id");
	    $publicitySelect.attr("name", "roleAccessrights["+acsIndex+"].publicityLevel");
	    $publicitySelect.val(accessrightEntry["publicityLevel"]);
	    $publicitySelect.appendTo($pRow);
	    
	    $accessrightSelect.removeAttr("id");
	    $accessrightSelect.attr("name", "roleAccessrights["+acsIndex+"].accessRightLevel");
	    $accessrightSelect.val(accessrightEntry["accessRightLevel"]);
	    $accessrightSelect.appendTo($acRow);
	    
	    var dirName = "<input type='text' id='roleDirectoryNAME_"+acsIndex+"' class='textrelation' value='"+accessrightEntry["accessRightPathLocal"]+"'> ",
	        dirId   = "<input type='hidden' id='roleDirectoryID_"+acsIndex+"' name='roleAccessrights["+acsIndex+"].accessRightPath' value='"+accessrightEntry["accessRightPath"]+"'>";                           
	
	    $browseBtn.attr("id", "browse_"+acsIndex);
	    // Create delete button only if role has more than one accessright entry
	    if (acsLen > 1) {
	        var $delBtn = $(document.createElement("img"));
	        $delBtn.attr({"src": "img/icons/silk/cancel.png", "alt": "[-]", "class": "helpicon pointerCursor deleteAccessRight acUpdate"});
	        $delBtn.appendTo($nestedSection);
	    }
	    
	    $dirRow.append(dirId);
	    $dirRow.append(dirName);
	    $dirRow.append($browseBtn);
	    
	    $recursiveSelect.removeAttr("id");
	    $recursiveSelect.attr("name", "roleAccessrights["+acsIndex+"].recursiveRule");
	    $recursiveSelect.val(accessrightEntry["recursiveRule"]);
	    $recursiveSelect.appendTo($recRow);
	    
	    // Append rows to container
	    $pRow.appendTo($nestedSection);
	    $acRow.appendTo($nestedSection);
	    $dirRow.appendTo($nestedSection);
	    $recRow.appendTo($nestedSection);
			
		$nestedSection.appendTo($("#roleacs"));
		
	} // for
	
	modalWindow(true);
	$("#updateRole").show();
	$("#loadingroledata").remove();
} //showUpdateRoleWindow()

function addAccessrightToRoleUpdate() {
	acAddIndex++;
	var $acDiv = $(document.getElementById("orig-ac").cloneNode(true));
	var delBtn = "<img src='img/icons/silk/cancel.png' class='deleteAccessRight pointerCursor helpicon acUpdate' alt='[-]'/>";
	
	// Check if accessright entry does not have delete button, add one
	$("#roleacs .accessrightEntry").each(function(){
		if ($(this).find(".acUpdate").length == 0) {
			$(this).append(delBtn);
		}
	});
	
	var	$pubSelect 	= $acDiv.find("#publicitySelect"),
		$acSelect 	= $acDiv.find("#accessrightSelect"),
		$recSelect 	= $acDiv.find("#roleRecursiveRule"),
		$dirName	= $acDiv.find("#roleDirectoryNAME_0"),
		$dirId		= $acDiv.find("#roleDirectoryID_0"),
		$browseBtn	= $acDiv.find(".browseBtn");
	
	$pubSelect.attr("name", "roleAccessrights["+acAddIndex+"].publicityLevel");
	$acSelect.attr("name", "roleAccessrights["+acAddIndex+"].accessRightLevel");
	$recSelect.attr("name", "roleAccessrights["+acAddIndex+"].recursiveRule");
	$dirId.attr("name", "roleAccessrights["+acAddIndex+"].accessRightPath");
	$dirId.attr("id", "roleDirectoryID_"+acAddIndex);
	$dirName.attr("id", "roleDirectoryNAME_"+acAddIndex);
	$browseBtn.attr("id", "browse_"+acAddIndex);
	
	var $elements	= $acDiv.find("select, label");
	$elements.removeAttr("id"); // Remove ids from elements to avoid duplicate ids
	$acDiv.removeAttr("id");
	$acDiv.addClass("accessrightEntry");
	
	$acDiv.append(delBtn);
	$acDiv.appendTo($("#roleacs"));
}