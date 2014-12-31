<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:redirect url="/" />

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="/layout/osa_header.jsp"/>
	</s:layout-component>
	
    <s:layout-component name="content">
        <s:messages/>
        <s:errors globalErrorsOnly="true"/>
        
	<s:form action="/Organization.action">
	
		<h1 class="largeheader"><fmt:message key="header.addorganization"/></h1>
		
		<s:label for="organizationName"><fmt:message key="organization.name"/></s:label>
		<s:text name="organizationName"/><br/>
		
		<s:label for="displayName">Display name:</s:label>
		<s:text name="displayName"/><br/>
		
		<s:label for="organizationDescription"><fmt:message key="organization.desc"/></s:label>
		<s:text name="organizationDescription"/><br/>
		
		<s:label for="confFile"><fmt:message key="organization.conffile"/></s:label>
		<s:text name="confFile"/><br/>
		
		<h2><fmt:message key="organization.admin"/></h2>
				
		<s:label for="adminFirstname"><fmt:message key="user.firstname"/></s:label>
		<s:text name="adminFirstname"></s:text><br/>

		<s:label for="adminLastName"><fmt:message key="user.lastname"/></s:label>
		<s:text name="adminLastName"></s:text><br/>
		
		<s:label for="adminMail"><fmt:message key="user.mail"/></s:label>
		<s:text name="adminMail"></s:text><br/>
				
		<h2><fmt:message key="organization.contactheader"/></h2>
	
		<s:label for="contact"><fmt:message key="organization.contact"/></s:label>
		<s:textarea rows="5" name="contact"/><br/>
		
		<input type="hidden" name="messageAddSuccess" value="<fmt:message key="message.addorganizationsuccess"/>" />	
		<input type="hidden" name="messageAddFail" value="<fmt:message key="message.addfail"/>" />	
		
		<s:submit name="addOrganization"><fmt:message key="button.add"/></s:submit>	
     </s:form>
    </s:layout-component>
</s:layout-render>