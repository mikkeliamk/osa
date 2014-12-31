<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:redirect url="/" />

<s:layout-render name="/layout/default.jsp">

	<s:layout-component name="header">
		<jsp:include page="/layout/osa_header.jsp"/>
	</s:layout-component>
	
    <s:layout-component name="content">
    
    <script type="text/javascript">
     	
    $(function() {
    	$("label#breadcrumb").html("<a href='admin.jsp'><fmt:message key='link.admin'/></a> &raquo; <fmt:message key='link.adduser'/>");
    	document.title = "<fmt:message key='link.adduser'/> | " + document.title;
    	
        $("select#organizations").change(function() {
            $.getJSON("User.action?findGroups=",{userOrganization: $(this).val(), ajax: 'true'}, function(j){
            var options = '';
                for (var i = 0; i < j.length; i++) {
                    options += '<option value="' + j[i] + '">' + j[i] + '</option>';
                }
                $("select#groups").html(options);
            })
            
            $.getJSON("User.action?findRoles=",{userOrganization: $(this).val(), ajax: 'true'}, function(j){
            var options = '';
                for (var i = 0; i < j.length; i++) {
                    options += '<option value="' + j[i] + '">' + j[i] + '</option>';
                }
                $("select#fedoraRoles").html(options);
            })
        });
        
        var added = window.location.search.replace( "?", "" );
        if(added != undefined && added.length > 0){
        	alert("User added!");
        }
    });
    </script>
	
	<s:useActionBean var="useraction" beanclass="fi.mamk.osa.stripes.UserAction"/>
	<s:form action="/User.action">
	
		<h1 class="largeheader"><fmt:message key="header.adduser"/></h1><div class="header-hr"></div>
		<p>
			<s:label for="fname" class="mediumsizelabel"><fmt:message key="user.firstname"/></s:label>
			<s:text name="fname"/>
		</p>
		
		<p>
			<s:label for="lname" class="mediumsizelabel"><fmt:message key="user.lastname"/></s:label>
			<s:text name="lname"/>
		</p>
		
		<p>
			<s:label for="mail" class="mediumsizelabel"><fmt:message key="user.mail"/></s:label>
			<s:text name="mail"/>
		</p>
	
		<p>
			<s:label for="preferredLanguage" class="mediumsizelabel"><fmt:message key="user.lang"/></s:label>
			<s:select name="preferredLanguage"  >
				<s:option value="en_US">En</s:option>
				<s:option value="fi_FI">Fi</s:option>
			</s:select>
		</p>
		
		<p>
			<s:label for="usrRole" class="mediumsizelabel"><fmt:message key="user.role"/>:</s:label>
			<s:select name="usrRole">
				<s:option value="admin">admin</s:option>
				<s:option value="manager">manager</s:option>
				<s:option value="reader">reader</s:option>
			</s:select>
		</p>
		
		<p>
	        <s:label for="usrFedoraRole" class="mediumsizelabel"><fmt:message key="user.fedorarole"/></s:label>
	        <s:select name="usrFedoraRole" id="fedoraRoles"></s:select>
        </p>
		
		<p>		
			<s:label for="userOrganization" class="mediumsizelabel"><fmt:message key="user.organization"/></s:label>
			<s:select name="userOrganization" id="organizations">
				<s:option value="">Choose:</s:option>
				<c:choose>
				<c:when test="${user.isInstanceAdmin() == true}">
					<c:forEach items="${useraction.findOrganizations()}" var="item">
						<s:option value="${item}">${item}</s:option>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<s:option value="${user.getOrganization().getName()}">${user.getOrganization().getName()}</s:option>
				</c:otherwise>
				</c:choose>
			</s:select>
		</p>
		
		<p>		
			<s:label for="userGroup" class="mediumsizelabel"><fmt:message key="user.group"/>:</s:label>
			<s:select name="userGroup" id="groups"></s:select>
		</p>
		
		<p>		
			<s:label for="password" class="mediumsizelabel"><fmt:message key="user.password"/></s:label>
			<s:password name="password"/>
		</p>
		
		<p>		
			<s:label for="repeatPassword" class="mediumsizelabel"><fmt:message key="user.password"/></s:label>
			<s:password name="repeatPassword"/>
		</p>
			
		<input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.addsuccess"/>" />	
		<input type="hidden" name="messageAddFail" value="<fmt:message key="message.addfail"/>" />	
		
		<s:submit name="createUser"><fmt:message key="button.add"/></s:submit>
	</s:form>
		
	</s:layout-component>
</s:layout-render>

