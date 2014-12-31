<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:redirect url="/" />

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">	
		<jsp:include page="/layout/osa_header.jsp"/>
	</s:layout-component>
	<s:layout-component name="html_head">
	    <script>
		    $(function () {
		    	$("label#breadcrumb").html("<a href='admin.jsp'><fmt:message key='link.admin'/></a> &raquo; <fmt:message key='link.addgroup'/>");
		    	document.title = "<fmt:message key='link.addgroup'/> | " + document.title;    	
		    	
			    var added = window.location.search.replace( "?", "" );
			    if(added != undefined && added.length > 0){
			    	alert("Group added!");
			    }
		    });
	    </script>
	</s:layout-component>
    
    <s:layout-component name="content">
		<s:useActionBean var="useraction" beanclass="fi.mamk.osa.stripes.UserAction"/>
		<s:form action="/Group.action">
		
			<h1 class="largeheader"><fmt:message key="header.addgroup"/></h1><div class="header-hr"></div>
			
			<p>
				<s:label for="groupName" class="mediumsizelabel"><fmt:message key="group.name"/></s:label>
				<s:text name="groupName"></s:text>
			</p>
			
			<p>
				<s:label for="groupOrganization" class="mediumsizelabel"><fmt:message key="group.organization"/></s:label>
				<s:select name="groupOrganization">
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
	
			<input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.addgroupsuccess"/>" />	
			<input type="hidden" name="messageAddFail" value="<fmt:message key="message.addfail"/>" />	
		
			<s:submit name="createGroup"><fmt:message key="button.add"/></s:submit>
		</s:form>
    </s:layout-component>
</s:layout-render>
	