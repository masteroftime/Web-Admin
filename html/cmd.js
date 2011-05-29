
function reload(data)
{
	var item = $(data);
	item.appendTo('#output');
	item.slideDown('fast', function() {
		$("#output").attr({ scrollTop: $("#output").attr("scrollHeight") });
	});
	
	$.get("/cmdline?random="+Math.random(), function(data) {
		reload(data);
	});
}

$('document').ready(function() {
	
	$('#form').submit(function (event) {
		event.preventDefault();
		$.post("/command", "command="+$('#input').val());
		$('#input').val("");
	});
	
	$('#clear').click(function (event) {
		event.preventDefault();
		$('#output').empty();
	});
	
	reload("<div class='out'>Started MC Web Console v0.1</div>");
});