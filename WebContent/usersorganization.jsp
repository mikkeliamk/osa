<%@ include file="/layout/taglibs.jsp" %>

<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="/layout/osa_header.jsp"/>
	</s:layout-component>
	
    <s:layout-component name="content">
        <s:messages/>
        <s:errors globalErrorsOnly="true"/>
		<script src="js/jquery.flot.js"></script>
		<script src="js/jquery.flot.categories.js"></script>
		<script src="js/jquery.flot.time.min.js"></script>
        <script>
    	$(function() {
            $.getJSON("Search.action?getSearchJson=",{ajax: 'true'}, function(j){
            	var data = [];
                for (var i = 0; i < j.length; i++) {
                	
                	var increased = false;
                	if(j[i]['metadataFields']['c.created'] != undefined){
                		
	                	var datepieces = j[i]['metadataFields']['c.created'].split(".");
	                	
	                	var jtn = datepieces[2]+","+datepieces[1]+","+datepieces[0];
	                	
	                	var cdate = new Date(jtn).getTime();
	                	
	                	for(var n = 0;n<data.length;n++){
	                		if(data[n][0] == cdate){
	                			data[n][1]++;
	                			increased = true;
	                		}
	                	}
	                	
	                	if(!increased){
	                		var datarow = [cdate, 1];
	                		data.push(datarow);
	                	}
                	}
                }
        		$.plot("#orgHistoryGraph", [ data], {
        			colors: ['navy'],
        			series: {
        				bars: {
        					show: true,
        					barWidth: 1000*60*60*24
        				}
        			},
        			xaxis: {
        				mode: "time",
        				tickLength: 0
        			}
        		})
            });
    	});
        </script>
        
        
        <h3>${user.organization.name}</h3>
        
        <div id="orgHistoryGraph"></div>
    </s:layout-component>
</s:layout-render>