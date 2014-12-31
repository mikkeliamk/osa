$(function(){	
	var brushSize;
	var originalImage;
	$('#feedbackSend').click(function(){
		
    	// If user selected to give anonymous feedback, draw black box on top the name
    	if($("#anonymousFeedback").is(':checked')){
			var cCanvas = document.getElementById('screenShot');
			var cContext = cCanvas.getContext('2d');
			
			cContext.beginPath();
			cContext.rect(0, 0, cCanvas.width, 50);
			cContext.fillStyle = 'black';
			cContext.fill();
    	}
    	
		var img = convertCanvasToImage(document.getElementById('screenShot'));
		var usrData = $('#userInput').val();
		
		var toServer = {};
		toServer['image'] = img;
		toServer['userInput'] = $("#userInput").serializeArray();
		swidth = screen.width;
		sheight = screen.height;
		
		navigator["appCodeName"];
		navigator["appName"];
		navigator["appMinorVersion"];
		navigator["cpuClass"];
		navigator["platform"];
		navigator["plugins"];
		navigator["opsProfile"];
		navigator["userProfile"];
		navigator["systemLanguage"];
		navigator["userLanguage"];
		navigator["appVersion"];
		navigator["userAgent"];
		navigator["onLine"];
		navigator["cookieEnabled"];
		navigator["mimeTypes"];
		
		var collectedData = {};
		
		if(!$("#anonymousFeedback").is(':checked')){
			collectedData['username'] = username;
		}
		
		collectedData['timestamp'] = new Date().getTime();

		collectedData['URL'] = document.URL;
		collectedData["screenwidth"] = swidth;
		collectedData["screenheight"] = sheight;
 		   for(var p in navigator){
 			   if(p == "plugins"){
 				   var plugins = [];
 				   var count = 0;
 				   for(var n in navigator[p]){
 					  var y = {};
 					 	y["name"] = navigator[p][n].name;
 					 	y["filename"] = navigator[p][n].filename;
 					 	y["description"] = navigator[p][n].description;
 						y["version"] = navigator[p][n].version;
 						plugins[count] = y;
 						count++;
 				   }
 				  collectedData[p] = plugins; 
 			   }else if(p == "mimeTypes"){
 				   //dont save
 			   }else{
 				  collectedData[p] = navigator[p]; 
 			   }	  
		   }

		   toServer["collectedData"] = collectedData;
		
		   var json = JSON.stringify(toServer);
		 	 
		   if((img == getOriginalImage()) && (!$("#userInput p [name='subject']").val())){
			   alert("Make screenshot or fill subject");
		   }
		   else{
			   $.post("Feedback.action?sentFeedback=", { jsonData: json} )
				.done(function(data) {
					alert(data);
					$('#feedbackdiv').fadeOut('fast', function() {
						$('#feedbackctrl input, #feedbackctrl textarea').val("");
					});
				});
		   }
		   
		
	});

	$( "#brushsize" ).slider({ 
		min: 1, 
		max: 25, 
		slide: function( event, ui ) {
			setBrushSize(ui.value);		      
			}
	});
		
	
	$('.closeFeedback').click(function(){
		closeFeedback();
		
	});
	
	$(document).bind("keypress", function (e) {
		if (e.keyCode == 27 && $('#feedbackdiv').is(":visible")) {
			closeFeedback();
		}
	});
	
	// Clear any markings done to window by taking a new screenshot of the window
	$('#clearFeedback').click(function(){
		$('#screenShot').remove();
		$('#feedbackdiv').slideToggle('fast', function() {
			takeAscreenShot();
		});
		
	});
					
	$('#feedback').click(function(){
		if( $('#feedbackdiv').is(':visible') ) {
			$('#feedbackdiv').empty();
			$('#feedbackdiv').slideToggle('fast', function() {});
		}else {
			takeAscreenShot();
		}
	}); 

	// To make feedback form window draggable
	$("#feedbackctrl").draggable({
		containment: "#wrapper"
	});
	
	// Frontpage change log
	$("#show-past-changes").click(function(){
		$(".past-changes").slideToggle("fast");
	});
}); // DOM ready

var prevX;
var prevY;

function closeFeedback() {
	document.documentElement.style.cursor = 'none';
	$('#feedbackdiv').fadeOut('fast');
	// Empty text fields after closing feedback window
	$('#feedbackctrl input, #feedbackctrl textarea').val("");
	$('#screenShot').remove();
}

function convertCanvasToImage(canvas) {
	return canvas.toDataURL("image/png");
}

function takeAscreenShot(){
	html2canvas(document.body, {
		onrendered: function(canvas) {
			canvas.setAttribute("id","screenShot");
			setBrushSize("1");
			$('#feedbackdiv').append(canvas);
			setOriginalImage(convertCanvasToImage(document.getElementById('screenShot')));

			var base64 = "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";
			document.documentElement.style.cursor = 'url("data:image/x-icon;base64,'+base64+'"), auto';
		

			var fillColor = '#ff0000';
			var width = canvas.width;
			var height = canvas.height;
			 
	        var ctx = canvas.getContext('2d');
	        ctx.clearTo = function(fillColor) {
	            ctx.fillStyle = fillColor;
	            ctx.fillRect(0, 0, width, height);
	        };

	        // bind mouse events
	        canvas.onmousemove = function(e) {
	            if (!canvas.isDrawing) {
	            	prevX = x; prevY = y;
	               return;
	            }
	            var x = e.pageX - this.offsetLeft;
	            var y = e.pageY - this.offsetTop;
	            ctx.beginPath();
	            ctx.strokeStyle = $('#feedback-brushcolor').val();	        
	           
	            	ctx.lineWidth = getBrushSize();	           
	         
	         //   ctx.lineWidth = $('#feedback-brushsize').val();
	            ctx.lineJoin = "round";
	            
	            ctx.moveTo(prevX, prevY);
	            ctx.lineTo(x, y);
	            ctx.closePath();
	            ctx.stroke();
	            
	            prevX = x; prevY = y;
	            
	        };
	        canvas.onmousedown = function(e) {
	            canvas.isDrawing = true;
	        };
	        canvas.onmouseup = function(e) {
	            canvas.isDrawing = false;
	        };
	       
	        $('#feedbackdiv').slideToggle('fast', function() {});
		}
	}); 
	
}
function setBrushSize(size){
	brushSize = size;
}
function getBrushSize(){
	return brushSize;
}
function setOriginalImage(image){
	originalImage = image;
}
function getOriginalImage(){
	return originalImage;
}
