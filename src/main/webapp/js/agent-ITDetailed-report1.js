	

var mainApp = angular.module("AgentDetailedReport", []);

mainApp.controller(
	"DetailedReportController",
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
			
			
			$http.post(appUrl + "/api/detailReport/getDetailReport", $scope.tjxRequest).then(
				function (response) {
					console.log("sucesssss" + JSON.stringify(response));
					document.getElementById("loader").style.display = "none";					
					$scope.length = response.data.length;
					
							
					if($scope.length<=0){
						$scope.showIcon = false;
					} else {
						$scope.showIcon = true;
					}
					bindDataToHtml(response.data);
					 
	  
				},
				function error(response) {
					
					document.getElementById("loader").style.display = "none";
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
		//			console.log('response : ' + JSON.stringify(response.data, null, 4));
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

        _html += `<tr id="${el.sessionId}_p">`;
        _html += `<td id="name">${el.date}</td>`;
		_html += `<td id="contact">${el.time}</td>`;
		_html += `<td id="section">${el.section}</td>`;
        _html += `<td id="type" >${el.callingNumber}</td>`;
        _html += `<td id="offered" >${el.answeringNumber}</td>`;		
        _html += `<td id="answered" >${el.callDuration}</td>`;
        _html += `<td id="abandoned" >${el.holdDuration}</td>`;
        _html += `</tr>`;
		_html += `<tr id="${el.sessionId}_c" style="border-bottom: 1px solid #aac5cd;">`;
		_html += `<td id="perAnswered" colspan="7" ><strong>UnAnswered Calls : </strong> ${el.unAnsweredCalls}</td>`;
		_html += `<td class="hide"></td><td class="hide"></td><td class="hide"></td><td class="hide"></td><td class="hide"></td><td class="hide"></td>`;
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

		
								$http({
										url: appUrl + '/api/detailReport/excel',
										method: 'POST',
										responseType: 'arraybuffer',
										data: $scope.tjxRequest, //this is your json data string
										headers: {
											'Content-type': 'application/json',
											'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
										}
									}).then(function(data){
											var blob = new Blob([data.data], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});										
											saveAs(blob, 'IT_Emergency_Report_' + new Date().toLocaleString() + '.xlsx');
										},
										function error(response) {
											console.log("Error while downloading agent Report excel");
										}
									);
		
		
	}
	
				$scope.downloadPdf = function() {

		
								$http({
										url: appUrl + '/api/detailReport/pdf',
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
