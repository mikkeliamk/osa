<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:if test="${empty user}"><c:redirect url="/" /></c:if>
<c:if test="${user.isAdmin() == false || user.isInstanceAdmin() == false}"><c:redirect url="/" /></c:if>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="/layout/osa_header.jsp"/>
    </s:layout-component>
    
    <s:layout-component name="html_head">
	    <script>
	    var tablerow = ""; 
	    $(function () {
	        $("label#breadcrumb").html("<a href='admin.jsp'><fmt:message key='link.admin'/></a> &raquo; <fmt:message key='link.disposal'/>");
	        document.title = "<fmt:message key='link.disposal'/> | " + document.title;      
	        
	        var added = window.location.search.replace( "?", "" );
	        if(added != undefined && added.length > 0){
	            alert("Disposal list added!");
	        }
	        
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
	        
	        $("#disposaltable").on("click", ".select", function(){
	        	var check = false;
	            if( !$(this).hasClass('checked')){
	                check = true;
	            }
	            $(this).toggleClass('checked');
	            $('#disposaltable :checkbox').prop('checked', check);
	        });
	        
	        $("#disposaltable").on("click", ".cancel", function(){
	            var pid = $(this).attr("id").split("|")[0];
	            var name = $(this).attr("id").split("|")[1];
	            var type = "img/icons/silk/"+$(this).attr("id").split("|")[2];
	
	            var rowid = pid.split("-")[1];
	            $(".confirmtext").html("<fmt:message key='message.disposemethod.deletefromdisposallist'/>: <br/>");
	            $(".confirmtext").append("<span class='deletelistitem'><img src='"+type+"' class='smallicon'/> "+ name+"</span>");
	            
	            $("#dialog").dialog('option', 'buttons', {
	                  "<fmt:message key='button.confirm'/>": function () {
	                    $.ajax({
	                        url: "Disposal.action?deleteFromDisposalList=",
	                        data: {pid: pid},
	                        complete: function(data, statusText) {
	                        	if (statusText == "true") {
		                            $("#row_"+rowid).css("background","#FF3700");
		                            $("#row_"+rowid).fadeOut(400, function(){
		                                $(this).remove();
		                            });
	                        	} else {
	                        		// TODO:
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
	        
	        $("#disposaltable").on("click", ".delete", function(){
	            var pid = $(this).attr("id").split("|")[0];
	            var name = $(this).attr("id").split("|")[1];
	            var type = "img/icons/silk/"+$(this).attr("id").split("|")[2];
	            var rowid = pid.split("-")[1];
	            $(".confirmtext").html("<fmt:message key='message.disposemethod.deletefromfedora'/>: <br/>");
	            $(".confirmtext").append("<span class='deletelistitem'><img src='"+type+"' class='smallicon'/> "+ name+"</span>");
	            
	            $("#dialog").dialog('option', 'buttons', {
	                  "<fmt:message key='button.confirm'/>": function () {
	                    $.ajax({
	                        url: "Disposal.action?deleteFromFedora=",
	                        data: {pid: pid},
	                        complete: function(data, statusText) {
	                            $("#row_"+rowid).css("background","#FF3700");
	                            $("#row_"+rowid).fadeOut(400, function(){
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
	    }); // DOM ready
	    </script>
    </s:layout-component>
    
    <s:layout-component name="content">
    <h1 class="largeheader"><fmt:message key="link.disposal" /></h1><div class="header-hr"></div>

        <table id="disposaltable">       
            <thead>
                <tr>
                    <th><input type='checkbox' class='select'></th>
                    <th><fmt:message key='headerkey.title'/></th>
                    <th><fmt:message key='headerkey.user'/></th>
                    <th><fmt:message key='headerkey.date'/></th>
                    <th><fmt:message key='button.cancel'/></th>
                    <th><fmt:message key='button.delete'/></th>
                </tr>
            </thead>     
        </table>
        
        <div id="dialog">
            <div class="confirmicon"></div>
            <div class="confirmtext"></div>
        </div>
        
    </s:layout-component>
</s:layout-render>