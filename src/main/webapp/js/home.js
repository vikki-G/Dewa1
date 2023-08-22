var mainApp = angular.module("DEWAReportingPortal_Home", []);


mainApp.controller("HomeController", function ($scope, $compile, $http, $filter) {
	
		$scope.logged_in_user = sessionStorage.getItem("username");
		$scope.user_role = sessionStorage.getItem("userrole");
		$scope.user_Id = sessionStorage.getItem("userId");
				
		if ($scope.logged_in_user == null || $scope.logged_in_user == "null") {
			window.location.href = appUrl + "/login.html";
			 //window.location.href = "D:/Satheesh/sts-workspace-all/CTS_reporting_portal/ctsReport/login.html";
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
		$scope.userProfile.features = sessionStorage.getItem("features");
		
		if($scope.userProfile.answer=='undefined'){
			$scope.userProfile.answer = '';
		}

		$scope.hasAccess1 = function (menuName) {
			var screens = $scope.userScreen;
			return screens && screens.includes(menuName);
		}
		
		userAccess();
		function userAccess() {
			$http.get(appUrl + "/api/users/hasAccess/" + $scope.user_Id)
				.then(function (response) {
					$scope.userScreen = response.data;
				}, function error(response) {
					console.log('Error while getting user features');
				});
				setTimeout(function() {
			$(".menu-container ul").removeClass("hide_menu");
		}, 600);
		}
		
		$scope.checkPassword = function (x, y) {
			debugger
			if (x === y) {
				$scope.isPassword = "true";
			} else {
				$scope.isPassword = "false";
			}
		};
		
		
});