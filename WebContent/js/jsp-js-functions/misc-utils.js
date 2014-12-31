$(function(){
	$(document).bind("keypress", function (e) {
		if (e.keyCode == 27) {
			if ($('.popup-window').is(":visible")) {
				modalWindow(false);
				$('.popup-window').hide();
				openedNodes = {}; // Reset nodes' states
			}
			if ($("#sidepanel-container").hasClass("sidepanel-container-full")) {
				modalWindow(false);
				$("#sidepanel-container").removeClass("sidepanel-container-full");
			}
		}
	});
	
	$(".close-popup").click(function(){
        $(this).parent().hide();
    });
	
	$(".messages-container").click(function(){
        $(this).fadeOut("fast", function(){
        	$(this).remove();
        });
    });
	
});

function alertStuff(msg) {
	alert(msg);
}

/** Creates/removes dark semi-opaque background behind modal window
* @param  create      A boolean flag indicating whether the background should be created or removed.<b>[true|false]</b> <br/>
* 					<b>True = create, false = remove</b>
*/
function modalWindow(create) {
    if (create === true && $("body").find("div#modalbackground").length == 0) { 
       $("body").append($("<div/>",{class: "modal", id: "modalbackground"}));
    } else {
    	$("body").find("div#modalbackground").remove();
    }
}

/** Creates delay for given time
* 					
*/
var delay = (function(){
    var timer = 0;
    return function(callback, ms){
      clearTimeout (timer);
      timer = setTimeout(callback, ms);
    };
})();

/** Gets URL parameter values for given parameter.<br/>
 * 	@param name		Name of the parameter to get value from<br/>
 * 	@returns		Parameter value or null if no results found
 */
function getURLParameter(name) {
	var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
	if (results==null){
	   return null;
	}
	else{
		return results[1] || 0;
	} 
}

/** Hides given element when it loses focus.<br/>
 *  I.e. some window is open and user clicks outside of it, the window closes.
 * 
 * @param elementSelector	jQuery selector of an element that should close on focus loss
 * @param event				Event parameter
 */
function hideElementOnFocusLoss(elementSelector, event) {
	var container = elementSelector;
    if (container.is(":visible") && !container.is(event.target) && container.has(event.target).length === 0){
    	container.hide();
    }
}
/** Scrolls page's top to given element's top
* @param  target  Selector of an element to scroll page top to
*/
function scrollToTarget(target) {
	$('html,body').animate({scrollTop: $(target).offset().top}, 'slow', "easeOutCubic");
}

function isEmptyStr(str) {
	return (!str || 0 === str.length);
}

function isBlankStr(str) {
	return (!str || /^\s*$/.test(str));
}