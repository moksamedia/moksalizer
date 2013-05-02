
function didHitBottom() {
	if  ($(window).scrollTop() == $(document).height() - $(window).height()) {
		loadMorePosts();
	}
}

$(window).scroll(didHitBottom);

var batchNum = 1;
var batchSize = 10;
	
function loadMorePosts() {
	
	/*
	 * ajaxloadtype = post | tag | category, and is used to differentiate between requests for more posts
	 * from the front page, from a page showing all posts for a tag, or from a page showing all posts for
	 * a category. It's set by adding the key and value to the passToPage property of the rendering context,
	 * and is then turned into a hidden form element.
	 */
	ajaxloadtype = getPassedToPage('ajaxloadtype')
	
	$("div.loading-msg").appendTo("div.post-list").delay(500).fadeIn('slow');
		
	$.get('/ajax/posts', {'batchnum':batchNum, 'batchsize':batchSize}, function(data) {
		
		html = $(data).html(); // this extracts the contents of the top-level div.post-list for us
		
		if ($.trim(html) == "") {
			$(window).unbind('scroll', didHitBottom);
			if ($("div.no-more-posts").length == 0) {
				$("div.post-divider:last").after("<div class='no-more-posts'><h3 class='post-title'>No more posts. Sorry. Email me and tell me how awesome I am and maybe I'll write some more.</h3></div>");
				$("div.no-more-posts").fadeTo('slow', 0.7);
				$("div.loading-msg").stop().hide();
			}
		}
		else {
			$("div.post-list").append(html);		
			batchNum += 1;
			$("div.loading-msg").stop().hide();
		}
	}).complete( function () {
		$("div.loading-msg").stop().hide();
	});
}