var contentModels = "";
/** Gets currently active content models from Fedora in JSON format.
 * 
 * @param handleData		A callback function that handles the returned data
 * @param loaderLocation	An optional jQuery selector of an element where a loader icon will be shown in
 */
function getContentModels(handleData, loaderLocation){
	// Display a loader icon if location is given
	if(loaderLocation !== undefined) {
		loaderLocation.append("<img src='img/ajax-loader-bar.gif' id='contentmodelLoader'>");
	}
	
	$.ajax({
		url: "Login.action?listContentModelsJson=",
		dataType: "json",
		success: function(data) {
			// Removes loader icon if its location is given
			if(loaderLocation !== undefined) {
				loaderLocation.find($("#contentmodelLoader")).remove();
			}
			// A Callback function to handle the returned data
			handleData(data);
		}
	});
}