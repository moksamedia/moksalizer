function loadLogin() {
	"use strict";

	// hides the login dialog and mask
	function hideLoginDialog() {
        $('#mask , .login-popup').fadeOut(300 , function() {
			$('#mask').remove();  
		});
	}
	
	// shows the login dialog and mask
	function showLoginDialog() {
		
        //Getting the variable's value from a link 
		var loginBox = $('a.login-window').attr('href');

		//Fade in the Popup
		$(loginBox).fadeIn(300);
		
		//Set the center alignment padding + border see css style
		var popMargTop = ($(loginBox).height() + 24) / 2; 
		var popMargLeft = ($(loginBox).width() + 24) / 2; 
		
		$(loginBox).css({ 
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});
		
		// Add the mask to body
		$('body').append('<div id="mask"></div>');
		$('#mask').fadeIn(300);
		
        $("form.signin label.username span").css("color", "#fff");
        $("form.signin label.password span").css("color", "#fff");	          

		
		return false;
	}
			
	// called when LOGOUT is clicked
	function logoutFunction() {
	    $.ajax({
		      type: "GET",
		      url: 'logout',
		      data: $('form').serialize(),
		      dataType:'json',
		      success: function(data) {
		    	  pushLoginToNav();
		      },
		      statusCode: {
		    	  200: function(e) {
		    		  pushLoginToNav();
		    	  }
		      }
		    });
	    
	    return false;
	}
	
	// swaps the logout link for the login link
	function pushLoginToNav() {
        $("ul.main-nav-left li#login a").remove();
        var login = $('<a href="#login-box" class="login-window">Login</a>');
        login.click(showLoginDialog);
        $("ul.main-nav-left li#login").append(login); 
	}
	
	// replaces the login link with the logout link
	function pushLogoutToNav() {
        $("ul.main-nav-left li#login a").remove();
        var logout = $('<a href="#logout" class="logout">Logout</a>');
        logout.click(logoutFunction);
        $("ul.main-nav-left li#login").append(logout); 
	}
	
	// When clicking on the button close or the mask layer the popup closed
	$('a.close, #mask').live('click', function() { 
		$('#mask , .login-popup').fadeOut(300 , function() {
			$('#mask').remove();  
		}); 
		return false;
	});
	
	// called when LOGIN button is clicked
	$("form button.login_button").click(function() {
		
		var url = getPassedToPage('blogHomeUrlSsl') + '/login'
				
	    $.ajax({
	      type: "POST",
	      //url: 'https://' + window.location.host + '/login',
	      url: url,
	      data: $('form').serialize(),
	      dataType:'json',
	      success: function(data) {
	    	  hideLoginDialog();
	    	  pushLogoutToNav();
	      },
	      statusCode: {
	        403: function(e) {
	          $("form.signin label.username span").css("color", "red");
	          $("form.signin label.password span").css("color", "red");	          
	        }
	      }
	    });
	    return false;
	  });

	// sets the initial logout click action
	$('a.logout').click(logoutFunction);

	// sets the initial login link click action
	$('a.login-window').click(showLoginDialog);

}
$(document).ready(function() {
	//loadLogin()
});