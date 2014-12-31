var browseval = "";
var browsename = "";
var i = 0;

$(function(){
	$("#add-row").click(function(e){
    	e.preventDefault();
    	addRow();
    });
	
	$("#closeBrowsingDiv").click(function(){
        $("#browsingDiv").slideToggle('fast');
    });
	
	$(".form-section").on("click", ".deleteAccessRight", function(){
		$(this).parent().remove();
	});
	
	$("form").on("click", ".browseBtn", function(e){
	    e.preventDefault();
		var lineId = $(this).attr("id").split("_")[1];
	    $("#browsingDiv").slideToggle('fast');
	    $('#browseTree').unbind();
	    $('#selectBrowse').unbind();
	
	    $('#selectBrowse').click(function(){
	    	$("#browsingDiv").slideToggle('fast');
	        $("#roleDirectoryID_"+lineId).val(browseval);
	        $("#roleDirectoryID_"+lineId).blur();
	        
	        if (browsename != "") {
	            $("#roleDirectoryNAME_"+lineId).val(browsename);
	            $("#roleDirectoryNAME_"+lineId).blur();
	        }
	    });
	     
//	    if (typeof(Storage)!=="undefined") {
	    if (false) {
	        store = true;   
	        if (sessionStorage.getItem("browsetree")) {
	            $.getJSON("Ingest.action?getContextTime=",{ajax: 'true'}, function(j){
	                var storageTime = sessionStorage.getItem("ctxtime");
	                if (j > storageTime) {
	                    $.getJSON("Ingest.action?listCollections=",{rootpid: ':root'}, function(n){
	                        sessionStorage.setItem(("browsetree"),JSON.stringify(n));
	                        sessionStorage.setItem("ctxtime",new Date().getTime());
	                        initalizeTree(n);   
	                     });
	                } else {
	                    sessionStorage.clear();
	                    if (sessionStorage.getItem("browsetree") != null) {
	                        initalizeTree(JSON.parse(sessionStorage.getItem("browsetree")));
	                    } else {
	                        $.getJSON("Ingest.action?listCollections=",{rootpid: ':root'}, function(n){
	                            sessionStorage.setItem(("browsetree"),JSON.stringify(n));
	                            sessionStorage.setItem("ctxtime",new Date().getTime());
	                            initalizeTree(n);   
	                        });
	                    }
	                }
	            });
	        } else {
	            $.getJSON("Ingest.action?listCollections=",{rootpid: ':root'}, function(j){
	                sessionStorage.setItem(("browsetree"),JSON.stringify(j));
	                sessionStorage.setItem("ctxtime",new Date().getTime());
	                initalizeTree(j);   
	             });
	        }
	     }else{
	         $.getJSON("Ingest.action?listCollections=",{rootpid: ':root'}, function(j){
	             initalizeTree(j);
	         });
	     }
	});
});

function initalizeTree(data){
    $('#browseTree').tree({
        data: [data],
        autoOpen: 0,
        onCreateLi: function(node, $li) {
            var type = node.type;
            $li.find('.jqtree-title').before('<span class="icon"><img src="img/icons/silk/'+types[type]+'"/></span>');
        },
        onCanSelectNode: function(node) {
            return true;
        }
    });
    $('#browseTree').bind(
        'tree.select',
        function(event) {
            browseval = event.node.id;
            browsename = event.node.name;
    });
    $('#browseTree').bind(
        'tree.open',
        function(event) {
            for(var i1 = 0;i1 < event.node.children.length;i1++){
                $('#browseTree').tree('loadDataFromUrl', 'Ingest.action?listCollections=&rootpid='+event.node.children[i1].id, event.node.children[i1]);
            }
        }
     );
};

/** Inserts given node after reference node
 * 
 * @param referenceNode		Node after which new node will be inserted
 * @param newNode			New node to be inserted
 */
function insertAfter(referenceNode, newNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

function addRow() {
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
};