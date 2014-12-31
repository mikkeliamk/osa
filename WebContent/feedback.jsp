<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:if test="${empty user || user.getHighestAccessRight() < 100}"><c:redirect url="/" /></c:if>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
		
	</s:layout-component>
	
    <s:layout-component name="content">
        <s:messages/>
        <s:errors globalErrorsOnly="true"/>
  
     <s:useActionBean var="json" beanclass="fi.mamk.osa.stripes.FeedbackAction"/>
     
    <script>
    
     function removeData(id){
 		$.post("Feedback.action?removeFeedback=", { jsonData: id} )
		.done(function(data) {
			$('#'+id).fadeOut(function(){
				$(this).remove();
			});
		}); 	 
     }
     
     $(function(){
   	    var data = ${json.jsonData};
		var tabledata = "<table class='feedbacktbl' id='feedbackdatatbl'>";
		tabledata += "<thead><tr><th width='8%'>ID</th><th >Message</th><th width='60%'>Browser info</th><th width='10%'>Date/time</th></tr><thead>";
		tabledata += "<tbody>";
		for(var i = 0;i<data.length;i++){
		 tabledata += '<tr class="feedbacktbl" id="'+data[i]['_id']['$oid']+'" >';
		 tabledata += '<td >'+data[i]['identifier']+'</td>';
		 tabledata += '<td><a href="'+data[i]['image']+'" target="_blank">Image</a>';
		 tabledata += '<pre>';
		 for(var y = 0;y<data[i]['userInput'].length;y++){
			 tabledata += data[i]['userInput'][y]['name']+" = "+data[i]['userInput'][y]['value']+'<br/>';
		 }
		 tabledata += '</pre></td><td><button id="toggleTechInfo" onclick="$(this).next().toggle();">Details</button><pre>';
		 for(var key in data[i]['collectedData']){
				 if(key == 'plugins'){
				 tabledata += key+"=<br/>";
				for (var n = 0;n<data[i]['collectedData'][key].length;n++){ 						
					 tabledata += data[i]['collectedData'][key][n]['name']+"<br/>";	
				 }
			 }else if(key == 'timestamp'){
				 var date = new Date(data[i]['collectedData'][key]); 
				 tabledata += key+" = "+date+'<br/>';		 
			 }else{
			 tabledata += key+" = "+data[i]['collectedData'][key]+'<br/>';
			 }
		 }
	     var date = new Date(data[i]['collectedData']['timestamp']);
		 tabledata += '</pre>';
		 tabledata += '<button onclick=removeData("'+data[i]['_id']['$oid']+'")>Remove</button></td>';
		 tabledata += '<td>'+date.toDateString()+' '+date.toLocaleTimeString()+'</td>'; 
		 tabledata += '</tr>';
		}
		tabledata += "</tbody>";
		tabledata += '</table>';
		$('#feedbacklist').append(tabledata);
		
		   $('#feedbackdatatbl').dataTable( {
   	     
   	    } );

		$('tr.feedbacktbl > td + td > pre').hide();
     });
     
     </script>
     
     <div id="feedbacklist"></div>
          
    </s:layout-component>
</s:layout-render>