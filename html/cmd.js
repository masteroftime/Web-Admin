
function reload(data)
{
	$('body').append(data);
	$.get("/cmdline", function(data) {
		reload(data);
	});
}

$('document').ready(function() {
	reload("<div class='out'>MC Web Console v0.0</div><br>");
});