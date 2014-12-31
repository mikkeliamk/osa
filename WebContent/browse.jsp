<%@ include file="/layout/taglibs.jsp" %>
<!-- Check if user logged in -->
<c:if test="${empty user || user.anonymous == true || user.getHighestAccessRight() < 50}"><c:redirect url="search.jsp" /></c:if>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
	</s:layout-component>
	<c:set var="redirect"> <fmt:message key="header.redirect"/> </c:set>
    <s:layout-component name="content">
      	<script src="js/tree_modified.jquery.js"></script>
       	<link rel="stylesheet" href="css/jqtree.css">
        <s:messages/>
        <s:errors globalErrorsOnly="true"/>
        
        <script>
        var selectedID, 
            selectedLabel;
        var openedNodes = {};
        
        $(function() {
        	$("label#breadcrumb").html("<fmt:message key='link.browse'/>");
        	document.title = "<fmt:message key='link.browse'/> | " + document.title;
        	
        	$('#viewAddBtn').qtip({
        		prerender: true,
				content: {
					text: $("#addElementMenu")
				},
				position: {
					my: 'top left', 
					at: 'top right'
				},
				show: 'click',
				hide:{
					event: 'unfocus click',
				},
				style: { 
					tip: false,
					classes: 'qtip-blue qtip-shadow'
				}
			});
        	
        	$(document).on("dblclick",".viewItem", function() {
        		var id = this.id.split("_")[1];
        		location.href="Ingest.action?pid="+id+"&view=";
        	});
        	
        	$("#openElementBtn").click(function(){
        		if(selectedID != null){
        			location.href="Ingest.action?pid="+selectedID+"&view=";	
        		}
        	});
        	
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
            
            $("#moveConfirmDialog").dialog({
                autoOpen: false,
                modal: true,
                draggable: false,
                resizable: false,
                minWidth: 500,
                title: "<fmt:message key='header.confirmationrequired'/>",
                closeText: "<fmt:message key='button.close'/>"
            });
            
            $("#moveAlertDialog").dialog({
                autoOpen: false,
                modal: true,
                draggable: false,
                resizable: false,
                minWidth: 500,
                title: "<fmt:message key='header.error'/>",
                buttons: [{
                    text: "Ok",
                    click: function () {
                        $(this).dialog("close");
                    }
                }],
                closeText: "<fmt:message key='button.close'/>"
            });
            
            $("#deleteElementBtn").click(function(e){
                if(selectedID != null){
                	e.preventDefault();
                    var objectsPid = selectedID;
                    var childrenArray = []; 

                    var deletemsg = "<fmt:message key='message.disposemethod.delete'/>";
                    var disposemsg = "<fmt:message key='message.disposemethod.dispose'/>";
                    var deletedmsg = "<fmt:message key='message.deleted'/>";
                    var disposedmsg = "<fmt:message key='message.disposed'/>";
                    var deniedmsg = "<fmt:message key='message.deletedenied'/>";
                    
                    var deleteStart = "<fmt:message key='message.delete.start'/>"; //"The following object ";
                    var noChildren = "<fmt:message key='message.delete.nochildren'/>"; //" which has no children will be deleted";
                    var deleteChildren = "<fmt:message key='message.delete.children'/>"; //" and its child objects will be deleted";                  
                    var deleteParent_notAllowed = "<fmt:message key='message.delete.parentnotallowed'/>"; //" cannot be deleted while it has child objects";
                    var deleteParent_leaveOrphanes = "<fmt:message key='message.delete.leaveorphanes'/>"; //" will be deleted and its child objects will become orphaned";
                    
                    // Change button text to inform user that something is happening
                    $(this).html("<fmt:message key='message.working'/>");
                    
                    $.ajax({
                        url: "Disposal.action?ajaxGetItems=",
                        data: {pid: objectsPid},
                        dataType: "json",
                        global: false,
                        success: function(data){
                            $("#deleteElementBtn").html("<fmt:message key='button.deleteObject'/>");
                            $(".confirmtext").empty();
                            if(data[0].method == "dispose") {
                            	$(".confirmtext").html(disposemsg + ":<br/>");
                            } else {
                            	$(".confirmtext").html(deletemsg + ":<br/>");
                            }
                            
                            if (data[0].deletionMsg == "noChildren"){
                        	    $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + noChildren + "<br/>");
                            } else if(data[0].deletionMsg == "deleteChildren"){
                        	    $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + deleteChildren + ":<br/>");
                            } else if(data[0].deletionMsg == "deleteParent_leaveOrphanes"){
                        	    $(".confirmtext").append(deleteStart + "<i>" + data[0].name + "</i> " + deleteParent_leaveOrphanes + ":<br/>");
                            } else if(data[0].deletionMsg == "deleteParent_notAllowed"){
                        	    $(".confirmtext").append("<i>" + data[0].name + "</i> " + deleteParent_notAllowed + ":<br/>");
                            }                          
                            childrenArray.push(data[0].id);
                            for(var i = 1; i < data.length; i++) {
                                $(".confirmtext").append("<img src='img/icons/silk/"+types[data[i].type]+"' class='smallicon' alt='"+types[data[i].type]+"'/>"+
                                                         " <span class='deletelistitem'>"+data[i].name+"</span><br/>");
                                childrenArray.push(data[i].id);
                            }
                            // alert(childrenArray.toString());
                            if (data[0].deletionMsg != "deleteParent_notAllowed"){

                                $("#dialog").dialog('option', 'buttons', {                            	
                                  "<fmt:message key='button.confirm'/>": function () {
                                   $(".ui-dialog-buttonpane").css({"background": "url(../img/ajax-loader-bar.gif) no-repeat scroll 20px 10px", "padding-left": "50px"});
                                   $(".ui-dialog-buttonpane").html("<fmt:message key='message.working'/>");
                                   $.ajax({
                                     url: "Disposal.action?ajaxDeleteItems=",//"Ingest.action?ajaxDeleteItems=",
                                     data: {childrenPids: childrenArray},
                                     global: false,
                                     success: function(data){
                                           $(".ui-dialog-content").hide();
                                           $(".ui-dialog-buttonpane").css({
   		                                       	"background":     "none", 
   		                                       	"padding-left":   "0.4em", 
   		                                       	"border-width":   "0px"
   	                                       	});
                                           if(data == "deleted") {$(".ui-dialog-buttonpane").html("<fmt:message key='general.objects'/> " + deletedmsg);}
                                           else if(data == "denied") {$(".ui-dialog-buttonpane").html(deniedmsg);}
                                           else {$(".ui-dialog-buttonpane").html("<fmt:message key='general.objects'/> " + disposedmsg);}
                                       },
                                     complete: function () {
                                           setTimeout(function (){ window.location.href = "browse.jsp"; }, 1000);
                                     }
                                   });
                                 },
                                 "<fmt:message key='button.cancel'/>": function () {
                                   $(this).dialog("close"); 
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
                }
            });
			
            function fillChildrenArray(data, types, childrenArray){            
            	childrenArray.push(data[0].id);
            	for(var i = 1; i < data.length; i++) {
            		$(".confirmtext").append("<img src='img/icons/silk/"+types[data[i].type]+"' class='smallicon' alt='"+types[data[i].type]+"'/>"+
                    		                 " <span class='deletelistitem'>"+data[i].name+"</span><br/>");
            		childrenArray.push(data[i].id);
                }
            }
        	
        	var $tree = $('#collectionTree');
        	$tree.css("background", "url(img/ajax-loader-bar.gif) no-repeat 130px 250px");
       		$.ajax({
       		    url: "Ingest.action?listCollections=",
       		    global: false,     // this makes sure ajaxStart is not triggered
       		    dataType: 'json',
       		 	data: {rootpid: ":root"},
       		    success: function(j) {
       		    	$tree.css("background","none");
       		    	$tree.tree({
        	        	data: [j],
        	        	dragAndDrop: true,
        	        	autoOpen: 0,
        	        	slide: false,
        	            onCreateLi: function(node, $li) {
        	            	var type = node.type;
        	            	$li.attr("title", node.name);
        	                $li.find('.jqtree-title').before('<span class="icon"><img src="img/icons/silk/'+types[type]+'"/></span>');
        	                if (type == "C" || type == "F" && node.children.length > 0) {$li.find('.jqtree-title').after(" ("+node.children.length+")");}
        	                var div = $('<div id="right_'+node.id.split(":")[1]+'" class="rightclickmenu"/>');
        	                div.append("<a href='Ingest.action?pid="+node.id+"&view='><fmt:message key='button.open'/></a>");
        	                $li.append(div);
        	                
        	            }
        	    	});
       		    	$tree.bind('tree.click',function(event) {
           		    	selectedID = event.node.id;
           		    	selectedLabel = event.node.name;
           		    	
           		    	if (event.node.type == "C") {
           		    		$("#viewAddBtn").prop("disabled", false);
           		    	} else {
           		    		$("#viewAddBtn").prop("disabled", true);
           		    	}
           		    	
           		    	$('#masonryView').empty();
						
       	                $.ajax({
       	                	url: "Ingest.action?getCollectionContent=",
       	                	global: false,
       	                	dataType: "json",
       	                	data: {pid: selectedID},
       	                	success: function(j) {
       	                		for(var i = 0;i<j.length;i++){
   									var type = j[i].type;
   									$('#masonryView').append("<div id='masonry_"+j[i].id+"' class='viewItem' title='"+j[i].name+"'><img src='img/icons/silk32/"+types[type]+"' alt='"+type+"' /><br />"+j[i].name+"</div>");
   								}
   								
   			                    $("#masonryView").masonry({
   			                        itemSelector: ".viewItem",
   			                        columnWidth: 50
   			                    });
       	                	}
       	                });
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
        		    
       		    	$tree.bind(
        		    	    'tree.move',
        		    	    function(event) {
        		    	    	 event.preventDefault();

        		    	    	var targetnodetype = event.move_info.target_node.id.split("-")[0].split(":")[1];
        		    	    	var movednodetype = event.move_info.moved_node.id.split("-")[0].split(":")[1];
    		    	    		var currentpid = [];
    		    	    		var pidobject = {};
    		    	    		pidobject.pid = event.move_info.moved_node.id;
    		    	    		pidobject.oldparent = event.move_info.previous_parent.id;
    		    	    		currentpid.push(pidobject);
    		    	    		currentpid = JSON.stringify(currentpid);
    		    	    		var newparent = event.move_info.target_node.id;
    		    	    		var oldparent = event.move_info.previous_parent.id;
                                
        		    	    	if(targetnodetype != "C"){
        		    	    		$("#moveAlertDialog").html("<img src='img/icons/silk32/cancel.png'/><fmt:message key='error.target'/>");
        		    	    		$("#moveAlertDialog").dialog("open");
        		    	    		
        		    	    	}else if(newparent == oldparent){
        		    	    		$("#moveAlertDialog").html("<img src='img/icons/silk32/cancel.png'/><fmt:message key='error.alreadyexists'/>");
        		    	    		$("#moveAlertDialog").dialog("open");
        		    	    		
        		    	    	}else{
        		    	    		var level = event.move_info.target_node.getLevel();
        		    	        	level--;
        		    	            $.ajax({
        		    	            	url: "Ingest.action?checkIfMoveAllowed=",
        		    	            	data: {nodeLevel: level, newParent: newparent, pid: pidobject.pid},
        		    	            	success: function(success) {
        		    	 	            	if(success == "true"){
                    		    	    		$(".confirmtext").html('<fmt:message key="message.abouttomove"/>: <span class="italic">'+event.move_info.moved_node.name+'</span><br/> <fmt:message key="message.undercollection"/>: <span class="italic">'+event.move_info.target_node.name+'.</span>');
                    		    	    		$("#moveConfirmDialog").dialog('option', 'buttons', {
                    	   		    			      "<fmt:message key='button.confirm'/>": function () {
                    	   		    			    	event.move_info.do_move();
                            		    	    		$.ajax({
                            		    	    			type: "POST",
                            		    	    			url: "Ingest.action?moveObject=",
                            		    	    			global: false,		
                            		    	    			data: { movedpids: currentpid, newParent: newparent, oldParent: oldparent}
                            		    	    		});
                            		    	    		$(this).dialog("close"); 
                    	   		    			      },
                    	   		    			      "<fmt:message key='button.cancel'/>": function () {
                    	   		    			    	$(this).dialog("close"); 
                    	   		    			      }
                    		    	    		});
                    		    	    		$("#moveConfirmDialog").dialog("open");
        		    		            	} else {
                    		    	    		$("#moveAlertDialog").html("<img src='img/icons/silk32/cancel.png'/><fmt:message key='error.preservation'/>");
                    		    	    		$("#moveAlertDialog").dialog("open");
        		    		            	}
        		    	            	}	    		
        		    	            });		
        		    	    	}
        		    	    }
        		    	);
       		    	$tree.bind(
	   		    	    'tree.contextmenu',
	   		    	    function(event) {
	   		    	        var node = event.node;
	   		    	        var e = event.click_event;
	   		    	        var nodeid = "#right_"+node.id.split(":")[1];
	   		    	        $(".rightclickmenu").not(nodeid).hide();
	   		    	        $(nodeid).toggle();
	   		    	    }
	   		    	); 
       		    }
       		}); // ajax            

			$(".addElementlink").on('click', function(e) {
				e.preventDefault();
				 if(selectedID != null){
					var doctype = selectedID.split(":")[1];
					doctype = doctype.split("-")[0];
					
					if(doctype != "C"){
	    	    		$("#moveAlertDialog").html("<img src='img/icons/silk32/cancel.png'/><fmt:message key='error.addintodocument'/>");
	    	    		$("#moveAlertDialog").dialog("open");
					}else{
						var loc = $(this).attr("href");
						loc += "&isPartOf="+selectedID+"&isPartOfLabel="+encodeURIComponent(selectedLabel);
						location.href=loc;
					} 
				 }else{
   	    			$("#moveAlertDialog").html("<img src='img/icons/silk32/cancel.png'/><fmt:message key='error.noselectedobject'/>");
    	    		$("#moveAlertDialog").dialog("open");
				 }
			});	
       					
			$("#collectionTreeResizable").resizable({
				maxWidth: parseInt($("#collectionTreeResizable").css("width"), 10),
				minWidth: parseInt($("#collectionTreeResizable").css("width"), 10), 
				minHeight: parseInt($("#collectionTreeResizable").css("height"), 10),
				handles: "se"
			});
			
       		$(".openHelp").click(function(){
       			$("#helpform").toggle("fast");
       		});
        });
        </script>
        
        <div id="moveConfirmDialog">
        	<div class="confirmicon"></div>
        	<div class="confirmtext"></div>
        </div>
        <div id="moveAlertDialog"></div>
        <div id="rightclickmenu"></div>

        <div id="dialog">
            <div class="confirmicon"></div>
            <div class="confirmtext"></div>
        </div>
        <h1 class="largeheader-nomargin"><fmt:message key="link.browse"/></h1><div class="header-hr"></div>
        <s:form action="/Ingest.action">
	        <p>
		        <s:label for="pid" class="labelsearch"><fmt:message key="capture.pid"/></s:label>
		        <s:text name="pid"/>
	        </p>
	        
	        <p>
		        <s:submit name="view" value="view"><fmt:message key="button.search"/></s:submit>
		        <s:reset name="clear" value="clear" onclick="clear();"><fmt:message key="button.clear"/></s:reset>
	        </p>
        </s:form>
        <form action="advancedbrowse.jsp">
            <input type="submit" value="${redirect}">
           </form>
        <label class="openHelp pointerCursor"><fmt:message key="header.help"/></label>
        <div id="helpform">
        	<p class="boldAndItalic"><fmt:message key="header.features.treeview"/></p>
        	<p><fmt:message key="help.treeview.start"/></p>
        	<p><fmt:message key="help.treeview.open"/></p>
        	<p><fmt:message key="help.treeview.add"/></p> 
        	<p><fmt:message key="help.treeview.move"/></p>
        </div>
			
		<div id="addElementMenu">
			<s:useActionBean var="loginaction" beanclass="fi.mamk.osa.stripes.LoginAction"/>
			<c:forEach var="menuitem" items="${loginaction.listContentModels()}">
                <p><a href="Ingest.action?index=${menuitem}&create=" id="${menuitem}" class="addElementlink"><fmt:message key="link.add.${menuitem}"/></a></p>
			</c:forEach>
		</div>

		<div id="treeviewContainer">
		 		<div id="collectionTreeResizable">
		 			<div id="collectionTree"></div>
		 		</div>
		 		<div id="masonryContainer">
			 		<div id="masonryMenubar">
			 			<button id="openElementBtn" class="sub-form-button"><fmt:message key="button.openObject"/></button>
			 			<c:if test="${user.getHighestAccessRight() >= 50}">
    			 			<button id="viewAddBtn" class="sub-form-button" disabled="disabled"><fmt:message key="button.addToObject"/></button>
                            <button id="deleteElementBtn" class="sub-form-button"><fmt:message key="button.deleteObject"/></button>
			 			</c:if>
			 		</div>
		        	<div id="masonryView"></div>
				</div>
		 	<div class="clearfix"></div>  
        </div>
    </s:layout-component>
</s:layout-render>