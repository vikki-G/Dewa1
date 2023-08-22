//var appUrl ='http://localhost:8086/dewaReport';
//var appUrl = 'http://localhost:8089';
//var appUrl ='http://172.16.11.157:8088/UccxReports';
var appUrl ='http://localhost:8088';

var zone = 'America/Mexico_City';

//$("#custom-search").click(function(){
    //  $(".loader-containerr").removeClass("hidden");
   //   var overlay = document.getElementById('overlay');
   //   overlay.style.display = 'block';
 //});
          
		
//function showLoader() {
  //  var overlay = document.getElementById('overlay');
   // overlay.style.display = 'block';
//}

// JavaScript to hide loader and overlay
function hideLoader() {
   // var overlay = document.getElementById('overlay');
   // overlay.style.display = 'none';
    var loaderContainer = document.querySelector(".loader-containerr");
	var overlay = document.getElementById('overlay');
    overlay.style.display = 'none';
    loaderContainer.style.display = 'none';
}

//  document.addEventListener("DOMContentLoaded", function () {
//            var loaderContainer = document.querySelector(".loader-containerr");
 //           loaderContainer.style.display = 'none';
 //       });


//$(window).on("load", function() {
//	$(".loader").delay(500).fadeOut("slow");
//});
function logout() {

	var formData = {
		userName: sessionStorage.getItem("username")
	}
	var token = sessionStorage.getItem('user_token');
	$.ajax({
		type: "POST",
		contentType: "application/json",
		headers: { "Authorization": 'Bearer ' + token },
		url: appUrl + "/api/logout",
		data: JSON.stringify(formData),
		dataType: 'json',
		success: function(data) {
			if (data.statusCode != "BAD_REQUEST") {
				$.growl.notice({ message: 'User logged out successfully!' });
				window.sessionStorage.removeItem("username");
				window.sessionStorage.removeItem("user_token");
				window.sessionStorage.removeItem("userrole");
				window.sessionStorage.removeItem("user_data");
				window.location.href = appUrl + "/login.html";
			} else {
				// $("#loginError").removeClass("hide");
				location.reload(true);
			}
		},
		error: function(e) {
			console.log("ERROR : ", e);
		}
	});

}


const inactivityTimeout = 600000;
let logoutTimer;


function resetLogoutTimer() {
	clearTimeout(logoutTimer);
	logoutTimer = setTimeout(logout, inactivityTimeout);
}

document.addEventListener("mousemove", resetLogoutTimer);
document.addEventListener("keypress", resetLogoutTimer);



function userProfPassword() {
	$('#userProfilePassword').keyup(function() {
		var password = $(this).val();
		var passwordMessage = $('#passwordMessage');
		var isValid = true;
		var pattern = /^(?=.*[A-Z])(?=.*\d).{8,}$/;

		if (!pattern.test(password)) {
			passwordMessage.html('Password should be at least 8 characters long and contain at least one uppercase letter and one number.');
			$('#ButtonId').prop('disabled', true);
		} else {
			passwordMessage.html('');
			$('#ButtonId').prop('disabled', false);
		}

		return isValid;
	});

}
$(document).ready(function() {
	$('#userProfilePassword, #userConfirmPassword').keyup(handleKeyUp);


	function handleKeyUp() {
		var password = $('#userProfilePassword').val();
		var confirmPassword = $('#userConfirmPassword').val();

		if (password === confirmPassword && confirmPassword === password) {
			$('#ButtonId').prop('disabled', false);
			$('#message').text('');
		} else {
			$('#ButtonId').prop('disabled', true);
			$('#message').text('Password and confirm password does not match');
		}
	}
});

//function userConfPassword(){

//	$('#userConfirmPassword').keyup(function() {
var password = $('#userProfilePassword').val();
var confirmPassword = $(this).val();
if (password !== confirmPassword && confirmPassword !== password) {
	$('#ButtonId').prop('disabled', true);
	$('#message').html('Password and confirm password does not match');
} else {
	$('#ButtonId').prop('disabled', false);
	$('#message').html('');
}
//		});

//}

function updateUserProfile() {


	if ($("#userProfileSecurityQuestion").val() == null || $("#userProfileAnswer").val() == null || $("#userProfileAnswer").val() == 'undefined') {
		return;
	}

	var formData = {
		id: sessionStorage.getItem("userId"),
		userName: sessionStorage.getItem("username"),
		status: $("#userProfileStatus").val(),
		updatedBy: sessionStorage.getItem("username"),
		password: $("#userProfilePassword").val(),
		firstName: $("#userProfileFirstName").val(),
		lastName: $("#userProfileLastName").val(),
		email: $("#userProfileEmail").val(),
		securityQuestion: $("#userProfileSecurityQuestion").val(),
		answer: $("#userProfileAnswer").val(),
		roleId: sessionStorage.getItem("role")
	}
	var token = sessionStorage.getItem('user_token');
	$.ajax({
		type: "PUT",
		contentType: "application/json",
		url: appUrl + "/api/users/update/" + sessionStorage.getItem("userId"),
		data: JSON.stringify(formData),
		dataType: 'json',
		success: function(data) {
			if (data.statusCode != "BAD_REQUEST") {
				$.growl.notice({ message: 'User Profile has been updated successfully!', delay: 1000 });
				$(".popup-body-container#userProfilePop").addClass("hide");
				sessionStorage.setItem("firstName", $("#userProfileFirstName").val());
				sessionStorage.setItem("lastName", $("#userProfileLastName").val());
				sessionStorage.setItem("email", $("#userProfileEmail").val());
				sessionStorage.setItem("password", $("#userProfilePassword").val());
				sessionStorage.setItem("securityQuestion", $("#userProfileSecurityQuestion").val());
				sessionStorage.setItem("answer", $("#userProfileAnswer").val());
				location.reload();
				// setTimeout(function () {
				// logout();
				// }, 2000);	
			} else {
				//location.reload(true);
				location.reload();
			}
		},
		error: function(e) {
			console.log("ERROR : ", e);
		}
	});

}