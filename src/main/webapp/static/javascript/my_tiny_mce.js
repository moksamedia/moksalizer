"use strict";

tinyMCE.init({
    theme : "advanced",
    mode : "exact",
    elements: "main-text-editor",
    plugins : "nonbreaking,autoresize",
    nonbreaking_force_tab : true,
    theme_advanced_toolbar_location : "top",
    theme_advanced_buttons1 : "draftbutton,previewbutton,publishbutton,separator,fontselect,fontsizeselect,mybutton,bold,italic,underline,separator,strikethrough,justifyleft,justifycenter,justifyright,justifyfull,bullist,numlist,undo,redo,link,unlink,separator,code,detach",
    theme_advanced_buttons2 : "",
    theme_advanced_buttons3 : "",
    theme_advanced_statusbar_location : "none",
    auto_focus: "main-text-editor",
    init_instance_callback: "fixTinyMCETabIssue",
    theme_advanced_font_sizes: "10px,12px,13px,14px,16px,18px,20px",
    font_size_style_values : "10px,12px,13px,14px,16px,18px,20px",
    content_css: "/tiny_mce/content.css",
    editor_css: "/tiny_mce/ui.css",
    skin: "thebigreason",
    add_form_submit_trigger:false,
    oninit: detachToolbarInitial,
    setup : function(ed) {
        // Add a custom button
        ed.addButton('detach', {
            title : 'Detach/Attach',
            class: "mce_image",
            onclick : toggleAttached
        });
        // Add a Draft button
        ed.addButton('draftbutton', {
        	title : 'Save draft',
        	image : '/tiny_mce/img/draft_icon.png',
        	onclick : function() {
        		$(".thebigreasonSkin iframe").contents().find("body.mceContentBody").css("margin", "100px");
        	}
        });
        // Add a save button
        ed.addButton('publishbutton', {
        	title : 'Save & publish post',
        	image : '/tiny_mce/img/publish_icon.png',
        	onclick : function() {
        		savePost();
        	}
        });
        // Add a save button
        ed.addButton('previewbutton', {
        	title : 'Preview post',
        	image : '/tiny_mce/img/preview_icon.png',
        	onclick : function() {
        		alert("Implement me!");
        	}
        });
    }
 });

function pushMessage(message, delay) {
	var container = $("div#coreContent");
	$("div.message").remove();
	message.prependTo(container).hide().fadeIn('fast').delay(delay).fadeOut('slow');
}

// suppress repeated save events while save is in progress
var suppressSave = false;
function disableSaveButton() { suppressSave = true; }
function enableSaveButton() { suppressSave = false; }

/*
 * Submits a post
 */
function savePost() {

	if (suppressSave) return;
	
	var content = tinyMCE.get('main-text-editor').getContent();
	var title = $("form#new-post-meta input#title").val();
	var id = null, seqNumber = null
	
	// is this a new post or an existing post?
	// - the post-data-passer is populated with id and seqnum if it's an existing post
	if ($("div#post-data-passer").length != 0) {
		id = $("div#post-data-passer").data('id');
		seqNumber = $("div#post-data-passer").data('seqnumber');
	}
	
	$.ajax({
		  url: "/savepost",
		  data: {'content':content, 'title':title, 'id':id, 'seqnumber':seqNumber},
		  type: 'POST',
		  dataType:'json',
		  // before
		  beforeSend: function(jqXHR, settings) {
			  disableSaveButton();
			  // no content
			  if (content.trim().length == 0) {
				  pushMessage($("<div class='message error'>You're trying to save a blank post.</div>"),3000);
				  enableSaveButton();
				  return false;
			  }
			  // title not set
			  if (title == "Type title here...") {
				  pushMessage($("<div class='message error'>Please change or delete the title.</div>"),3000);
				  enableSaveButton();
				  return false;
			  }
			  
			  if ($("div.inprogress").length == 0) {
				  pushMessage($("<div class='message inprogress' id='save-in-progress'>Saving " + title +"</div>"),3000);
			  } 
			  return true;
		  },
		  error: function(jqXHR, textStatus, errorThrown) {
				  pushMessage($("<div class='message error'>Oops. There was an error saving: " + textStatus + " / " + errorThrown +"</div>"),3000);
				  enableSaveButton();
		  },
		  success: function(data, textStatus, jqXHR) {
				  pushMessage($("<div class='message'>Successfully saved.</div>"),3000);
				  $("<div id='post-data-passer' style='display:none;' data-id='" + data.id + "' data-seqnumber='" + data.seqnumber +"'></div>").appendTo("#coreContent");
				  enableSaveButton();
		  }
		});
}


