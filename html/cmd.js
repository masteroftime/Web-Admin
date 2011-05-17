
function reload(data)
{
	var item = $(data);
	item.appendTo('#main');
	item.slideDown('fast');
	
	$.get("/cmdline", function(data) {
		reload(data);
	});
}

$('document').ready(function() {
	
	$('#form').submit(function (event) {
		event.preventDefault();
		
		var item = $("<div class='out'> >"+$('#input').val()+"</div>");
		item.appendTo('#main');
		item.slideDown('fast');
		$.post("/command", "command="+$('#input').val());
		
		$('#input').val("");
	});
	
	reload("<div class='out'>MC Web Console v0.0</div>");
});