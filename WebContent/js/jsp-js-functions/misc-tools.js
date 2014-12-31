/** Creates/removes dark opaque background behind modal window
* @param  create      A boolean flag indicating whether the background should be created or removed.<b>[true|false]</b> <br/>
* 					<b>True = create, false = remove</b>
*/
function modalWindow(create) {
    if (create === true) { 
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