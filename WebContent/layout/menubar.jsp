<%@ include file="/layout/taglibs.jsp" %>
<c:if test="${!empty user && !user.isAnonymous()}">
	<script src="js/jsp-js-functions/get-basket-size.js"></script>
	<script src="js/jsp-js-functions/sidepanel-latest-edits.js"></script>
</c:if>
<script>
$(function(){
	// Positioning login window correctly
	$('#loginli').qtip({
		prerender: true,
		content: {
			text: $("#loginmenu")
		},
		position: {
			my: 'top right', 
			at: 'bottom right',
				adjust: {
					x: -10
				}
		},
		show: 'click',
		hide:{
			event: 'unfocus click'
		},
		events: {
			visible: function(event, api) {
				$("input[name=username]").focus();
			}
		},
		style: { 
			tip: false,
			classes: 'qtip-dark qtip-shadow'
		}
	});
	
	/********************************
	 * MENUBAR SUBMENU FUNCTIONALITY					
	 ********************************/
	// Preventing links from working, instead open submenu
	$("#feedbacklink, #accountm, #addlink, #login").on('click', function(e) {
		  e.preventDefault();
	});	
	$('#logoutli').click(function() {
		  $('#accountmenu').toggle('fast');
	});
	
	// Simulate link behaviour if clicked li element instead of link inside it
	$('#accountmenu li').click(function(e) {
		e.stopPropagation();
		if (this.id != "logout") {
			window.location.href = $(this).find("a").attr("href");
		}
	});
	
	// Logout
	$('#logout').click(function(e) {
		e.preventDefault();
		e.stopPropagation();
        $.ajax({
            url: "Logout.action?logout=",
            success: function(redir) {
            	sessionStorage.removeItem("fileForm");
                sessionStorage.removeItem("attFileForm");
            	window.location.replace(redir);
            }
        });
	});
}); // DOM ready
</script>

<div class="menu-bar-container">
<noscript>
	<fmt:message key="noscript.info"/><br/>
	<fmt:message key="noscript.request"/>
</noscript>
	<div class="menu-bar">
		<ul>
			<s:link href="#" id="feedbacklink" class="navigationlink"><li id="feedback" class="mainlink-li pointerCursor"><fmt:message key="link.feedback"/></li></s:link>
			<s:link href="/" id="mainlink" class="navigationlink"><li id="main" class="mainlink-li pointerCursor"><fmt:message key="link.main"/></li></s:link>
	        <s:link href="search.jsp" id="searchlink" class="navigationlink"><li id="search" class="mainlink-li pointerCursor"><fmt:message key="link.search"/></li></s:link>
<%-- 	        <s:link href="advancedbrowse.jsp" id="advancedbrowselink" class="navigationlink"><li id="feedback" class="mainlink-li pointerCursor"><fmt:message key="link.advancedbrowse"/></li></s:link> --%>
            
            <c:if test="${user != null && user.anonymous == false && user.getHighestAccessRight() >= 50}">
	            <s:link href="browse.jsp" id="viewlink" class="navigationlink"><li id="view" class="mainlink-li pointerCursor"><fmt:message key="link.browse"/></li></s:link>
		    </c:if>
		    <c:if test="${user.getHighestAccessRight() >= 100}">
		        <s:link href="admin.jsp" id="adminlink" class="navigationlink"><li id="admin" class="mainlink-li pointerCursor"><fmt:message key="link.admin"/></li></s:link>
	        </c:if>
	        <c:choose>
                <c:when test="${empty user || user.anonymous == true}">
				    <li id="loginli" class="mainlink-li pointerCursor"><s:link href="#" id="login" class="navigationlink"><fmt:message key="link.login"/></s:link></li>
		        </c:when>
		        <c:otherwise>
                    <c:if test="${user.getHighestAccessRight() >= 40}">
			        	<s:link href="workspace.jsp" id="workspacelink" class="navigationlink"><li id="workspace" class="mainlink-li pointerCursor"><fmt:message key="link.workspace"/></li></s:link>
		        	</c:if>
                    <s:link href="basket.jsp" id="basketlink" class="navigationlink"><li id="basket" class="mainlink-li pointerCursor"><fmt:message key="link.basket"/> (<span class="basketsize">0</span>)</li></s:link>
		        	<li id="logoutli" class="mainlink-li pointerCursor"><s:link id="accountm" href="#" class="navigationlink">${user.cn}</s:link>
						<div id="accountmenu" class="hiddenmenu">
							<ul>
								<li class="hiddenmenulist"><s:link class="navigationlink" href="usersorganization.jsp">${user.organization.name}</s:link></li>
								<li class="hiddenmenulist"><s:link class="navigationlink" href="editaccount.jsp"><fmt:message key="link.editaccount"/></s:link></li>
								<li class="hiddenmenulist" id="logout"><s:link class="navigationlink" href="#"><fmt:message key="link.logout"/></s:link></li>
							</ul>
						</div>
                    </li>
		        </c:otherwise>
	        </c:choose>
		</ul>
	</div>
</div>

<!-- div to display login window -->
<div id="loginmenu">
	<s:form action="/Login.action" focus="">
		<p>
			<s:label for="username"><fmt:message key="login.username"/></s:label>
			<s:text name="username"/>
		</p>
		<p>
			<s:label for="password"><fmt:message key="login.password"/></s:label>
			<s:password name="password"/>
		</p>
		<s:submit name="login"><fmt:message key="button.login"/></s:submit>
	</s:form>
</div>