<%@ include file="/layout/taglibs.jsp" %>

<c:if test="${empty user || user.getHighestAccessRight() < 100}"><c:redirect url="/" /></c:if>
<s:useActionBean var="useraction" beanclass="fi.mamk.osa.stripes.UserAction"/>
<s:useActionBean var="froupaction" beanclass="fi.mamk.osa.stripes.GroupAction"/>
<c:set var="adduser"> <fmt:message key="admin.adduser"/> </c:set> 
<c:set var="addgroup"> <fmt:message key="admin.addgroup"/> </c:set> 
<c:set var="addrole"> <fmt:message key="admin.addorganizationrole"/> </c:set> 
<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
	</s:layout-component>
	
    <s:layout-component name="html_head">
        <link rel="stylesheet" href="css/jqtree.css">
        <link rel="stylesheet" href="css/core.css">
        <script src="js/jsp-js-functions/js-tab-functionality.js"></script>
        <script src="js/tree_modified.jquery.js"></script>
        <script src="js/jsp-js-functions/admin.js"></script>
        <script src="js/jsp-js-functions/datatables-multiselect.js"></script>
        <script src="js/datatables-plugins/fnReloadAjax.js"></script>
	    <script>
	    var statusquery;
	    var prevData;
	    var oUserTable = "";
	    var oRoleTable = "";
	    $(function(){
            createBreadcrumbAndPageTitle("<fmt:message key='link.admin'/>", "<fmt:message key='link.admin'/> | " + document.title);

            setDatatablesDefaults(function(){
                showUserTable();
                showRoleTable();
                showGroupsTable();
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
            
            $(".close-popup").click(function(){
                modalWindow(false);
                $(this).parent().hide();
                disablePopup();
            });
            
            $(".form-section").on("click", "#add-group-btn", function(){
                modalWindow(true);
                $("#add-group-popup").show();
            });
            
            $(".popup-update-btn").click(function(){
                $(this).after( "<img src='img/ajax-loader-arrow.gif' class='smallicon'>");
            });
            
            $("select#organizations").change(function() {
                if (this.value != "") {
                    if ($("#loadingroles").length == 0) {
                        $(this).after(" <img src='img/ajax-loader-arrow.gif' class='smallicon' id='loadingroles'/>");
                    }
                    getOrgGroupsForSelect(this.value, $("select#groups"));
                    
                    $.getJSON("User.action?findRoles=",{userOrganization: this.value, ajax: 'true'}, function(roles){
                        var options = '';
                        for (var i = 0; i < roles.length; i++) {
                            options += '<option value="'+roles[i]+'">'+roles[i]+'</option>';
                        }
                        $("select#fedoraRoles").html(options);
                        $("#loadingroles").remove();
                    });
                }
            });
            
            //Buttons call popup windows
            $("#addOrganizationRoleBtn").click(function() {
                loadPopup("#addOrganizationRole");     
                $('#addRoleForm').reset();
                return false;
            });
            
            $("#addUserBtn").click(function() {
                loadPopup("#addUser");
                $('#addUserForm').reset();
                return false;
            });
           	
            $("#addUserGroupBtn").click(function() {
                loadPopup("#add-group-popup"); 
                $('#add-group-ajax').reset();
                return false;
            });
            
            // Set organization if using instance admin user
            $("#currentOrganization").change(function() {
                $.ajax({
                    url: "User.action?setOrganization=",
                    data: {userOrganization: $(this).val(), ajax: 'true'},
                    dataType: "text",
                    success: function(data){
                        document.location.reload(true);
                    }, 
                });
            });


            //Functions for popup   

            $(this).keyup(function(event) {
                if (event.which == 27) { 
                    disablePopup();  
                }
            });
            
            function closeloading() {
                $("div.loader").fadeOut('normal');
            }
            var popupStatus = 0; 
            function loadPopup(contentId) {
                this.contentId = contentId;
                $("#add-group-popup #loader").show();
                if(popupStatus == 0) { 
                    closeloading(); 
                    $(contentId).fadeIn(0500);             			
                    popupStatus = 1; 
                }
            }
            
            function disablePopup() {
                if(popupStatus == 1) { 
                    $(".updatePopup displayNone popup-window").fadeOut("normal");
                    popupStatus = 0;
                }
            }
            
            $("[name=createUser]").click(function() { 
                $.ajax({
                    type: 'POST',
                    url: "User.action?createUser=",
                    data: $('#addUserForm').serialize(),
                    dataType: "text",
                    success: function(error){
                        if((error) && (error != "")){
                            alert(error);
                        } else {
                            popupStatus = 0;  
                            $('#addUser').fadeOut("normal");
                            showUserTable(true);
                        }
                    }, 
                });
            });
            
            $("[name=addRole]").click(function() { 
                $.ajax({
                    type: 'POST',
                    url: "Role.action?addRole=",
                    data: $('#addRoleForm').serialize(),
                    dataType: "text",
                    success: function(error){
                       if((error) && (error != "")){
                    	   alert(error);
                       } else {
                    	   popupStatus = 0;  
                    	   $('#addOrganizationRole').fadeOut("normal");
                    	   showRoleTable(true);
                       }
                    }, 
                });
             });
            
            $("[name=createGroupAjax]").click(function() { 
                $.ajax({
                    type: 'POST',
                    url: "Group.action?createGroupAjax=",
                    data: $('#add-group-ajax').serialize(),
                    dataType: "text",
                    success: function(error){
                        if((error) && (error != "")){
                           alert(error);
                        } else {
                            popupStatus = 0;  
                            $('#add-group-popup').fadeOut("normal");
                            showGroupsTable(true);
                        }
                    }, 
            	});               
            });
            
            // Shows delete user button on mouse enter and hides it on mouse leave
            $("#usertable").on("mouseenter mouseleave", "tr", function(e){
            	if (e.handleObj.origType == "mouseenter") {
            	    $(this).find(".delete-user").show();
            	} else {
            	    $(this).find(".delete-user").hide();
            	}
            });
            
            // Marks selected lines with darker background
            $("#usertable, #roletable").on("change", ".select-user, .select-role", function(){
                var $this = $(this);
                if ($this.is(":checked")) {
                    $this.closest("tr").children("td").addClass("selected-row");
                } else {
                    $this.closest("tr").children("td").removeClass("selected-row");
                }
            });
            
            $("#usertable").on("click", ".edit-user", function(e){
                var $edit_button = $(this);
                $(".errortext").remove();
                if ($("#loadinguserdata").length == 0) {
                	$edit_button.after(" <img src='img/ajax-loader-arrow.gif' class='smallicon' id='loadinguserdata'/>");
                }
                // Get user's dn from clicked row's checkbox
                var dn = encodeURI(JSON.stringify($(this.parentNode.parentNode).find(".select-user").data("dn")));
                $.ajax({
                    url: "User.action?getUser=",
                    data: {userDn: dn},
                    dataType: "json",
                    success: function(user){
                    	showUpdateUserWindow(user);
                    	
                    }, // success
                    error: function(error) {
                    	$("#loadinguserdata, .errortext").remove();
                    	$edit_button.after(" <span class='errortext'>"+error.status+" - "+error.statusText+"</span>");
                    }
                });
            });
            
            $("#roletable").on("click", ".edit-role", function(e){
                var $edit_button = $(this);
                $(".errortext").remove();
                if ($("#loadingroledata").length == 0) {
                    $edit_button.after(" <img src='img/ajax-loader-arrow.gif' class='smallicon' id='loadingroledata'/>");
                }
                
                var roleDn = encodeURI(JSON.stringify($(this.parentNode).find(".select-role").data("dn")));
                $.ajax({
                    url: "Role.action?getRoleForUpdating=",
                    data: {roleDn: roleDn},
                    dataType: "json",
                    success: function(role){
                    	showUpdateRoleWindow(role);
                    }, // success
                    error: function(error) {
                        $("#loadingroledata, .errortext").remove();
                        $edit_button.after(" <span class='errortext'>"+error.status+" - "+error.statusText+"</span>");
                    }
                });
            });
            
            $("#usertable").on("click", ".delete-user", function(){
                var $this      = $(this);
                var dnArr      = [];
                
                var userInfo = "";
                userInfo = "<span class='deletelistitem displayBlock'>"+getPropertyValueFromDn($this.data("dn"), "cn") + " <span class='italic'>["+$this.data("role")+"]</span></span>";
                dnArr.push(encodeURI(JSON.stringify($this.data("dn"))));
                
                $(".confirmtext").html("<fmt:message key='message.deleteusers'/>: <br/>");
                $(".confirmtext").append(userInfo);
            	
                $("#dialog").dialog('option', 'buttons', {
                    "<fmt:message key='button.confirm'/>": function () {
                        $.ajax({
                            url: "User.action?deleteUser=",
                            data: {entryDns: dnArr},
                            success: function(data) {
                                // Remove rows from table
                				$this.closest("tr").fadeOut(400, function(){
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
            
            $("#delete-users, #delete-roles").click(function(){
                var button     = this;
                var $this      = "";
                var action     = "";
                var message    = "";
                
                var entryInfo  = "";
                var dnArr      = [];
                var rowArr     = [];
                
                // Set correct actionBean & message
                action  = (button.id == "delete-users") ? "User.action?deleteUser=" : "Role.action?deleteRole=";
                message = (button.id == "delete-users") ? "<fmt:message key='message.deleteusers'/>" : "<fmt:message key='message.deleteroles'/>";
				
				$(this).closest(".tab_content").find(".dataTable input:checkbox:checked").map(function(){
					$this = $(this);
					rowArr.push($this.closest("tr"));
					dnArr.push(encodeURI(JSON.stringify($this.data("dn"))));
	                if (button.id == "delete-users") {
                	   entryInfo += "<span class='deletelistitem displayBlock'>"+getPropertyValueFromDn($this.data("dn"), "cn") + " <span class='italic'>["+$this.data("role")+"]</span></span>";
	                }
	                else if (button.id == "delete-roles") {
	                	entryInfo += "<span class='deletelistitem displayBlock'>"+getPropertyValueFromDn($this.data("dn"), "cn") + "</span>";
	                }
                }).get();
                
                $(".confirmtext").html(message+": <br/>");
                $(".confirmtext").append(entryInfo);
                
                $("#dialog").dialog('option', 'buttons', {
                    "<fmt:message key='button.confirm'/>": function () {
                        $.ajax({
                            url: action,
                            data: {entryDns: dnArr},
                            success: function(data) {
                                // Remove rows from table
                                var rowLength = rowArr.length;
                                for (var i = 0; i < rowLength; i++) {
                                	rowArr[i].closest("tr").fadeOut(400, function(){
	                                    $(this).remove();
	                                });
                                }
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

	    }); // DOM ready
	    
        function getOrgGroupsForSelect(org, targetElement){
            $.getJSON("User.action?findGroups=",{userOrganization: org, ajax: 'true'}, function(groups){
            	var groupsLen = groups.length;
                if (groupsLen > 0) {
                    var options = '';
                    for (var i = 0; i < groupsLen; i++) {
                        options += '<option value="'+groups[i]+'">'+groups[i]+'</option>';
                    }
                    targetElement.html(options);
                } else {
                	// Organization does not have any groups yet, create button to open an add form to dynamically add a group.
                    if ($("#add-group-btn").length == 0) {
                    	targetElement.after(" <img src='img/icons/silk/add_blue.png' class='smallicon' alt='[+]'/> <span id='add-group-btn' class='blue info-small pointerCursor'><fmt:message key='admin.addgroup'/></span>");
                    }
                }
            });
        }

	    function setDatatablesDefaults(callback) {
            $.extend(true, $.fn.dataTable.defaults, {
                "sPaginationType":  "full_numbers",
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
        
	    function getPropertyValueFromDn(dn, propertyName) {
	    	var index = 0;
	    	var propertyValue = "";
	    	
	    	switch (propertyName) {
	    	  case "cn":
	    		  index = 0;
	    		  break;
	    	  case "ou":
	    		  index = 1;
	    		  break;
	    	  case "o":
	    		  index = 2;
	    		  break;
    		  default:
    			  index = 0;
    		      break;
	    	}
	    	
	    	propertyValue = dn.split(",")[index].split("=")[1];
	    	return propertyValue;
	    }
	    </script>
    </s:layout-component>
    <s:layout-component name="content">
	    <s:messages/>
        <c:if test="${user.getHighestAccessRight() == 200}">
            <s:label for="currentOrganization"><fmt:message key="user.organization"/></s:label>
            <select id="currentOrganization" name="currentOrganization">
                <option value=""><fmt:message key="general.default"/></option>
                <c:forEach items="${useraction.findOrganizations()}" var="item">
                    <c:if test="${item == user.organization.name}"><option value="${item}" selected="selected">${item}</option></c:if>
                    <c:if test="${item != user.organization.name}"><option value="${item}">${item}</option></c:if>
                </c:forEach>
            </select>
        </c:if>
	    
	    <h1 class="largeheader"><fmt:message key="link.admin" /></h1><div class="header-hr"></div>
	    <div id="tabs_wrapper">
	        <div id="tabs_container">
				<ul id="tabs">
					<li class="tab-item active"><a href="#users"><fmt:message key="admin.users"/></a></li>
					<li class="tab-item"><a href="#organizationroles"><fmt:message key="admin.organizationroles"/></a></li>
					<li class="tab-item"><a href="#organizationgroups"><fmt:message key="admin.organizationgroups"/></a></li>
	                <c:if test="${user.getHighestAccessRight() == 200}">
	                    <li class="tab-item"><a href="#addorganization"><fmt:message key="admin.addorganization"/></a></li>
	                    <li class="tab-item"><a href="#workflow"><fmt:message key="admin.workflow"/></a></li>
	                </c:if>
				</ul>
	        </div>
	        <div id="tabs_content_container" class="admin-tabs_content_container">
	            <div id="users" class="tab_content">
                    <h2 class="largeheader">
                       <c:if test="${user.getHighestAccessRight() != 200}">
                           <fmt:message key="admin.users.long"><fmt:param value="${user.organization.name}"/></fmt:message>
                       </c:if>
                       <c:if test="${user.getHighestAccessRight() == 200}">
                           <fmt:message key="admin.users"/>
                       </c:if>
                    </h2>
                    <div class="datatable-tools users">
                        <input type="checkbox" class="select" title="<fmt:message key="button.select.all"/>">
                        <button id="delete-users" disabled><fmt:message key="button.selected.delete.users"/></button>
                        <button id="addUserBtn"><fmt:message key="admin.adduser"/></button>
                    </div>
                    <img id="usertable-loader" src="img/ajax-loader-blue.gif"/>
                    <table id="usertable" class="admin-datatable">
                       <thead>
                           <tr>
                               <th><fmt:message key="headerkey.user"/></th>
                               <th><fmt:message key="user.group"/></th>
                               <th><fmt:message key="user.fedorarole"/></th>
                           </tr>
                       </thead>
                    </table>
                </div> <%-- Users tab --%>
	            
                <div id="organizationroles" class="tab_content">
                   <h2 class="largeheader">
                       <c:if test="${user.getHighestAccessRight() != 200}">
                           <fmt:message key="admin.organizationroles.long"><fmt:param value="${user.organization.name}"/></fmt:message>
                       </c:if>
                       <c:if test="${user.getHighestAccessRight() == 200}">
                           <fmt:message key="admin.organizationroles"/>
                       </c:if>
                   </h2>
                   <div class="datatable-tools roles">
                        <input type="checkbox" class="select" title="<fmt:message key="button.select.all"/>">
                        <button id="delete-roles" disabled><fmt:message key="button.selected.delete.roles"/></button>
                        <button id="addOrganizationRoleBtn" class="topopup" ><fmt:message key="admin.addorganizationrole"/></button>                       
                   </div>
                   <table id="roletable" class="admin-datatable">
                       <thead>
                           <tr>
                               <th><fmt:message key="role.name"/></th>
                               <th><fmt:message key="role.accessrightlevel"/></th>
                               <th><fmt:message key="role.occupants"/></th>
                           </tr>
                       </thead>
                   </table>
                </div> <%-- Organizationroles tab --%>
	            
                <div id="organizationgroups" class="tab_content">
                    <h2 class="largeheader"><fmt:message key="admin.organizationgroups"/></h2>
                    <div class="datatable-tools users">
                		<button id="addUserGroupBtn"><fmt:message key="admin.addgroup"/></button>
                   </div>
                    <table id="grouptable" class="admin-datatable">
                		<thead>
                		    <tr>
                		        <th><fmt:message key="group.name"/></th>
                		    </tr>
                        </thead>
                    </table>
                </div>
	            
	            <div id="addUser" class="updatePopup displayNone popup-window">
	            	<img src="img/icons/silk/cross.png" class="close-popup pointerCursor" title="<fmt:message key="button.close"/>" alt=[x]/>
					<h2 class="largeheader"><fmt:message key="admin.adduser"/></h2>
                    <form id="addUserForm">
                        <div class="form-section">
	                        <p>
		                        <label for="fname" class="mediumsizelabel"><fmt:message key="user.firstname"/></label>
		                        <input type="text" name="fname" class="emailinput"/>
	                        </p>
	                        
	                        <p>
		                        <label for="lname" class="mediumsizelabel"><fmt:message key="user.lastname"/></label>
		                        <input type="text" name="lname" class="emailinput"/>
	                        </p>
	                        
	                        <p>
		                        <label for="mail" class="mediumsizelabel"><fmt:message key="user.mail"/></label>
		                        <input type="text" name="mail" value="" class="emailinput"/>
	                        </p>  
	                        
	                        <p>
		                        <label for="preferredLanguage" class="mediumsizelabel"><fmt:message key="user.lang"/></label>
								<select name="preferredLanguage">
								    <option value="fi_FI"><fmt:message key="capture.language.fi_FI"/></option>
								    <option value="en_US"><fmt:message key="capture.language.en_US"/></option>
								</select>
							</p>
                        </div>
                        <div class="form-section">
							<p>     
								<label for="userOrganization" class="mediumsizelabel"><fmt:message key="user.organization"/></label>
								<select name="userOrganization" id="organizations">
							        <option value=""><fmt:message key="general.default"/></option>
									<c:choose>
										<c:when test="${user.getHighestAccessRight() == 200}">
											<c:forEach items="${useraction.findOrganizations()}" var="item">
									            <option value="${item}">${item}</option>
											</c:forEach>
										</c:when>
										<c:otherwise>
									        <option value="${user.organization.name}">${user.organization.name}</option>
										</c:otherwise>
									</c:choose>
								</select>
							</p>
							
							<p>
								<label for="usrFedoraRole" id="usrFedoraRoleLabel" class="mediumsizelabel"><fmt:message key="user.fedorarole"/></label>
								<select name="usrFedoraRoles" id="fedoraRoles" multiple="multiple">
	                                <option value=""><fmt:message key="general.default.organization"/></option>
								</select>
							</p>
							
							<p> 
								<label for="userGroup" class="mediumsizelabel"><fmt:message key="user.group"/></label>
								<select name="userGroup" id="groups">
	                                <option value=""><fmt:message key="general.default.organization"/></option>
								</select>
							</p>
                        </div>
                        <div class="form-section">
							<p>      
								<label for="password" class="mediumsizelabel" ><fmt:message key="user.password"/></label>
								<input type="password"  name="password" value=""/>
							</p>
							
							<p>     
								<label for="repeatPassword" class="mediumsizelabel"><fmt:message key="user.password.repeat"/></label>
                                <input type="password" name="repeatPassword"/>
							</p>
                        </div>
						<input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.user.addsuccess"/>" />
						<input type="hidden" name="messageAddFail" value="<fmt:message key="message.user.addfail"/>" />
						<input type="button" name="createUser" value="${adduser}"/>
						<img src="img/ajax-loader-arrow.gif" id="loader" class="displayNone"/>
                    </form>
                </div>
                    <!-- new user end -->
                    
                    
                 <div id="add-group-popup" class="popup-window">
                        <img src="img/icons/silk/cross.png" class="close-popup pointerCursor" title="<fmt:message key="button.close"/>" alt=[x]/>
                        <form  id="add-group-ajax">
	                        <div class="form-section">
	                            <p>
	                                <label for="groupName" class="mediumsizelabel"><fmt:message key="group.name"/></label>
	                                <input type="text" name="groupName">
	                            </p>
	                            
	                            <p>
	                            <label for="groupOrganization" class="mediumsizelabel"><fmt:message key="group.organization"/></label>
	                            <select name="groupOrganization">
	                                <c:choose>
	                                    <c:when test="${user.getHighestAccessRight() == 200}">
	                                        <c:forEach items="${useraction.findOrganizations()}" var="item">
	                                            <option value="${item}">${item}</option>
	                                        </c:forEach>
	                                    </c:when>
	                                    <c:otherwise>
	                                        <option value="${user.organization.name}">${user.organization.name}</option>
	                                    </c:otherwise>
	                                </c:choose>
	                            </select>
	                            </p>
	                        </div>
	                        <input type="button" name="createGroupAjax" id="createGroupAjax"  value="${addgroup}">
	                     <!--   <img src="img/ajax-loader-arrow.gif" id="loader" class="displayNone"/> --> 
                        </form>
                    </div> 
	             <%-- New user tab --%>
	            
	            <c:if test="${user.getHighestAccessRight() == 200}">
	                <div id="addorganization" class="tab_content">
	                   <h2 class="largeheader"><fmt:message key="admin.addorganization"/></h2>
                        <s:form action="/Organization.action">
                            <div class="form-section">
	                            <p>
									<s:label for="organizationName" class="mediumsizelabel"><fmt:message key="organization.name"/></s:label>
									<s:text name="organizationName"/>
	                            </p>
								<p>
								    <s:label for="displayName" class="mediumsizelabel"><fmt:message key="organization.displayname"/></s:label>
								    <s:text name="displayName"/>
								</p>
								<p>
									<s:label for="organizationDescription" class="mediumsizelabel"><fmt:message key="organization.desc"/></s:label>
									<s:textarea rows="7" cols="40" name="organizationDescription"/>
								</p>
								<p>
									<s:label for="confFile" class="mediumsizelabel"><fmt:message key="organization.conffile"/></s:label>
									<s:text name="confFile"/>
								</p>
							</div>
							
							<h3 class="largeheader"><fmt:message key="organization.admin"/></h3>
							<div class="form-section">
								<p>
									<s:label for="adminFirstname" class="mediumsizelabel"><fmt:message key="user.firstname"/></s:label>
									<s:text name="adminFirstname"></s:text>
								</p>
								<p>	
									<s:label for="adminLastName" class="mediumsizelabel"><fmt:message key="user.lastname"/></s:label>
									<s:text name="adminLastName"></s:text>
								</p>
								<p>	
									<s:label for="adminMail" class="mediumsizelabel"><fmt:message key="user.mail"/></s:label>
									<s:text name="adminMail"></s:text>
								</p>
                            </div>
                            
                            <h3 class="largeheader"><fmt:message key="organization.contactheader"/></h3>
                            <div class="form-section">
								<s:label for="contact" class="mediumsizelabel"><fmt:message key="organization.contact"/></s:label>
								<s:textarea rows="7" cols="40" name="contact"/>
                            </div>
                            <s:submit name="addOrganization"><fmt:message key="admin.addorganization"/></s:submit>
                        </s:form>
	                </div> <%-- Add-organization tab --%>
	            </c:if>
	            
	            
	            
	            <!-- Add organization role -->
	             <div class="updatePopup displayNone popup-window" id="addOrganizationRole" >
			        <div class="close"></div>
			       	<!-- <span >Press Esc to close <span class="arrow"></span></span> -->
					<img src="img/icons/silk/cross.png" class="close-popup pointerCursor" title="<fmt:message key="button.close"/>" alt=[x]/>
					
					<c:if test="${user.getHighestAccessRight() >= 100}">
	                <div id="addorganizationrole" >
						<h2 class="largeheader-nomargin"><fmt:message key="admin.addorganizationrole"/></h2>
						<s:useActionBean var="useraction" beanclass="fi.mamk.osa.stripes.UserAction"/>
						<form id="addRoleForm">
                            <div class="form-section">
								<p>
									<label for="roleName" class="mediumsizelabel"><fmt:message key="role.name"/></label>
									<input type="text" name="roleName">
								</p>
								
								<p>
									<label for="roleOrganization" class="mediumsizelabel"><fmt:message key="role.organization"/></label>
									<select name="roleOrganization">
										<c:choose>
											<c:when test="${user.getHighestAccessRight() == 200}">
												<c:forEach items="${useraction.findOrganizations()}" var="item">
												   <option value="${item}">${item}</option>
												</c:forEach>
											</c:when>
											<c:otherwise>
											   <option value="${user.organization.name}">${user.organization.name}</option>
											</c:otherwise>
										</c:choose>
									</select>
								</p>
								<div id="orig-ac" class="form-section nested-form-section">
									<p>
										<label for="rolePublicity" class="mediumsizelabel" id="publicityLabel"><fmt:message key="role.publicity"/></label>
										<select name="roleAccessrights[0].publicityLevel" id="publicitySelect">
											<option value="public"><fmt:message key="role.public"/></option>
											<option value="restricted"><fmt:message key="role.restricted"/></option>
											<option value="confidential"><fmt:message key="role.confidential"/></option>
										</select>
									</p>
									
									<p>
									    <label for="roleAccessright" id="accessrightLabel" class="mediumsizelabel"><fmt:message key="role.accessrightlevel"/></label>
									    <select name="roleAccessrights[0].accessRightLevel" id="accessrightSelect">
									        <option value="-1"><fmt:message key="role.accessrightlevel.-1"/></option>
									        <option value="10"><fmt:message key="role.accessrightlevel.10"/></option>
									        <option value="20"><fmt:message key="role.accessrightlevel.20"/></option>
									        <option value="30"><fmt:message key="role.accessrightlevel.30"/></option>
									        <option value="40"><fmt:message key="role.accessrightlevel.40"/></option>
									        <option value="50"><fmt:message key="role.accessrightlevel.50"/></option>
									        <option value="100"><fmt:message key="role.accessrightlevel.100"/></option>
									    </select>
									</p>
									
									<p>
										<label id="roleDirectoryLabel" class="mediumsizelabel"><fmt:message key="role.directory"/></label>
										<!-- directory id in hidden field -->
										<input type="hidden" id="roleDirectoryID_0" name="roleAccessrights[0].accessRightPath"/>
										<!-- directory name in visible field | browsename-->
										<input type="text" id="roleDirectoryNAME_0" name="roleDirectoryName" class="textrelation"/>
										<button id="browse_0" name="roleDirectoryButton" class="browseBtn sub-form-button">...</button>
									</p>
									<p>
									    <label id="roleRecursiveRuleLabel" class="mediumsizelabel"><fmt:message key="role.recursiveRule"/></label>
									    <select name="roleAccessrights[0].recursiveRule" id="roleRecursiveRule">
									        <option value="true"><fmt:message key="role.recursiveRule.true"/></option>
									        <option value="false"><fmt:message key="role.recursiveRule.false"/></option>
									    </select>
									</p>
								</div>
								<div id="duplicater"></div>
								
								<p><button id="add-row" name="add-row" class="addBtn" title="<fmt:message key="button.addAccess"/>">+</button><p>
								
								<input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.role.addsuccess"/>" />   
								<input type="hidden" name="messageAddFail" value="<fmt:message key="message.addfail"/>" />  
							</div>
							<input type="button" name="addRole" value="${addrole}">
						</form>
	                </div> 
				</c:if>
			
			    </div> 			
	            <!-- Add organization role end -->
                
                <c:if test="${user.getHighestAccessRight() == 200}">
		            <div id="workflow" class="tab_content">
		               <h2 class="largeheader-nomargin"><fmt:message key="admin.feedback"/></h2>
		               <c:if test="${user.getHighestAccessRight() == 200}">
				           <s:form action="/Feedback.action">
				               <s:submit name="listFeedback">List feedback</s:submit>
				           </s:form>
				           <br>
	                   </c:if>
					   
				       <s:form action="/Ingest.action" focus="">
					       <h2 class="largeheader"><fmt:message key="admin.root.create"/></h2>
					       <p>
						       <s:label for="root"><fmt:message key="admin.root.name"/></s:label>
						       <s:text name="root" class="textsearch">${user.organization.name}</s:text>
					       </p>
					       <p>         
						       <c:if test="${user.getHighestAccessRight() == 200}">
								    <s:label for="currentOrganization"><fmt:message key="user.organization"/></s:label>
						        	<s:select name="currentOrganization">
						        		<s:option value=""><fmt:message key="general.default"/></s:option>
						        			<c:forEach items="${useraction.findOrganizations()}" var="item">
												<s:option value="${item}">${item}</s:option>
											</c:forEach>
						        	</s:select>
						    	</c:if>
					       </p>
					       <p>
			         	        <s:submit name="addRoot"><fmt:message key="button.add"/></s:submit>
						        <s:reset name="clear"><fmt:message key="button.clear"/></s:reset>
						        <s:submit name="deleteRoot"><fmt:message key="button.delete"/></s:submit>
						        <s:submit name="delAllFromOrg"><fmt:message key="button.delete.organizationcontext"/></s:submit>
						        <s:submit name="deleteActionObjects"><fmt:message key="button.delete.actionobjects"/></s:submit>
					       </p>
					    </s:form>
	 			    </div> <!-- Workflow tab -->
 			    </c:if>
	    	</div> <%-- #tabs_content_container --%>
	    	<div class="clearfix"></div>
    	</div>
    	
    	<div id="dialog">
            <div class="confirmicon"></div>
            <div class="confirmtext"></div>
    	</div>
    	
    	<div id="browsingDiv" class="popup-window">
		    <img src="img/icons/silk/cross.png" id="closeBrowsingDiv" class="pointerCursor" title="<fmt:message key="button.close"/>"/>
		    <div id="browsingDivContent">
		        <div id="browseHeader">
		            <h1 class="largeheader-nomargin"><fmt:message key="header.browsing"/></h1>
		            <label class="errorlabel displayNone"><fmt:message key="error.relationitself"/></label><br/>
		        </div>
		        <div id="browseTree"></div>
		        <div id="browseFooter"></div>
		        <button id="selectBrowse"><fmt:message key="button.select"/></button>
		    </div>
		</div>
    	
    	<div id="updateUser" class="updatePopup displayNone popup-window">
            <h2 class="largeheader"><fmt:message key="user.editinfo"/></h2>
            <img src="img/icons/silk/cross.png" class="close-popup pointerCursor" title="<fmt:message key="button.close"/>" alt="[x]"/>
            
            <s:form action="/User.action">
	            <div class="form-section">
					<p>
                        <label class="mediumsizelabel"><fmt:message key="user.firstname"/></label>
                        <label id="update-user-firstname"></label>
                        <s:hidden name="userDn" id="update-user-dn"/>
					</p>
					<p>
						<label class="mediumsizelabel"><fmt:message key="user.lastname"/></label>
                        <label id="update-user-lastname"></label>
					</p>
					<p>
						<label class="mediumsizelabel"><fmt:message key="user.mail"/></label>
						<label id="update-user-mail"></label>
					</p>
					<p>
						<label class="mediumsizelabel"><fmt:message key="user.lang"/></label>
						<label id="update-user-language"></label>
					</p>
	            </div>
				<div class="form-section">
		            <p>
                        <label class="mediumsizelabel"><fmt:message key="user.organization"/></label>
                        <s:select name="userOrganization" id="update-user-organization">
							<c:choose>
								<c:when test="${user.getHighestAccessRight() == 200}">
									<s:option value=""><fmt:message key="general.default"/></s:option>
									<c:forEach items="${useraction.findOrganizations()}" var="item">
			                            <s:option value="${item}">${item}</s:option>
									</c:forEach>
								</c:when>
								<c:otherwise>
								   <s:option value="${user.organization.name}">${user.organization.name}</s:option>
								</c:otherwise>
							</c:choose>
                        </s:select>
					</p>
					<p>
					   <label class="mediumsizelabel"><fmt:message key="user.fedorarole"/></label>
					   <s:select name="usrFedoraRoles" id="update-user-fedoraroles" class="multipleSelect" multiple="multiple">
					       <s:option value=""></s:option>
					   </s:select>
					</p>
	                <p>
		                <label class="mediumsizelabel"><fmt:message key="user.group"/></label>
		                <s:select name="userGroup" id="update-user-usergroup"></s:select>
	                </p>
				</div>
				<s:submit name="updateUser" class="popup-update-btn"><fmt:message key="button.update.user"/></s:submit>
			</s:form>
    	</div>
    	
    	<div id="updateRole" class="updatePopup displayNone popup-window">
            <h2 class="largeheader"><fmt:message key="role.editinfo"/></h2>
            <img src="img/icons/silk/cross.png" class="close-popup pointerCursor" title="<fmt:message key="button.close"/>" alt="[x]"/>
            
            <s:form action="/Role.action">
	            <div class="form-section">
	                <p>
	                    <label class="mediumsizelabel"><fmt:message key="role.name"/></label>
	                    <s:text name="roleName" id="update-role-name"/>
	                    <s:hidden name="roleDn" id="update-role-dn"/>
	                </p>
	                <p>
	                   <label class="mediumsizelabel"><fmt:message key="role.organization"/></label>
	                   <label id="update-role-organization"></label>
	                </p>
	                <div id="roleacs"></div>
	                <p><button id="update-add-ac" title="<fmt:message key="button.addAccess"/>">+</button></p>
	            </div>
	            <s:submit name="updateRole" class="popup-update-btn"><fmt:message key="button.update.role"/></s:submit>
            </s:form>
    	</div>
    </s:layout-component>
</s:layout-render>
