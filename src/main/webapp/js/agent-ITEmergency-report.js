	

var mainApp = angular.module("AgentCallTransactionReport", []);

mainApp.controller(
	"CallTransactionReportReportController",
	function ($scope, $compile, $http, $filter) {
		$scope.logged_in_user = sessionStorage.getItem("username");
		$scope.user_Id = sessionStorage.getItem("userId");

		

		if ($scope.logged_in_user == null || $scope.logged_in_user == "null") {
			  window.location.href = appUrl + "/login.html";
		}
		$scope.tjxRequest = {};
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
		
		$("#custom-search").click(function() {
			$(".loader-containerr").removeClass("hidden");
			    var overlay = document.getElementById('overlay');
                overlay.style.display = 'block';
		});
			supportAccess();
			debugger;
		    function supportAccess() {			
			$.ajax({
			url : appUrl +  "/api/users/supportDtls/" + $scope.user_Id,
			method:"get",
			async:false,
			cache: false,
			success : function(response) {
				$scope.supportDtls = response;				
					//console.log('response : ' +  $scope.campaignDtls );
					
			}
			,  error: function(jqXHR,error, errorThrown) { 
			console.log('Error while getting campaignsssss');
			}	
				
			});	
			}
			
		
		getSupportType();
		
		$scope.typeDropdown = [];
		
			function getSupportType() {
		
		console.log('response supportType: ' +  $scope.supportDtls );
		
		
		$http.get(appUrl + "/api/agent/list/" + $scope.supportDtls )
			.then(function (response) {
				$scope.type = response.data.response;
				//alert(JSON.stringify($scope.campaign));
				console.log(JSON.stringify($scope.type));
			}, function error(response) {
				console.log('Error while retriving supportType');
			});
	}
		
	
		
		$scope.getTjxReportData = function() {
            debugger
			var range = $('input[name="intervel"]:checked').val();
			$scope.tjxRequest.startDate = $("#txtStartDate").val();;
			$scope.tjxRequest.endDate = $("#txtEndDate").val();;
			$scope.tjxRequest.dateRange = range;
			var supportType = $("#type").val();	
			$scope.support="";
			
			if (supportType==="string:All" || supportType === "?" ){
				for (var i = 0; i < $scope.supportDtls.length; i++) {
			if (i === ($scope.supportDtls.length - 1)) {
				$scope.support += $scope.supportDtls[i];			
				} else {
				$scope.support += $scope.supportDtls[i] + ",";
				}
			}		
				supportType = $scope.support;
				console.log(supportType + "testeste");
				$scope.tjxRequest.type = supportType;
			}else{
				 supportType = $("#type").val();
				 $scope.tjxRequest.type = supportType.substring(supportType.indexOf(':')+1);  
			}
			
			console.log('$scope.tjxRequest : ' + JSON.stringify($scope.tjxRequest,null,4));
			
			
	        // var download = hasAccess1('Downloads');
			$http.post(appUrl + "/api/agent/agentReport", $scope.tjxRequest).then(
				function (response) {
					//console.log("sucesssss" + JSON.stringify(response));
					//document.getElementById("loader").style.display = "none";					
					$scope.length = response.data.length;
					//$scope.showIcon = hasAccess1('Downloads') ? true : false;
					bindDataToHtml(response.data);
		            $(".loader-containerr").addClass("hidden");
                    overlay.style.display = 'none';
					if($scope.length<=0){
						$scope.showIcon = false;
					} else {
						$scope.showIcon = true;
					}
			
					
					// if (download && $scope.length  > 0) {
					// 	$scope.showIcon = true;
					//   } else {
					// 	$scope.showIcon = false;
					//   }
	  
				},
				function error(response) {
					
					//document.getElementById("loader").style.display = "none";
					console.log("Error while retrieving TJX report data");
				}
			);
		}
		
		

		
		
		userAccess();
		function userAccess() {
		//	console.log('$scope.user_Id : ' + $scope.user_Id);
			$http.get(appUrl + "/api/users/hasAccess/" + $scope.user_Id)
				.then(function (response) {
					$scope.userScreen = response.data;
					$scope.length = response.data.length;
		//			console.log('response : ' + JSON.stringify(response.data, null, 4));
						// if($scope.userScreen === "Downloads" && $scope.length>=0){
						// 	$scope.showIcon = true;
						// } else {
						// 	$scope.showIcon = false;
						// }
				}, function error(response) {
					console.log('Error while checking userAccess');
				});
			// return true;
			setTimeout(function() {
			$(".menu-container ul").removeClass("hide_menu");
		}, 400);
		}



		$scope.hasAccess1 = function (menuName) {
			debugger
			var screens = $scope.userScreen;
			return screens && screens.includes(menuName);

		}

	
		function bindDataToHtml(jsonResponse) {
			$('#TJX_report').DataTable().destroy();
            var _tbody = $('#tbodyCtiDropReason');
            _tbody.html('');
             var _html = "";
        $.each(jsonResponse, function (i, el) {

        _html += `<tr id="${el.name}">`;
        _html += `<td id="name">${el.name}</td>`;
		_html += `<td id="contact">${el.contact}</td>`;
        _html += `<td id="type" >${el.type}</td>`;
        _html += `<td id="offered" >${el.offered}</td>`;		
        _html += `<td id="answered" >${el.answered}</td>`;
        _html += `<td id="abandoned" >${el.abandoned}</td>`;
		_html += `<td id="perAnswered">${el.perAnswered}</td>`;
        _html += `</tr>`;


    });

    _tbody.append(_html);
	//Datatable
        $(document).ready(function () {
			$('#TJX_report').DataTable({
                destroy: true,
				autoWidth: false,
                scrollCollapse: true,
                iDisplayLength: 25,
                order: []
            
            });
        });
  		//Datatable 

}


			$scope.downloadExcel = function() {

							debugger
								$http({

									
										url: appUrl + '/api/agent/agentReport/excel',
										method: 'POST',
										responseType: 'arraybuffer',
										data: $scope.tjxRequest, //this is your json data string
										headers: {
											'Content-type': 'application/json',
											'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
										}
									}).then(function(data){
									var blob = new Blob([data.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});										
							//		var file = new File([data.data], 'IT_Emergency_Report_' + new Date().toLocaleString() + '.xlsx', {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
									saveAs(blob, 'IT_Emergency_Report_' + new Date().toLocaleString() + '.xlsx');
								//	saveAs(file);
						},
										function error(response) {
											console.log("Error while downloading agent Report excel");
										}
									);
		
		
	}
	
				$scope.downloadPdf = function() {

		
								$http({
										url: appUrl + '/api/agent/agentReport/pdf',
										method: 'POST',
										responseType: 'arraybuffer',
										data: $scope.tjxRequest, //this is your json data string
										headers: {
											'Content-type': 'application/json',
											'Accept': 'application/pdf'
										}
									}).then(function(data){
										var blob = new Blob([data.data], {type: "application/pdf"});										
										saveAs(blob, 'IT_Emergency_Report_' + new Date().toLocaleString() + '.pdf');
									}).error(function(){
										console.log('Error while downloading agent Report pdf');
									});
		
		
	}
	
	$scope.checkPassword = function (x, y) {
			if (x === y) {
				$scope.isPassword = "true";
			} else {
				$scope.isPassword = "false";
			}
		};
		
		
	}
);