var bannerAndFooterVisible = true;
var mceLayoutBorderFader,mceLayoutFirstBorderFader,mceLayoutLastBorderFader,mceLayoutTitleBorderFader;

/*
 * Fades an element's borders in and out, remembering the color and width
 * for each side individually.
 */
function BorderFader(elem) {
   
   var borderTopColor = elem.css("borderTopColor");
   var borderBottomColor = elem.css("borderBottomColor");
   var borderLeftColor = elem.css("borderLeftColor");
   var borderRightColor = elem.css("borderRightColor");

   var borderTopWidth = elem.css("borderTopWidth");
   var borderBottomWidth = elem.css("borderBottomWidth");
   var borderLeftWidth = elem.css("borderLeftWidth");
   var borderRightWidth = elem.css("borderRightWidth");
   
   function fadeOut(speed) {
      elem.stop().animate({
         'borderTopColor': "#FFF",
         'borderBottomColor': "#FFF",
         'borderLeftColor': "#FFF",
         'borderRightColor': "#FFF"
      }, speed);
      elem.css({
         'borderTopWidth': borderTopWidth,
         'borderBottomWidth': borderBottomWidth,
         'borderLeftWidth': borderLeftWidth,
         'borderRightWidth': borderRightWidth         
      });
   }
   function fadeIn(speed) {
      elem.stop().animate({ 
         'borderTopColor': borderTopColor,
         'borderBottomColor': borderBottomColor,
         'borderLeftColor': borderLeftColor,
         'borderRightColor': borderRightColor
      }, speed);
      elem.css({
         'borderTopWidth': borderTopWidth,
         'borderBottomWidth': borderBottomWidth,
         'borderLeftWidth': borderLeftWidth,
         'borderRightWidth': borderRightWidth         
      });
   }
   
   this.fadeOut = fadeOut;
   this.fadeIn = fadeIn;

}

var toolbar, boxShadowOriginal;
var bordersVisible = true;
var toolbarAttached = true;

function toggleAttached() {
	if (toolbarAttached) {
		detachToolbar();
	}
	else {
		attachToolbar();
	}
}

function detachToolbarInitial() {
	toolbarAttached = false;
    $("body").prepend('<div id="top-admin-bar"><table class="thebigreasonSkin"></table></div>');
    $("div#top-admin-bar table").prepend($("tr.mceFirst"));
    $("tr.mceFirst").stop().fadeTo("slow",1.0);
    $("td.mceToolbar").off("mouseenter mouseleave");
    $("div#top-admin-bar * td.mceToolbar").css("opacity", "1.0");
}

function detachToolbar() {
	toolbarAttached = false;
    $("body").prepend('<div id="top-admin-bar"><table class="thebigreasonSkin"></table></div>');
    $("div#top-admin-bar table").prepend($("tr.mceFirst"));
    $("tr.mceFirst").stop().fadeTo("slow",1.0);
    $("td.mceToolbar").off("mouseenter mouseleave");
}

/*
 * Attaches the toolbar to the mce table
 */
function attachToolbar() {
	toolbarAttached = true;
	
	// fade out the admin bar
	$("div#top-admin-bar").stop().fadeOut('fast');
	
	// fade out the mce toolbar (in the admin bar)
	$("tr.mceFirst").stop().fadeOut('fast', function() {
		// when done, add toolbar to mce document table
		$("tr.mceFirst").prependTo("table.mceLayout tbody");
		// show the attached toolbar
		$("tr.mceFirst").stop().fadeIn('fast');
		// get rid of the admin bar
		$("div#top-admin-bar").remove();
	});
	
	// this may have been hidden
	$("td.mceToolbar").css("opacity","0.9");
	
	// after delay, fade out toolbar (and set mouseleave handler when
	// done to avoid intial mouseleave event)
	$("td.mceToolbar").stop().delay(1000).fadeTo('slow',0.0, function() {
		$("td.mceToolbar").mouseleave( function() {
			$("td.mceToolbar").stop().fadeTo('slow',0.0);
		});
	});
	
	// mouseenter to show toolbar
	$("td.mceToolbar").mouseenter( function() {
		$("td.mceToolbar").stop().fadeTo('fast',0.9);
	});

	

}

