<%@ include file="/layout/taglibs.jsp" %>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
	</s:layout-component>
	
    <s:layout-component name="content">
    <style type="text/css">
	.error{	
	color: red;
	}
	.messages-body {
    min-height: 25px;
    padding: 7px 5px 2px;
    width: 70%;
    }
	.message-success {
    color: rgb(53, 137, 39);
}
	</style>
        <s:messages/>
        <div class="messages-body"></div>
        <s:errors globalErrorsOnly="true"/>
        
        <c:set var="usrDn" scope="session" value ="${user.getDn()}"/>
        
        <script>
        
        var validationLang= false;
        var validationMail= false;
       	$(function () {
       		
       		$("label#breadcrumb").html("<fmt:message key='link.editaccount'/>");
        	document.title = "<fmt:message key='link.editaccount'/> | " + document.title;
        	
	        var dn = "${usrDn}";
	        
	    	$.getJSON("User.action?getAllAttributes=",{userDn: dn, ajax: 'true'}, function(j){
	            var values = '<form id="editAcc">';
	            values += "";
	            for (var i in j) {
	            	if(i != 'objectClass' && i != 'userPassword' && i != 'ou'){
	                	values += '<p><s:label for="'+i+'" class="mediumsizelabel">'+i+'</s:label>';
	                	values += '<input type="text" name="'+i+'" value="'+j[i]+'" class = "fields"/></p>';	 
	            	}
	            }
	            var count = j;
            	values += '<p><s:label for="userPassword" class="mediumsizelabel">new password</s:label>';
            	values += '<input type="password" name="userPassword" value=""/></p>';
            	
            	values += '<p><s:label for="userPasswordNew" class="mediumsizelabel">repeat password</s:label>';
            	values += '<input type="password" name="userPasswordNew" value=""/></p>';
	            
	            values += '<p><input type="submit" value="<fmt:message key='button.save'/>" /></p>';
	            values += '</form>';
	            
	            $("#accContainer").html(values);
	            
	            $(".fields").focusout(function(){	            
	            	validateValue($(this).attr("name"));
	            });	         
	            
	            $('#editAcc').submit(function() {	            
	            	for (var i in count) {	            		
	 	                	validateValue(i);
	 	            	}
	            	
	 	            if((validationMail) && (validationLang)){	 	            
	 	            	 if($('input[name=userPassword]').val() == $('input[name=userPasswordNew]').val()){
	 		            	
	 		            	var newpw = true;
	 		            	
	 		            	if($('input[name=userPassword]').val() == undefined || $('input[name=userPassword]').val() == ''){
	 		            		newpw = false;
	 		            	}
	 		            	
	 		            	var jsonmod = JSON.stringify($('#editAcc').serializeObject(newpw));	 
	 		            	  $.getJSON("User.action?modifyAttributes=",{userDn: dn, modJson: jsonmod, ajax: 'true'}, function(answer){	
	 		            		  
	 		                });	 		            	 
 		            		 $(".messages-body").html("<p class='message-success'>Edited successfully</p>");	
	 		                return false;
	 		            }else{
	 		            	alert("Passwords are not matching!");
	 		            }
	 	            }
		           
	            });
	            
	    	});
       	});
    	
     	function validateValue(name){       
       		var value = $("[name='"+name+"']").val();       	
       		if (value != 'undefined'){
       			switch(name){
           		case "mail":           			
           			var pattern = /^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$/;
           			if (value.search(pattern) == -1){
           				validationMail = false;
           				$("[name='"+name+"']").attr("class","error");
           				$("[name='"+name+"']").val("invalid email");
           			}
           			else{    
           				validationMail = true;
           				$("[name='"+name+"']").attr("class","");           				
           			}
           			break;
           		case "preferredLanguage":           		
           			var pattern = /^[a-z]{2}_[A-Z]{2}$/;
           			if (value.search(pattern) == -1){
           				validationLang = false;
           				$("[name='"+name+"']").attr("class","error");
           				$("[name='"+name+"']").val("use fromat en_US");
           			}
           			else{        
           				validationLang = true;
           				$("[name='"+name+"']").attr("class","");
           				
           			}
           			break;
           		}
       		}
       		
       	}
     
       	
    	$.fn.serializeObject = function(updatePw){
    	    var o = {};
    	    var a = this.serializeArray();
    	    $.each(a, function() {
    	    	if(this.name !== 'userPasswordNew'){
    	    		if(!updatePw && this.name == 'userPassword'){
    	    			//Dont serialize password if it is not going to change
    	    		}else{
		    	        if(o[this.name] !== undefined) {
		    	            if (!o[this.name].push) {
		    	                o[this.name] = [o[this.name]];
		    	            }
		    	            o[this.name].push(this.value || '');
			    	    }else{
			    	        o[this.name] = this.value || '';
			    	    }
    	    		}
    	    	}
    	    });
    	    return o;
    	};
    	
        </script>
		
		<div id="accContainer" class="form-section"></div>
    </s:layout-component>
</s:layout-render>