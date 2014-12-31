$(function() {
	$("#closeBrowsingDiv").click(function(){
        $("#browsingDiv").slideToggle('fast');
    });
	
	$(".browseBtn").on("click", function(e){
        e.preventDefault();

        selectedrelation = $(this).attr("id").split("_")[1];
        selectedrelationnameid = $(this).attr("name");
        
        $('#browseTree').unbind();
        $('#selectBrowse').unbind();

	    $('#selectBrowse').click(function(){
	        $("#relationID_"+selectedrelation).val(browseval);
	        $("#relationID_"+selectedrelation).blur();

	        if (browsename != "") {
	        	$("#relationNAME_"+selectedrelation).val(browsename);
                $("#relationNAME_"+selectedrelation).blur();
	        }
	        
	        $("#browsingDiv").slideToggle('fast');
	    });
        		 
		if(typeof(Storage)!=="undefined"){
		    store = true;   
                    
			if(sessionStorage.getItem("browsetree"+selectedrelationnameid)){
			    $.getJSON("Ingest.action?getContextTime=",{ajax: 'true'}, function(j){
			        var storageTime = sessionStorage.getItem("ctxtime");
			        if(j > storageTime){
			            sessionStorage.clear();
			            $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid}, function(n){
			                sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(n));
			                sessionStorage.setItem("ctxtime",new Date().getTime());
			                initalizeTree(n);   
			             });
			        }else{
			            // TODO: check this
			            sessionStorage.clear();
			            if (sessionStorage.getItem("browsetree"+selectedrelationnameid) != null) {
			                initalizeTree(JSON.parse(sessionStorage.getItem("browsetree"+selectedrelationnameid)));
			            } else {
			                $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid}, function(n){
			                    sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(n));
			                    sessionStorage.setItem("ctxtime",new Date().getTime());
			                    initalizeTree(n);   
			                });
			            }
			        }
			    });    
			}else{ // if sessionStorage
		    	$.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid}, function(j){
			        sessionStorage.setItem(("browsetree"+selectedrelationnameid),JSON.stringify(j));
			        sessionStorage.setItem("ctxtime",new Date().getTime());
			        initalizeTree(j);   
			    });
			}	      
		}else{ // if typeof
		    $.getJSON("Ingest.action?listCollections=",{rootpid: ':root', selectedrelationname: selectedrelationnameid}, function(j){
		        initalizeTree(j);
		    });
		}     
		$("#browsingDiv").slideToggle('fast');
	});
	
	var currYear = new Date().getFullYear();
	                
	for (var c = 1300; c < currYear; c += 100)
	{
	    $(".selectcentury").append("<option value='"+c+"'>"+c+"</option>");
	}
	
	for (var d = 1300; d < currYear; d += 10)
	{
	    $(".selectdecade").append("<option value='"+d+"'>"+d+"</option>");
	}
	
	$(".selectdecade").change(function(){
	    var year = parseInt($(this).val());
	    var dBegin = new Date(year, 0, 1);
	    var dEnd = new Date((year+9), 11, 31);
	
	    var beginString = dBegin.getFullYear()+"-"+padZero((dBegin.getMonth()+1))+"-"+padZero(dBegin.getDate());
	    var endString = dEnd.getFullYear()+"-"+padZero((dEnd.getMonth()+1))+"-"+padZero(dEnd.getDate());
	    
	    $("#temprangeBegin").val(beginString);
	    $("#temprangeEnd").val(endString);
	    
	    $(this).parent().hide();
	});
	
	$(".selectcentury").change(function(){
	    var year = parseInt($(this).val());
	    var dBegin = new Date(year, 0, 1);
	    var dEnd = new Date((year+99), 11, 31);
	    
	    var beginString = dBegin.getFullYear()+"-"+padZero((dBegin.getMonth()+1))+"-"+padZero(dBegin.getDate());
	    var endString = dEnd.getFullYear()+"-"+padZero((dEnd.getMonth()+1))+"-"+padZero(dEnd.getDate());
	    
	    $("#temprangeBegin").val(beginString);
	    $("#temprangeEnd").val(endString);
	    
	    $(this).parent().hide();
	});
	
	$(".opentimerangeselect").click(function(){
	    $(".timerangeselect").toggle(); 
	});
	
	function padZero(d)
	{
	    if(d < 10) {
	        d = "0"+d;
	    }
	    return d;
	}
});