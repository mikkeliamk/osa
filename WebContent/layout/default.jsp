<%@ include file="/layout/taglibs.jsp" %>

<c:if test="${empty gui}">
	<s:useActionBean var="loginaction" event="setdefaultgui" beanclass="fi.mamk.osa.stripes.LoginAction"/>
</c:if>

<s:layout-definition>
    <!DOCTYPE html>
    <html>
        <head>
            <title>${gui.appTitle}</title>
            <link rel="shortcut icon" type="image/x-icon" href="img/favicon.ico">
            <!-- <link rel="stylesheet" type="text/css" href="css/reset.css"> -->
            <link rel="stylesheet" type="text/css" href="css/core.css">
            <link rel="stylesheet" type="text/css" href="${gui.cssFile}">
            <link rel="stylesheet" type="text/css" href="css/jqueryui.css">
            <link rel="stylesheet" type="text/css" href="js/themes/datatables/css/jquery.dataTables_themeroller.css">
            <link rel="stylesheet" type="text/css" href="css/jquery.qtip.min.css">
            <link rel="stylesheet" type="text/css" href="css/fileuploader.css">
             
            <script src="js/jquery-1.10.1.js"></script>
            <script src="js/jquery-ui-1.10.3.js"></script>
            <script src="js/jquery.dataTables.min.js"></script>
            <script src="js/masonry.js"></script>
            <script src="js/html2canvas.js"></script>
            <script src="js/jquery.qtip.min.js"></script>
            <script src="js/fileuploader.js"></script>
            <script src="js/prettyCommentsModified.js"></script>
            <script src="js/TOTALLY_NOT_easteregg.js"></script>
            <!-- Datepicker localization files  -->
            <script src="js/jquery-ui-datepicker-fi.js"></script>
            <script src="js/jsp-js-functions/misc-utils.js"></script>
            <script src="js/jsp-js-functions/breadcrumb-pagetitle.js"></script>
            <script src="js/jsp-js-functions/messagebundle-and-icons.js"></script>
			<script>
			    var username = "${user.cn}";
			    $(function(){
			    	if ($('.errordiv').length > 0 && $('.error-msg').length > 0){
			    		if ( $('.error-msg').text() == "<fmt:message key='error.duplicatelogin'/>") {
			    			$(".errordivicon img").prop("src", "img/icons/silk32/information.png");
                            $( ".errordiv" ).dialog({
                                modal: true,
                                minWidth: 500,
                                draggable: false,
                                resizable: false,
                                title: "<fmt:message key='header.confirmationrequired'/>",
                                buttons: {
                                    Ok: function() {
                                        var $this = $(this);
                                        $.ajax({
                                            url: "Login.action?forceLogin=",
                                            global: false,    // this makes sure ajaxStart is not triggered
                                        	success: function(data){
                                        		$this.dialog( "close" );
                                        		window.location.href = "index.jsp"; 
                                        	}
                                        });
                                    },
                                    Cancel: function() {
                                        $( this ).dialog( "close" );
                                    }
                                }
                            });
                            
			    	    } else {
				    		$( ".errordiv" ).dialog({
				    			modal: true,
				    			minWidth: 500,
				    			draggable: false,
				    			resizable: false,
				    			title: "<fmt:message key='header.error'/>",
				    			buttons: {
					    			Ok: function() {
					    				$( this ).dialog( "close" );
					    			}
				    			}
				    		});
			    	    }
			    	}

		    		var currentYear = new Date().getFullYear();
			    	$(".datepicker").datepicker({
			    		firstDay: 1,
			    		changeYear: true,
			    		changeMonth: true,
			    		dateFormat: "yy-mm-dd",
			    		yearRange: "1300:"+currentYear
			   		});
			    	
			    });
			</script>
			<script src="js/jsp-js-functions/feedback.js"></script>
			<jsp:include page="/layout/messages.jsp"/>
            <s:layout-component name="html_head"/>
        </head>
        <body class="body">
       		<div id="wrapper">
				<%-- div to display feedback window --%>
				<div id="feedbackdiv">
				<div class="feedbackInfobar">
					<fmt:message key="header.feedback.giving"/><button class="closeFeedback" style="float: right;"><fmt:message key="button.close"/></button>
				</div>
					<div id="feedbackctrl">
						<p><fmt:message key="header.feedback.drag"/></p><hr/>
						<p><fmt:message key="header.feedback.draw"/></p>
						<p>
							<fmt:message key="header.feedback.color"/>
							<select id="feedback-brushcolor">
								<option value="black"><fmt:message key="color.black"/></option>
								<option value="blue"><fmt:message key="color.blue"/></option>
								<option value="green"><fmt:message key="color.green"/></option>
								<option value="red" selected="selected"><fmt:message key="color.red"/></option>
								<option value="yellow"><fmt:message key="color.yellow"/></option>
							</select>
							<br>
							<fmt:message key="header.feedback.size"/>  <div id="brushsize"></div>
						</p>
						<p><button id="clearFeedback"><fmt:message key="button.clear"/></button></p>
						<input id="anonymousFeedback" type="checkbox" /> <label for="anonymousFeedback"><fmt:message key="header.feedback.anonymous"/></label><br/>
						<form id="userInput">
							<p><fmt:message key="capture.subject"/>:</p>
							<p><input name="subject" type="text"/></p>
							<p><fmt:message key="header.feedback.more"/></p>
							<p><textarea name="freetext"></textarea></p>
						</form>
				
						<p><button id="feedbackSend"><fmt:message key="button.sendfeedback"/></button> <button class="closeFeedback"><fmt:message key="button.close"/></button></p>
					</div>
				</div>
	        
				<s:layout-component name="header">
					<%-- Main menu has header-main.jsp, other pages have header.jsp  --%>
				</s:layout-component>
				
				<div class="contentDiv">
					<s:layout-component name="content"/>
				    <!-- Clear fix for floats  -->     
                    <div class="clearfix"></div>
				</div>

<%-- 			For 100% width page, normal is 60% 	
				<div class="extendedContentDiv">
					<s:layout-component name="extendedContent"/>
				</div> --%>
				
				<div class="footer">
				    Follow OSA - Open Source Archive at 
				    <a href="http://osarchive.wordpress.com/" target="_blank">Wordpress</a> <span class="blue bold">/</span> 
					<a href="https://twitter.com/OSArchive" target="_blank">Twitter</a>
				</div>
				
				<%-- Div to display modal window 
				<div class="modal"><div id="modalload" class="modal"><p><fmt:message key="datatable.loading" /></p></div></div>
				--%>
			</div>
			<div class="errordiv">
				<div class="errordivicon">
					<img src="img/icons/silk32/cancel.png" />
				</div>
				<div class="errordivtext">
				<s:errors>
					<s:errors-header></s:errors-header>
					<p class="error-msg"><s:individual-error/></p>
					<s:errors-footer></s:errors-footer>
				</s:errors>
				</div>
			</div>
        </body>
    </html>
</s:layout-definition>
