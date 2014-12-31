<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:redirect url="/" />

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="/layout/osa_header.jsp"/>
    </s:layout-component>
    
    <s:layout-component name="content">
    
    <script src="js/tree_modified.jquery.js"></script>
    <link rel="stylesheet" href="css/jqtree.css">
    
    <script type="text/javascript">
    
    var browseval = "";
    var browsename = "";
    var i = 0;
    
    var types = {}; //icons for different node types
    types["F"] = "cog_go.png";
    types["W"] = "user_go.png";
    types["S"] = "application.png";
    types["A"] = "music.png";
    types["C"] = "folder.png";
    types["T"] = "page.png";
    types["D"] = "pencil.png";
    types["E"] = "time.png";
    types["P"] = "photo.png";
    types["M"] = "map.png";
    types["V"] = "film.png";
    types["O"] = "box.png";
    types["L"] = "world.png";
    types["X"] = "asterisk_yellow.png";
    types["undefined"] = "bullet_error.png";
    
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
    }
          
    $(function () {
    	initForm = $("form[name=addorganizationrole]").serialize();
    	    	
    	$('form').bind("keypress", function (e) {
            if (e.keyCode == 13) return false;
        });
    	
        $("label#breadcrumb").html("<a href='admin.jsp'><fmt:message key='link.admin'/></a> &raquo; <fmt:message key='link.addrole'/>");
        document.title = "<fmt:message key='link.addrole'/> | " + document.title;      
        
        var added = window.location.search.replace( "?", "" );
        if (added != undefined && added.length > 0) {
            alert("Role added!");
        }
        
        $("#closeBrowsingDiv").click(function(){
            $("#browsingDiv").slideToggle('fast', function() {
                       
            });
        });
        
        $("#add-row").click(function(e){
        	e.preventDefault();
        	addRow();
        });
        
        $("form").on("click", ".browseBtn", function(e){
            e.preventDefault();
        	var lineId = $(this).attr("id").split("_")[1];
            $("#browsingDiv").show('fast');
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
             
            if (typeof(Storage)!=="undefined") {
                store = true;   
                if (sessionStorage.getItem("browsetree")) {
                    $.getJSON("Ingest.action?getContextTime=",{ajax: 'true'}, function(j){
                        var storageTime = sessionStorage.getItem("ctxtime");
                        if (j > storageTime) {
                            sessionStorage.clear();
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
    }); // DOM ready
    
    // add new row for directory selection
    function addRow() {
        i++;
        var duplicater = document.getElementById("duplicaterow");
        var original = duplicater.cloneNode(true);
        
        var originalLabel = document.getElementById("roleDirectoryLabel");
        var cloneLabel = originalLabel.cloneNode(true);
        original.appendChild(cloneLabel);
        
        var originalHidden = document.getElementById("roleDirectoryID_0");
        var cloneHidden = originalHidden.cloneNode(true);
        cloneHidden.id = "roleDirectoryID_"+i;
        cloneHidden.name = "roleDirectories["+i+"]";
        cloneHidden.value = "";
        original.appendChild(cloneHidden);
        
        var originalTextfield = document.getElementById("roleDirectoryNAME_0");
        var cloneTextfield = originalTextfield.cloneNode(true);
        cloneTextfield.id = "roleDirectoryNAME_"+i;
        cloneTextfield.className = "textrelation";
        cloneTextfield.name = "roleDirectoryName["+i+"].name";
        cloneTextfield.value = "";
        original.appendChild(cloneTextfield);
        
        var originalButton = document.getElementById("browse_0");
        var cloneButton = originalButton.cloneNode(true);
        cloneButton.id = "browse_"+i;
        cloneButton.name = "roleDirectoryButton"+i;
        cloneButton.className = "browseBtn";
        original.appendChild(cloneButton);
        
        document.getElementById("duplicater").appendChild(original);
    }
    </script>
    
    <div id="browsingDiv">
    	<div id="browsingDivContent">
	    	<img src="img/icons/silk/cross.png" id="closeBrowsingDiv" title="<fmt:message key="button.close"/>"/>
	        <div id="browseHeader">
	            <h1 class="largeheader"><fmt:message key="header.browsing"/></h1>
	            <label id="errorlabel"><fmt:message key="error.relationitself"/></label><br/>
	        </div>
	        <div id="browseTree"></div><br/>
	        <div id="browseFooter"></div>
        	<button id="selectBrowse"><fmt:message key="button.select"/></button>
        </div>
    </div>
    
    <s:useActionBean var="useraction" beanclass="fi.mamk.osa.stripes.UserAction"/>
    <s:form action="/Role.action">
    
        <h1 class="largeheader"><fmt:message key="header.addrole"/></h1><div class="header-hr"></div>
        
        <p>
        <s:label for="roleName" class="mediumsizelabel"><fmt:message key="role.name"/></s:label>
        <s:text name="roleName"></s:text>
        </p>
        
        <p>
        <s:label for="roleAccessrightlevel" class="mediumsizelabel"><fmt:message key="role.accessrightlevel"/></s:label>
        <s:select name="roleAccessrightlevel">
            <s:option value="public"><fmt:message key="role.public"/></s:option>
            <s:option value="restricted"><fmt:message key="role.restricted"/></s:option>
            <s:option value="confidential"><fmt:message key="role.confidential"/></s:option>
        </s:select>
        </p>
        
        <p>
        <s:label for="roleOrganization" class="mediumsizelabel"><fmt:message key="role.organization"/></s:label>
        <s:select name="roleOrganization">
	        <c:choose>
            <c:when test="${user.isInstanceAdmin() == true}">
	            <c:forEach items="${useraction.findOrganizations()}" var="item">
	                <s:option value="${item}">${item}</s:option>
	            </c:forEach>
	        </c:when>
	        <c:otherwise>
	            <s:option value="${user.getOrganization().getName() }">${user.getOrganization().getName() }</s:option>
	        </c:otherwise>
	        </c:choose>
        </s:select>
        </p>

        <p>
        <s:label for="roleDirectory" id="roleDirectoryLabel" class="mediumsizelabel"><fmt:message key="role.directory"/></s:label>
        <!-- directory id in hidden field -->
        <s:hidden id="roleDirectoryID_0" name="roleDirectories[0]"/>
        <!-- directory name in visible field | browsename-->
        <s:text id="roleDirectoryNAME_0" name="roleDirectoryName" class="textrelation"/>
        <button id="browse_0" name="roleDirectoryButton" class="browseBtn"><fmt:message key="button.browse" /></button>
        
        <div id="duplicater">
            <div id="duplicaterow"></div>
        </div>
        
        <button id="add-row" name="add-row" class="addBtn">+</button>
        </p>
        
        <input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.addrolesuccess"/>" />   
        <input type="hidden" name="messageAddFail" value="<fmt:message key="message.addfail"/>" />  
    
        <s:submit name="createRole"><fmt:message key="button.add"/></s:submit>
    </s:form>
    
    </s:layout-component>
</s:layout-render>