/*
 * Toggles between 3 different minimal states: everything, borders gone,
 * page-like view.
 */
function showHideToolbar() {

	// hides the borders
   if (bordersVisible) {
	   bordersVisible = false;
	   // fade the borders out
	   mceLayoutBorderFader.fadeOut('slow');
	   mceLayoutTitleBorderFader.fadeOut('slow');
   }
   // page view
   else if (bannerAndFooterVisible) {
      bannerAndFooterVisible = false;
      // fade the banner out
      $("#banner").stop().fadeTo("slow", 0.0);
      // fade the title info / meta out
      $("form#new-post-meta input#title").stop().fadeTo("slow",0.0);
      // fade out the footer
      $("#footer").stop().fadeTo("slow", 0.0, function() {
    	  	// bring up the grey background
           $("body").stop().animate({backgroundColor : "#DDD"}, "slow");
     	   $("div#siteWrapper").stop().animate({backgroundColor:"#DDD"}, "slow");
     	   // get rid of the box shadow
     	   $("div#siteWrapper").css("box-shadow", "none");
            // add the border
            $("table.mceLayout").css("border", "1px black solid");
      });
      
   }
   // full view
   else {
	   bordersVisible = true;
	   bannerAndFooterVisible = true;
	   // fade in title and title border
	   mceLayoutTitleBorderFader.fadeIn('fast');
	   $("form#new-post-meta input#title").fadeTo("fast",100.0);
	   // fade away grey area around document
	   $("div#siteWrapper").stop().animate({backgroundColor:"#FFF"}, "fast");
	   $("body").stop().animate({backgroundColor:"#FFF"}, "fast", function() {
		   // fade in original border
		   mceLayoutBorderFader.fadeIn('fast');
		   // fade in banner and footer
		   $("#banner").stop().fadeTo("fast", 100.0);
		   $("#footer").stop().fadeTo("fast", 100.0);

	   });
	   $("div#siteWrapper").css("box-shadow", boxShadowOriginal);
   }
}

function initJavascript() {

	// init the border faders
	mceLayoutBorderFader = new BorderFader($("table.mceLayout"));
	mceLayoutTitleBorderFader = new BorderFader($("form#new-post-meta input#title"));

	// do we have some post data passed in from the server?
	if ($("div#post-data-passer").length != 0) {
		tinyMCE.activeEditor.setContent($("div#post-data-passer").html());
	}
	// save the original box shadow values (so we can bring it back later)
	boxShadowOriginal = $("div#siteWrapper").css("box-shadow");

}

function fixTinyMCETabIssue(inst) {
   
    inst.onKeyDown.add(function(inst, e) {
        // Firefox uses the e.which event for keypress
        // While IE and others use e.keyCode, so we look for both
       var code;
       
        if (e.keyCode) {
           code = e.keyCode;
        }
        else if (e.which) {
           code = e.which;
        }
        /*
        if(code == 9 && !e.altKey && !e.ctrlKey) {
            // toggle between Indent and Outdent command, depending on if SHIFT is pressed
            if (e.shiftKey) {
               inst.execCommand('Outdent');
            }
            else {
               inst.execCommand('Indent');
            }
            
            // prevent tab key from leaving editor in some browsers
            if(e.preventDefault) {
                e.preventDefault();
            }
            
            return false; //e.preventDefault() and e.stopPropagation()
        }
        else 
        */
        if (code == 27) { // 27 == ESCAPE KEY
           showHideToolbar();
           e.stopPropagation();
           e.preventDefault();
           return false;
        }
    });
}

$(window).load(initJavascript);

