//var selected_tab = "";
$(function(){
	// TODO Maybe remove links altogether and use li id attribute instead of link's href
	$("#tabs li, #tabs li a").click(function(e) {
		$("#tabs li").removeClass('active');
		$(".tab_content").hide();
		if (e.target.localName == "li") { // If clicked li-element instead of link inside it
			$(this).addClass("active");
			selected_tab = e.target.children[0].attributes[0].nodeValue;
		} else{
			$(this).parent().addClass("active");
			selected_tab = e.target.attributes[0].nodeValue;
		}
		$(selected_tab).show();
	    return false;
	});
});