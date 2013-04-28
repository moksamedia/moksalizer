$(document).ready( function() {
	$("#import").submit( function() {
		$("div#wpimport-start").fadeOut("slow", function () {
			$("div#wpimport-inprocess").fadeIn("fast");	
		});
		fetchPosts();
		return false;
	});

});

//http://www.thepathis.com/?json=get_recent_posts?count=500
function fetchPosts() {
	var requestUrl = $("input#wpSiteUrl").val();
	var pagesToImport = $("input#wpPagesToImport").val()
	$("div#wpimport-inprocess").append("<div>Requesting from: " + requestUrl + "</div>");
	
	$.ajax({
		  type: 'GET',
		  url: "/importgetjson",
		  data: { url:requestUrl, pagesToImport:pagesToImport },
		  success:function(data) {
			  payloadReceived(data);
		  },
		  error:function() {
			  showError();
		  }
		});

}

function showError() {
	$("div#wpimport-inprocess").fadeOut("slow", function() {
		$("div#wpimport-error").fadeIn("fast");
	});
}

function payloadReceived(data) {
	$("div#wpimport-inprocess").fadeOut("slow", function() {
		$("div#wpimport-received").fadeIn("fast", function() {
			$("div#wpimport-received").append("<div>Received " + data.count + " of " + data.count_total + " posts.</div>");
			$("div#coreContent").append("<pre style='margin-top:50px;text-align:left;overflow:auto;'>" + JSON.stringify(data, null, 4) + "</pre>");
		});
	});
}