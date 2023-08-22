var mainApp = angular.module("DEWAReportingPortal_User", []);

mainApp.controller(
	"UserController",
	function($scope, $compile, $http, $filter) {

		// const element = document.getElementById("useraccess");
		// element.remove();
	


		$scope.logged_in_user = sessionStorage.getItem("username");
		$scope.user_role = sessionStorage.getItem("userrole");
		$scope.user_Id = sessionStorage.getItem("userId");

		$scope.confirmPassword = "";

		if ($scope.logged_in_user == null || $scope.logged_in_user == "null") {
			window.location.href = appUrl + "/login.html";
		}
		$scope.userProfile = {};
		$scope.userProfile.userName = sessionStorage.getItem("username");
		$scope.userProfile.firstName = sessionStorage.getItem("firstName");
		$scope.userProfile.lastName = sessionStorage.getItem("lastName");
		$scope.userProfile.email = sessionStorage.getItem("email");
		$scope.userProfile.password = sessionStorage.getItem("password");
		$scope.userProfileConfirmPassword = sessionStorage.getItem("password");
		$scope.userProfile.securityQuestion = sessionStorage.getItem("securityQuestion");
		$scope.userProfile.answer = sessionStorage.getItem("answer");
		$scope.userProfile.status = sessionStorage.getItem("status");
		$scope.userProfile.roleId = sessionStorage.getItem("role");
		$scope.userProfile.roleName = sessionStorage.getItem("roleName");
		$scope.userProfile.features = sessionStorage.getItem("features");

		if ($scope.userProfile.answer == 'undefined') {
			$scope.userProfile.answer = '';
		}

		getUsers();
		function getUsers() {
			$http.get(appUrl + "/api/users/list").then(
				function(response) {
					$scope.users = response.data.response;
			    //	$scope.users = response.data;
					$(document).ready(function() {
						setTimeout(function() {
							$('#userManagementTable').DataTable({
								destroy: true,
								iDisplayLength: 25,
								order: [],
								columnDefs: [{ orderable: false, targets: [8] }]
							});
						}, 300);

					});
					hideLoader()
					      // var loaderContainer = document.querySelector(".loader-containerr");
					      // var overlay = document.getElementById('overlay');
                          // overlay.style.display = 'block';
                          // loaderContainer.style.display = 'none';
				},
				function error(response) {
					console.log("Error while retriving users");
					hideLoader()
				}
			);
		}
		//add user

		$scope.addPopup = function() {
			$scope.confirmPassword = "";
			$(".popup-body-container#addeditUserPop").removeClass("hide");
			$(".popup-body-container#addeditUserPop .addeditUserSubmit-btn").removeClass("hide");
			$(".popup-body-container#addeditUserPop .addeditUserUpdate-btn").addClass("hide");

			setTimeout(function() {
				$(".popup-body-container").addClass("popup-active");
			}, 100);
			$(".popup-body-container#addeditUserPop .popup-header")
				.removeClass("edit-popup-header")
				.addClass("add-popup-header");
			$("#addeditUserPophead").text("Add User");
			$scope.user = {};
			$scope.userForm.$setPristine();
			$scope.userForm.$setUntouched();
		};

		$scope.submit = function() {
			angular.forEach($scope.userForm, function(control, name) {
				// Excludes internal angular properties
				if (typeof name === "string" && name.charAt(0) !== "$") {
					// To display ngMessages
					control.$setTouched();
					// Runs each of the registered validators
					control.$validate();
				}
			});
			if ($scope.userForm.$valid) {
				$scope.user.createdBy = $scope.logged_in_user;
				$http.post(appUrl + "/api/users/save", $scope.user).then(
					function(response) {
						//	$scope.users = response.data.response;
						if (response.data.isSuccess === false) {
							$(".popup-body-container#addeditUserPop").addClass("hide").removeClass("popup-active");
							$.growl.error({ message: response.data.message, delay: 1000 });
						} else {
							$(".popup-body-container#addeditUserPop").addClass("hide").removeClass("popup-active");
							$.growl.notice({ title: "Success!", message: "User has been created successfully!", delay: 1000 });
							location.reload();
						}

					},

					function error(response) {
						console.log("Error while retriving users");
					}
				);
			}
		};
		
		//function openUserProfile() {
			$scope.openUserProfile = function(){
			$(".popup-body-container#userProfilePop").removeClass("hide");
			setTimeout(function () {
				$(".popup-body-container").addClass("popup-active");
			}, 100);
			$(".popup-body-container#addeditUserPop .popup-header")
				.removeClass("add-popup-header")
				.addClass("edit-popup-header");
			$("#userProfilePophead").text("User Profile");
}

		//Edit user
		$scope.editPopup = function(user) {
			$(".popup-body-container#addeditUserPop").removeClass("hide");
			$(".popup-body-container#addeditUserPop .addeditUserSubmit-btn").addClass("hide");
			$(".popup-body-container#addeditUserPop .addeditUserUpdate-btn").removeClass("hide");
			setTimeout(function() {
				$(".popup-body-container").addClass("popup-active");
			}, 100);
			$(".popup-body-container#addeditUserPop .popup-header")
				.removeClass("add-popup-header")
				.addClass("edit-popup-header");
			$("#addeditUserPophead").text("Edit User");
			$scope.user = user;
			$scope.confirmPassword = user.password;
			$scope.user.roleId = user.role.id;
		};

		$scope.editSubmit = function() {
			delete $scope.createdOn;
			delete $scope.updatedOn;
			delete $scope.role;
			delete $scope.$$hashKey;
			angular.forEach($scope.userForm, function(control, name) {
				// Excludes internal angular properties
				if (typeof name === "string" && name.charAt(0) !== "$") {
					// To display ngMessages
					control.$setTouched();
					// Runs each of the registered validators
					control.$validate();
				}
			});
			if ($scope.userForm.$valid) {
				$scope.user.updatedBy = $scope.logged_in_user;
				$http.put(appUrl + "/api/users/update/" + $scope.user.id, $scope.user).then(
					function(response) {
						//	$scope.users = response.data.response;
						if (response.data.isSuccess === false) {
							$(".popup-body-container#addeditUserPop").addClass("hide").removeClass("popup-active");
							$.growl.error({ message: response.data.message, delay: 1000 });
						} else {
							$(".popup-body-container#addeditUserPop").addClass("hide").removeClass("popup-active");
							$.growl.notice({ title: "Success!", message: "User has been updated successfully!", delay: 1000 });
							location.reload();

						}
					},
					function error(response) {
						console.log("Error while updating users");
					}
				);
			}
		};

		//delete user
		$scope.deletePopup = function(userId, userName) {
			$scope.deleteUserId = userId;
			var getusername = $(this)
				.parent("li")
				.parent("ul")
				.parent("td")
				.siblings(".username")
				.html();
			$(".popup-body-container.popup-body-msg").removeClass("hide");
			setTimeout(function() {
				$(".popup-body-container.popup-body-msg").addClass("popup-active");
			}, 100);
			$(".popup-body-container.popup-body-msg  .popup-header").addClass(
				"cancel-del-pop-header"
			);
			$(".popup-body-msg .message-popup-head").text("Delete User");
			$(".popup-body-msg .message-popup-text").text(
				"Do you want to delete the account " + userName + " ?"
			);
		};

		$scope.deleteYes = function() {
			if ($scope.deleteUserId == $scope.user_Id) {
				$.growl.error({ message: "Cannot delete loggedin user", delay: 1000 });
				return;
			}
			//$http.put(appUrl + "/api/users/delete/" + $scope.deleteUserId + "/updatedBy/" + $scope.logged_in_user).then(
			$http.delete(appUrl + "/api/users/delete/" + $scope.deleteUserId).then(
				function(response) {
					$.growl.notice({ title: "Success!", message: "User has been deleted successfully!", delay: 1000 });
					location.reload();
				},
				function error(response) {
					console.log("Error while deleting user");
				}
			);
		};

		$scope.checkPassword = function(password, confirmPassword) {
			if (password !== confirmPassword) {
				$scope.isPassword = true;
				$('#donotmatch').show();

			} else {
				$scope.isPassword = false;
				//	$('#password-error').show();
				$('#donotmatch').hide();

			}
		};

		getRoles();
		function getRoles() {
			$http.get(appUrl + "/api/roles/listByStatus")
				.then(function(response) {
					$scope.roles = response.data.response;
				}, function error(response) {
					console.log('Error while retriving roles');
				});
		}



		userAccess();
		function userAccess() {
			$http.get(appUrl + "/api/users/hasAccess/" + $scope.user_Id)
				.then(function(response) {
					$scope.userScreen = response.data;
				}, function error(response) {
					console.log('Error while deleting role');
				});
			setTimeout(function() {
				$(".menu-container ul").removeClass("hide_menu");
			}, 400);
		}

		$scope.hasAccess1 = function(menuName) {
			var screens = $scope.userScreen;
			return screens && screens.includes(menuName);
		}

		$scope.closeBtn = function() {
			location.reload();
		}
	}
);

mainApp.directive("compareTo", function() {
	return {
		require: "ngModel",
		scope: {
			otherModelValue: "=compareTo",
		},
		link: function(scope, element, attributes, ngModel) {
			ngModel.$validators.compareTo = function(modelValue) {
				return modelValue == scope.otherModelValue;
			};

			scope.$watch("otherModelValue", function() {
				ngModel.$validate();
			});
		},
	};
});