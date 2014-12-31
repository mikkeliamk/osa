
$(this).keyup(function(event) {
	if (event.which == 27) { 
		disablePopup();  
		}
	
});

function closeloading() {
	$("div.loader").fadeOut('normal');
	
}

var popupStatus = 0; 
function loadPopup(contentId) {
	this.contentId = contentId;
	$("#add-group-popup #loader").show();
	if(popupStatus == 0) { 
		closeloading(); 
		$(contentId).fadeIn(0500);         
		popupStatus = 1; 
		}      
	
}

function disablePopup() {
	if(popupStatus == 1) { 
		$(".updatePopup displayNone popup-window").fadeOut("normal");
		popupStatus = 0; 
		}
	
}  