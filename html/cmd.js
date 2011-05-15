
function reload(data)
{
	$('<div class="out">'+data+"</div>").appendTo("body");
	$.get("line.php", function(data) {
		reload(data);
	});
}

$('document').ready(function() {
	reload("MC Web Console v0.0");
});