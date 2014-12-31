function getLocalizedMsg(msgBundleCode, handleData){
	$.ajax({
		url: "General.action?getLocalizedMessage=",
		data: {msgBundleCode: msgBundleCode},
		success: function(data) {
			// A Callback function to handle the returned data
			handleData(data);
		}
	});
}