$(function(){
	$(".datatable-tools .select").click(function(){
		var $this 	= $(this);
		var $tab 	= $this.parent().parent();
		var check 	= false;
		
		if ($this.is(':checked')) {
		    check = true;
		}
		// Only mark checkboxes on the same tab
		$tab.find('.dataTable :checkbox').prop('checked', check);
		// Trigger stuff bind to change-event
		$tab.find('.dataTable :checkbox').trigger("change");
	});
	$(document).on("click", ".dataTable :checkbox, .datatable-tools .select", function(){
		var $tab = $(this).closest(".tab_content");
		
		if ($tab.find(".dataTable input:checkbox:checked").length == 0) {
			$tab.find(".datatable-tools button, .datatable-tools input[type=submit]").prop("disabled", true);
		} else {
			$tab.find(".datatable-tools button, .datatable-tools input[type=submit]").prop("disabled", false);
		}
	});
});