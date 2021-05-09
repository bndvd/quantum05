// Dashboard Controller
app.controller("dashboardCtrl", function($scope, $http) {
	$scope.assets = [];
	$scope.assetsTotalPrincipal = 0.0;
	$scope.assetsTotalLastValue = 0.0;
	$scope.assetsTotalUnrealizedGain = 0.0;
	$scope.assetsTotalRealizedGain = 0.0;
	$scope.assetsTotalContribution = 0.0;
	$scope.assetsTotalRealizedGainYtd = 0.0;
	$scope.assetsTotalYtdShortTermTax = 0.0;
	$scope.assetsTotalYtdLongTermTax = 0.0;
	$scope.assetsTotalYtdShortTermTaxAdj = 0.0;
	$scope.assetsTotalYtdLongTermTaxAdj = 0.0;
	$scope.assetsTotalYtdShortTermTaxAdjBool = false;
	$scope.assetsTotalYtdLongTermTaxAdjBool = false;
	
	$scope.positions = [];

	$http({
		  method: "GET",
		  url: "api/v1/assets"
		}).then(
			function successCallback(response) {
				$scope.assets = response.data;
				for (i = 0; i < $scope.assets.length; i++) {
					$scope.assetsTotalPrincipal += $scope.assets[i].principal;
					$scope.assetsTotalLastValue += $scope.assets[i].lastValue;
					$scope.assetsTotalUnrealizedGain += $scope.assets[i].unrealizedGain;
					$scope.assetsTotalRealizedGain += $scope.assets[i].realizedGain;
					$scope.assetsTotalContribution += $scope.assets[i].contribution;
					$scope.assetsTotalRealizedGainYtd += $scope.assets[i].realizedGainYtd;
					$scope.assetsTotalYtdShortTermTax += $scope.assets[i].ytdShortTermTax;
					$scope.assetsTotalYtdLongTermTax += $scope.assets[i].ytdLongTermTax;
					$scope.assetsTotalYtdShortTermTaxAdj += $scope.assets[i].ytdShortTermTaxAdj;
					$scope.assetsTotalYtdLongTermTaxAdj += $scope.assets[i].ytdLongTermTaxAdj;
				}
			},
			function errorCallback(response) {
				window.alert("Error loading assets: "+response.status);
			}
	);

	$http({
		  method: "GET",
		  url: "api/v1/positions"
		}).then(
			function successCallback(response) {
				$scope.positions = [];
				var i;
				for (i = 0; i < response.data.length; i++) {
					if (response.data[i].shares != 0) {
						$scope.positions.push(response.data[i]);
					}
				}
			},
			function errorCallback(response) {
				window.alert("Error loading positions: "+response.status);
			}
	);

});

