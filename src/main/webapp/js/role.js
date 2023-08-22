var mainApp = angular.module('DEWAReportingPortal_Role', ['angularjs-dropdown-multiselect']);

mainApp.controller("RoleController", function ($scope, $compile, $http, $filter) {


	mainApp.config([
		"$locationProvider",
		function ($locationProvider) {
			$locationProvider.html5Mode({
				enabled: true,
				requireBase: false,
			});
		},
	]);

	$scope.logged_in_user = sessionStorage.getItem("username");
	$scope.user_role = sessionStorage.getItem("userrole");
	$scope.user_Id = sessionStorage.getItem("userId");
	
	
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
		$scope.userProfile.supportType = sessionStorage.getItem("supportType");

		
		if($scope.userProfile.answer=='undefined'){
			$scope.userProfile.answer = '';
		}

	if ($scope.logged_in_user == null || $scope.logged_in_user == "null") {
		 window.location.href = appUrl + "/login.html";
	}
	
		
	getFeatures();
	getSupportType();

	
		$scope.featureDropdown = [];
	$scope.featureSetting = {
		scrollableHeight: '200px',
		scrollable: true,
		enableSearch: true,
		displayProp: 'featureName',
		idProperty: 'id'
	};
	
	

	getRoles();
	function getRoles() {
		$http.get(appUrl + "/api/roles/list")
			.then(function (response) {
				$scope.roles = response.data.response;
				for (var i = 0; i < $scope.roles.length; i++) {
					$scope.featureStr = "";
					for (var j = 0; j < $scope.roles[i].features.length; j++) {
						if (j === ($scope.roles[i].features.length - 1)) {
							$scope.featureStr += $scope.roles[i].features[j].featureName;
						} else {
							$scope.featureStr += $scope.roles[i].features[j].featureName + ",";
						}
					}
					$scope.roles[i].featuresList = $scope.featureStr;
				}
		//Datatable
      $(document).ready(function () {
		  setTimeout(function () {
               $('#RoleManagementTable').DataTable({
          destroy: true,
          iDisplayLength: 25,
          order: [],
          columnDefs: [{ orderable: false, targets: [8] }]
        });
            }, 100);
        hideLoader(); 
      });
      
     //   var loaderContainer = document.querySelector(".loader-containerr");
      //  loaderContainer.style.display = 'none';
      //Datatable
			}, function error(response) {
				console.log('Error while retriving roles');
				hideLoader(); 
			});
	}

	//Add Role
	$scope.addRole = function () {
		
		$scope.featureDropdown = [];
		$scope.typeDropdown = [];

		$('.popup-body-container#addRolePop').removeClass("hide");
		setTimeout(function () {
			$('.popup-body-container').addClass("popup-active");
		}, 100);
		$('.popup-body-container#addRolePop .popup-header').removeClass("edit-popup-header").addClass("add-popup-header");
		$('#addRolePophead').text('Add Role');
		$scope.role = {};
		$scope.roleForm.$setPristine();
		$scope.roleForm.$setUntouched();
	}
	
	// getFeatures();
	function getFeatures() {
		$http.get(appUrl + "/api/features/list")
			.then(function (response) {
				$scope.features = response.data.response;
			}, function error(response) {
				console.log('Error while retriving features');
			});
	}

	
	
	function getSupportType() {
		$http.get(appUrl + "/api/roles/typelist")
			.then(function(response) {
				let data =[];
				for(var i=0;i < response.data.response.length;i++){
					data.push({
						label:response.data.response[i].section, id:response.data.response[i].id
					})
				}
				$scope.type = data;
				console.log($scope.type);
			},function error(response){
				console.log('Error while retriving types');
			});
	}
				


	$scope.save = function () {
		
		$scope.featureIds = "";
		$scope.sectionsAccessible = "";
		
		
	var addSuportTypeSelectedVal = $("#add-support-type .selected_val").val();
		for (var i = 0; i < $scope.featureDropdown.length; i++) {
			if (i === ($scope.featureDropdown.length - 1)) {
				$scope.featureIds += $scope.featureDropdown[i].id;
				//alert($scope.featureIds);
				
			} else {
				$scope.featureIds += $scope.featureDropdown[i].id + ",";
			}
		}
			
		
		
		angular.forEach($scope.roleForm, function (control, name) {
			// Excludes internal angular properties
			if (typeof name === "string" && name.charAt(0) !== "$") {
				// To display ngMessages
				control.$setTouched();
				// Runs each of the registered validators
				control.$validate();
			}
		});
		$scope.errorMessage = null;
		if($scope.featureDropdown.length===0){
			$scope.errorMessage = "The Role features is required";
			return;
		}
		$scope.errorMessage = null;
		if(addSuportTypeSelectedVal.length===0){
			$scope.errorMessage = "The Sections Accessible is required";
			return;
		}

		if ($scope.roleForm.$valid) {
			
			$scope.role.featureIds = $scope.featureIds;
			$scope.role.createdBy = $scope.logged_in_user;
			$scope.role.sectionsAccessible = addSuportTypeSelectedVal;
	
			//alert(JSON.stringify($scope.role));
			 $http.post(appUrl + "/api/roles/save", $scope.role)
				.then(function (response) {
					$scope.rolesList = response.data.response;
					if (response.data.isSuccess === false) {
						$(".popup-body-container#addRolePop").addClass("hide");
						$.growl.error({ message: response.data.message, delay: 1000 });
					} else {
						$(".popup-body-container#addRolePop").addClass("hide");
						$.growl.notice({ title: "Success!", message: "Role has been created successfully!", delay: 1000 });
						setTimeout(function() {
								location.reload();
							}, 2000);
					}
					$scope.role = null;
					$scope.featureDropdown = [];					
					addroleEnd();
				}, function error(response) {
					console.log('Error while saving role');
				}); 
		}
	}

	$scope.cancel = function () {
		addroleEnd();
	}

	function addroleEnd() {
		$(".multiselect_field .multiselect_group_option > li > .multiselect_group_option_list > li > label .input-checkbox").prop("checked", false);
		$(".multiselect_field .multiselect_group_option > li > .group_name_head .select-all .input-checkbox").prop("checked", false);
		$(".selected_val").val("");
		$(".campaign_field").addClass("hide");
	}
	
	//Edit Role

	$scope.editRole = function (role) {
		$scope.role = role;
		$scope.featureDropdown = [];
		
		for (var i = 0; i < $scope.role.features.length; i++) {
			$scope.featureDropdown.push($scope.role.features[i]);
		}
		
		$('.popup-body-container#editRolePop').removeClass("hide");
		setTimeout(function () {
			$('.popup-body-container').addClass("popup-active");
		}, 100);
		$('.popup-body-container#editRolePop .popup-header').removeClass("add-popup-header").addClass("edit-popup-header");
		$('#editRolePophead').text('Edit Role');
	}

	$scope.update = function (role) {
		$scope.featureIds = "";
		var editSuportTypeSelectedVal = $("#edit-support-type .selected_val").val();
		for (var i = 0; i < $scope.featureDropdown.length; i++) {
			if (i === ($scope.featureDropdown.length - 1)) {
				$scope.featureIds += $scope.featureDropdown[i].id;
			} else {
				$scope.featureIds += $scope.featureDropdown[i].id + ",";
			}
		}
			
		angular.forEach($scope.roleeditForm, function (control, name) {
			// Excludes internal angular properties
			if (typeof name === "string" && name.charAt(0) !== "$") {
				// To display ngMessages
				control.$setTouched();
				// Runs each of the registered validators
				control.$validate();
			}
		});
		$scope.errorMessage = null;
		if($scope.featureDropdown.length===0){
			$scope.errorMessage = "The Role features is required";
			return;
		}

		$scope.errorMessage = null;
		if(editSuportTypeSelectedVal.length===0){
			$scope.errorMessage = "The Sections Accessible is required";
			return;
		}

		
		if ($scope.roleeditForm.$valid) {
			$scope.updateRole = {};
			$scope.updateRole.roleName = role.roleName;
			$scope.updateRole.status = role.status;
			$scope.updateRole.featureIds = $scope.featureIds;
			$scope.updateRole.updatedBy = $scope.logged_in_user;
		//	$scope.updateRole.updatedOn = role.updatedOn;
			//$scope.updateRole.createdBy = $scope.logged_in_user;
		//	$scope.updateRole.supportType = editSuportTypeSelectedVal;
		  	//alert(JSON.stringify($scope.updateRole));

			$scope.updateRole.sectionsAccessible = editSuportTypeSelectedVal;
			
			$http.put(appUrl + "/api/roles/update/" + role.id, $scope.updateRole)
				.then(function (response) {
					if (response.data.isSuccess === false) {
						$.growl.error({ message: response.data.message, delay: 1000 });
					} else {
						$(".popup-body-container#editRolePop").addClass("hide");
						$.growl.notice({ title: "Success!", message: "Role has been updated successfully!", delay: 1000 });
						setTimeout(function() {
								location.reload();
							}, 2000);
					}
					$scope.role = null;
					$scope.featureDropdown = [];
				}, function error(response) {
					console.log('Error while saving role');
				});
		}
	}

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



	//delete role
	$scope.deletePopup = function (id, roleName) {
		$scope.deleteroleId = id;
		var getusername = $(this)
			.parent("li")
			.parent("ul")
			.parent("td")
			.siblings(".username")
			.html();
		$(".popup-body-container.popup-body-msg").removeClass("hide");
		setTimeout(function () {
			$(".popup-body-container.popup-body-msg").addClass("popup-active");
		}, 100);
		$(".popup-body-container.popup-body-msg  .popup-header").addClass(
			"cancel-del-pop-header"
		);
		$(".popup-body-msg .message-popup-head").text("Delete Role");
		$(".popup-body-msg .message-popup-text").text(
			"Do you want to delete the account " + roleName + " ?"
		);
	};

	$scope.deleteYes = function () {
		$http.delete(appUrl + "/api/roles/delete/" + $scope.deleteroleId).then(
			function (response) {
				if (response.data.isSuccess === false) {
						$.growl.error({ message: 'Error while deleting role', delay: 1000 });
					} else {
						$(".popup-body-container#editRolePop").addClass("hide");
						$.growl.notice({ title: "Success!", message: "Role has been deleted successfully!", delay: 1000 });
						setTimeout(function() {
								location.reload();
							}, 2000);
					}
			},
			function error(response) {
				console.log("Error while deleting role");
			}
		);
	};

	userAccess();
	function userAccess() {
		$http.get(appUrl + "/api/users/hasAccess/" + $scope.user_Id)
			.then(function (response) {
				$scope.userScreen = response.data;
			}, function error(response) {
				console.log('Error while deleting role');
			});
			setTimeout(function() {
			$(".menu-container ul").removeClass("hide_menu");
		}, 300);
	}
	
	
	

	$scope.hasAccess1 = function (menuName) {
		var screens = $scope.userScreen;
		return screens && screens.includes(menuName);
	}
	
	
$scope.closeBtn = function () {
		location.reload();
		editoleEnd();
	}
	
	$scope.checkPassword = function (x, y) {
			if (x === y) {
				$scope.isPassword = "true";
			} else {
				$scope.isPassword = "false";
			}
		};
		
			function editoleEnd() {
		$(".multiselect_field .multiselect_group_option > li > .multiselect_group_option_list > li > label .input-checkbox").prop("checked", false);
		$(".multiselect_field .multiselect_group_option > li > .group_name_head .select-all .input-checkbox").prop("checked", false);
		$(".selected_val").val("");
		$(".campaign_field").addClass("hide");
	}



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

// $(document).ready(function() {
//     $("#sectionsaccessible").click(function() {
//         var textareaValue = $("#sectionsaccessible").val();
//         var submitButton = $("#rolesubmit");
//         if (textareaValue.trim() === "") {

// 			$( "#rolesubmit" ).prop( "disabled", true );
//             // $("#rolesubmit").prop("disabled", true);
//             // console.log("Textarea is empty. Please enter a value.");
//              alert("Textarea is empty. Please enter a value.");
//             // return;
//         } else {
		
// 			$( "#rolesubmit" ).prop( "disabled", false );
//             // $("#rolesubmit").prop("disabled", false);
//             // console.log("Textarea value is valid:", textareaValue);
//              alert(`Textarea value is valid: ${textareaValue}`);
//         }
//     });
// });
