// Victoria Bill
// EE 382V APT
// Lab 2 jQuery

$(function() {
	var auto_names = ["Andy","Andrew","Bob","Bobby","Chuck","Charles","David"];
	$("#username").autocomplete({source: auto_names });
});