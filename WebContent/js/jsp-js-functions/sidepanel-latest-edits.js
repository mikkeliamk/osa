// Variable to indicate wether the user has closed the panel by themself 
var userClosed = false;
var show_more_text = "";
var header_text	= "";
$(function(){
	header_text = document.createTextNode(messagebundle.header["latestedits"]);
	show_more_text = document.createTextNode("\u25B6 "+messagebundle.button["showmore"]);
	// Gets latest edited objects from database and constructs a sidepanel containing the objects
	$.ajax({
		url: "User.action?getLatestEdited=",
		dataType: "json",
		success: function(data){
			// Creates elements
			var link 				= "";
			var sidepanel_item 		= "";
			var sidepanel_container = document.createElement("div");
			var sidepanel 			= document.createElement("div");
			var header				= document.createElement("div");
			var show_more			= document.createElement("div");
			var toggle_button 		= document.createElement("label");
			var arrow 				= (sessionStorage.getItem("userClosedPanel") === "false") ? document.createTextNode('\u00BB') : document.createTextNode('\u00AB');

			// Add ids, classes and contents to elements
			sidepanel_container.id 	= "sidepanel-container";
			sidepanel.id 			= "sidepanel";
			header.className		= "sidepanel-header";
			show_more.className		= "sidepanel-showmore";
			toggle_button.className = "pointerCursor";
			
			toggle_button.appendChild(arrow);
			show_more.appendChild(show_more_text);
			header.appendChild(header_text);
			sidepanel.appendChild(header);

			var dLength = data.length;
			for (var i = 0; i < dLength; i++) {
				var items = data[i]["pids"];
				var iLength = items.length;
				// Sort items by editDate property so newest one is first in the list
				items.sort(function(a,b) {return (a.editDate > b.editDate) ? -1 : ((b.editDate > a.editDate) ? 1 : 0);});
				for (var j = 0; j < iLength; j++) {
					link 			= document.createElement("a");
					sidepanel_item 	= document.createElement("div");
					var date 		= new Date();
					date.setTime(items[j]["editDate"]);
					var min 	= (date.getMinutes() < 10 ? '0' :'') + date.getMinutes(),
						hour 	= date.getHours(), 
						day 	= date.getDate(),
						month 	= (date.getMonth()+1),
						year 	= date.getFullYear().toString().substr(2,2);
					
					var divContentName 			= document.createElement("span");
					divContentName.innerHTML 	= (items[j]["id"] ? items[j]["id"] : "")+" "+items[j]["name"];
					divContentName.className	= "latestedit-name";

					var typeicon 				= document.createElement("img");
					typeicon.src 				= "img/icons/silk/"+types[items[j]["pid"].split(":")[1].split("-")[0]];
					typeicon.className			= "smallicon";
					
					var divContentDate			= document.createElement("span");
					divContentDate.innerHTML 	= day+"."+month+"."+year+", "+hour+":"+min;
					divContentDate.className	= "latestedit-date";
						
					sidepanel_item.title		= "["+items[j]["eventLocal"]+"] " + items[j]["name"];
					sidepanel_item.className 	= "sidepanel-item latestedit-"+items[j]["event"];
					link.href 					= 'Ingest.action?pid='+items[j]["pid"]+'&view=';
					
					sidepanel_item.appendChild(typeicon);
					sidepanel_item.appendChild(divContentName);
					sidepanel_item.appendChild(document.createElement("br"));
					sidepanel_item.appendChild(divContentDate);
					link.appendChild(sidepanel_item);
					sidepanel.appendChild(link);
				}
			}
			// Append created elements to the panel container
			sidepanel_container.appendChild(sidepanel);
			sidepanel_container.appendChild(show_more);
			sidepanel_container.appendChild(toggle_button);
			// Append panel container to screen
			document.getElementById("wrapper").appendChild(sidepanel_container);
			
			// If user has closed the panel manually, retain the state through page loads by using sessionStorage
			if (sessionStorage.getItem("userClosedPanel") == "true") {
				sidepanel_container.className 	+= " sidepanel-container-closed";
				sidepanel.className 			+= " sidepanel-closed";
				show_more.className 			+= " sidepanel-closed";
			}
		}
	});

	$(window).resize(function(){
		if ($(this).width() <= 1300 && sessionStorage.getItem("userClosedPanel") == "false" && !$("#sidepanel-container").hasClass("sidepanel-container-full")) {
			$("#sidepanel-container").addClass("sidepanel-container-closed");
			$("#sidepanel, .sidepanel-showmore").addClass("sidepanel-closed");
			$("#sidepanel-container label").html('\u00AB');
		} 
		if ($(this).width() > 1300 && sessionStorage.getItem("userClosedPanel") == "false") {
			$("#sidepanel-container").removeClass("sidepanel-container-closed");
			$("#sidepanel, .sidepanel-showmore").removeClass("sidepanel-closed");
			$("#sidepanel-container label").html('\u00BB');
		}
	});
	
	$("#wrapper").on("click", "#sidepanel-container label", function(){
		if($("#sidepanel").is(":visible")) {
			$("#sidepanel-container").addClass("sidepanel-container-closed");
			$("#sidepanel, .sidepanel-showmore").addClass("sidepanel-closed");
			$(this).html('\u00AB');
			sessionStorage.setItem("userClosedPanel", "true");
		} else {
			$("#sidepanel-container").removeClass("sidepanel-container-closed");
			$("#sidepanel, .sidepanel-showmore").removeClass("sidepanel-closed");
			$(this).html('\u00BB');
			sessionStorage.setItem("userClosedPanel", "false");
		}
	});
	
	$("#wrapper").on("click", ".sidepanel-showmore", function(){
		modalWindow(true);
		$("#sidepanel-container").toggleClass("sidepanel-container-full");
	});
});