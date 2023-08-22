var mainApp = angular.module('DEWAApp_Login', []);

mainApp.controller('DEWALoginController', function($rootScope, $scope, $http, $location) {

   
//    const errorContainer = document.getElementById('errormessage');
// 		// Add click event listener to the cancel button
// 		$('#cancelbtn').on('click', function() {
// 		  // Clear the text in the input field
// 		  $('#textInput').val('');  
// 		  errorContainer.style.display = 'none';
		  
// 		});


var input = document.getElementById("textInput");
input.addEventListener("keypress", function(event) {
  if (event.key === "Enter") {
    event.preventDefault();
    document.getElementById("finduser").click();
  }
});
$(".finalBtn").keyup(function(event) {
    if (event.keyCode === 13) {
        $("#finalbtn").click();
    }
});
$("#answer_input").keyup(function(event) {
    if (event.keyCode === 13) {
        $("#answerbtn").click();
    }
});

	$("#submitbtn").click(function() {
			$(".loader-containerr").removeClass("hidden");
			  var overlay = document.getElementById('overlay');
              overlay.style.display = 'block';
		});



		$scope.cancel = function () {
           //   $('#textInput').val('');  
			//  $scope.showMessage = false; 
             location.reload();
		}


	$scope.error = "";
	$scope.showMessage = false;
	$scope.findUserbtn = true;
	$scope.answerBtn = false;
	$scope.finalBtn = false;
	
	$scope.isPassword = "true";
	
	$scope.forgotPasswordPopup = function () {
		$scope.findUserbtn = true;
		$scope.answerBtn = false;
		$scope.finalBtn = false;
		$scope.showMessage = false;
		$('.popup-body-container#forgotpassword').removeClass("hide");
            setTimeout(function () {
                $('.popup-body-container#forgotpassword').addClass("popup-active");
            }, 100);
            $('#forgotpasswordPophead').text('Forgot Password');
	}

	$scope.findUser = function () {
		
		$http.get(appUrl + "/api/users/findUser/" + $scope.userName)
				.then(function (response) {
					$scope.user = response.data.response;
					if(response.data.statusCode===200){
					  $scope.secQuestion = $scope.user.securityQuestion;
					  $scope.findUserbtn = false;
					  $scope.answerBtn = true;
					  $scope.showMessage = false;
					  $('#questionField').removeClass("hide");
					  $('#answerField').removeClass("hide");
					} else if (response.data.statusCode===404){
						$('#passwordField').addClass("hide");
					    $('#confirmpasswordField').addClass("hide");
						$scope.showMessage = true;
						$scope.error = "No user found";
						
					}
				}, function error(response) {
					console.log('Error while getting user');
				});
		
	}
	
	$scope.checkAnswer = function (){
		if($scope.answer===$scope.user.answer){
			$scope.showMessage = false;
			$scope.answerBtn = false;
			$scope.finalBtn = true;
			$('#passwordField').removeClass("hide");
			$('#confirmpasswordField').removeClass("hide");
		} else {
			$scope.showMessage = true;
			$scope.error = "Incorrect Answer";
		}
		
	}
	
	$scope.editPassword = function() {
		
		angular.forEach($scope.userForm, function (control, name) {
				// Excludes internal angular properties
				if (typeof name === "string" && name.charAt(0) !== "$") {
					// To display ngMessages
					control.$setTouched();
					// Runs each of the registered validators
					control.$validate();
				}
			});
			
			if ($scope.userForm.$valid) 
		
			{
		$scope.user.roleId = $scope.user.role.id;
		$scope.user.password = $scope.password;
				$http.put(appUrl + "/api/users/update/" + $scope.user.id, $scope.user).then(
					function (response) {
						if (response.data.isSuccess === false) {
							$.growl.error({ message: "Error while updating password", delay: 1000 });
						} else {
							$("#forgotpassword").addClass("hide");
							$.growl.notice({ title: "Success!", message: "Password has been updated successfully!", delay: 1000 });
							setTimeout(function () {
								location.reload();
							}, 2000);	
						}
					},
					function error(response) {
						console.log("Error while updating password");
					}
				);
			}
	}
	
		$scope.checkPassword = function (x, y) {
			if (x === y) {
				$scope.isPassword = "true";
			} else {
				$scope.isPassword = "false";
			}
		};
	
});

mainApp.directive("compareTo", function () {
    return {
        require: "ngModel",
        scope: {
            otherModelValue: "=compareTo",
        },
        link: function (scope, element, attributes, ngModel) {
            ngModel.$validators.compareTo = function (modelValue) {
                return modelValue == scope.otherModelValue;
            };

            scope.$watch("otherModelValue", function () {
                ngModel.$validate();
            });
        },
    };
});


function loginSuccess() {
	debugger
	console.log('login success');
	// PREPARE FORM DATA
    	var formData = {
    		userName : $("#username").val(),
    		password :  $("#password").val()
    	}
	console.log('formdata : ' + JSON.stringify(formData,null,4));
	$("#loginError").addClass("hide");
	 $.ajax({
        type: "POST",
        contentType: "application/json",
        url: appUrl + "/api/login",      
        data: JSON.stringify(formData),
        dataType: 'json',
        success: function (data) {
			//hideLoader();
			console.log("SUCCESS : ", JSON.stringify(data,null,4));
			 if (data.statusCode != "BAD_REQUEST") {
			//	$.growl.notice({ message: 'User logged successfully!'});
			//	sessionStorage.setItem("username", data.username);
				sessionStorage.setItem("username", data.username);
				sessionStorage.setItem("firstName", data.firstName);
				sessionStorage.setItem("lastName", data.lastName);
				sessionStorage.setItem("email", data.email);
				sessionStorage.setItem("password", data.password);
				sessionStorage.setItem("securityQuestion", data.securityQuestion);
				sessionStorage.setItem("answer", data.answer);
				sessionStorage.setItem("status", data.status);
				sessionStorage.setItem("role", parseInt(data.role.id,10));
				sessionStorage.setItem("features", data.features);
			    sessionStorage.setItem("roleName", data.role.roleName);
			//	sessionStorage.setItem("cts_user_token", data.accessToken)
			//	sessionStorage.setItem("cts_user_language", "English");
			//	sessionStorage.setItem("cts_userrole", data.authorities[0].authority)
				sessionStorage.setItem('user_data', JSON.stringify(data));
				sessionStorage.setItem('userId', data.userId);
				//window.location.href =appUrl + "/home.html";
				$(".loader-containerr").addClass("hidden");
				window.location.href = appUrl + "/user-management.html";
				//window.location.href = "/home.html";
				//window.location.href = "D:/Satheesh/sts-workspace-all/CTS_reporting_portal/ctsReport/home.html";
				//window.location.href = "E:/STS_workspce/ctsReport/home.html";
			//	userAccess(data.userId);		
			}else{
				$(".loader-containerr").addClass("hidden");
			    overlay.style.display = 'none';
				$("#loginError").removeClass("hide");
			
				// location.reload(true);
			}
        },
        error: function (e) {
		//	hideLoader()
	       	$(".loader-containerr").addClass("hidden");
	       	overlay.style.display = 'none';
			$("#loginError").removeClass("hide");
            console.log("ERROR : ", e);
        }
    });
	// script.js
	// function showLoader() {
	// 	$('#loader').show(); // Show the loader
	//   }
	
	//   function hideLoader() {
	// 	$('#loader').hide(); // Hide the loader
	//   }


}
