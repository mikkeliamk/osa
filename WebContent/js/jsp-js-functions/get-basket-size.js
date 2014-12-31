function updateBasketSize() {
	$.ajax({
		url: "Basket.action?getBasketSize=",
		success: function(size) {
			$(".basketsize").html(size);
		}
	});
}
$(function(){
	updateBasketSize();
});