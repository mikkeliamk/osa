$(function(){
	//Uncomment for blue highlight/selected effect
	/*$("li#"+index).removeClass('navigationlink');
	$("li#"+index).addClass('activelink');
	$("a#"+index+"link").removeClass('navigationlink');
	$("a#"+index+"link").addClass('activelink'); */

	$("#tabs li").click(function() {
		//  First remove class "active" from currently active tab
		$("#tabs li").removeClass('active');
		//  Now add class "active" to the selected/clicked tab
		$(this).addClass("active");
		
		//  Hide all tab content
		$(".tab_content").hide();
		
		//  Here we get the href value of the selected tab
		var selected_tab = $(this).find("a").attr("href");
		//  Show the selected tab content
		$(selected_tab).show();
		
		//  At the end, we add return false so that the click on the link is not executed
	    return false;
	});
});