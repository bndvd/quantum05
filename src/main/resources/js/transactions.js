// Transaction Controller
app.controller("transactionsCtrl", function($scope, $http) {
	$scope.securities = [];
	$scope.positionSelectedPageIndex = -1;
	$scope.positionSelected = [];
	$scope.allTransactions = [];
	$scope.transactionAddTypeOptions = ["BUY", "SEL", "DIV", "SPL", "CNV"];
	$scope.transactionAddDate = new Date();
	$scope.transactionAddType = "";
	$scope.transactionAddShares = "";
	$scope.transactionAddPrice = "";
	$scope.transactionDeleteTran = null;
	$scope.transactionUpdateTran = null;
	$scope.transactionUpdateNewPrice = null;
	
	$scope.SEC_ID_ALL_TRANSACTIONS = -1;
	$scope.secIdToSymbolMap = [];
	
	
	$http({
		  method: "GET",
		  url: "api/v1/securities"
		}).then(
			function successCallback(response) {
				if (response != null && response.data != null && response.data.length > 0) {
					// add the "all transactions" page
					$scope.securities.push({
				        id: $scope.SEC_ID_ALL_TRANSACTIONS,
				        symbol: "All Transactions"
				    });
					
					var i;
					for (i = 0; i < response.data.length; i++) {
						$scope.securities.push(response.data[i]);
						$scope.secIdToSymbolMap[response.data[i].id] = response.data[i].symbol;
					}
					
					// load the "all transactions" page by default
					$scope.loadPositionForPageIndex(0);
				}
			},
			function errorCallback(response) {
				window.alert("Error loading securities: "+response.status);
			}
	);
	
	//
	// Load the transactions for all Positions or a given Position
	//
	$scope.loadPositionForPageIndex = function(pageIndex) {
		$scope.positionSelectedPageIndex = pageIndex;
		// All Transactions page
		if (pageIndex == 0) {
			$http({
				  method: "GET",
				  url: "api/v1/transactions"
				}).then(
					function successCallback(response) {
						$scope.allTransactions = response.data;
						// fill in the symbol, used in the "all transactions" page
						if ($scope.allTransactions != null) {
							var i;
							for (i = 0; i < $scope.allTransactions.length; i++) {
								$scope.allTransactions[i].symbol = $scope.secIdToSymbolMap[ $scope.allTransactions[i].secId ];
							}
						}
					},
					function errorCallback(response) {
						window.alert("Error loading all transactions: "+response.status);
					}
			);

		}
		// Individual Securities
		else if (pageIndex >= 1 && pageIndex < $scope.securities.length) {
			var secId = $scope.securities[pageIndex].id;
			$http({
				  method: "GET",
				  url: "api/v1/position/"+secId
				}).then(
					function successCallback(response) {
						$scope.positionSelected = response.data;
					},
					function errorCallback(response) {
						window.alert("Error loading position transactions: "+response.status);
					}
			);

		}
		else {
			$scope.positionSelected = [];
		}
	};
	
	//
	// reload currently selected Position
	//
	$scope.reloadPosition = function() {
		$scope.loadPositionForPageIndex($scope.positionSelectedPageIndex);
	};
	
	//
	// Show Add Transaction dialog
	//
	$scope.showTransactionAddDialog = function(show) {
		if (show) {
			$scope.transactionAddDate = new Date();
			$scope.transactionAddType = "";
			$scope.transactionAddShares = "";
			$scope.transactionAddPrice = "";
			document.getElementById('modalTranAdd').style.display='block';
		}
		else {
			document.getElementById('modalTranAdd').style.display='none';
		}
	};
	
	//
	// Show Delete Transaction dialog
	//
	$scope.showTransactionDeleteDialog = function(show, tranId) {
		if (show) {
			$scope.transactionDeleteTran = $scope.findTransactionInSelectedPosition(tranId);
			document.getElementById('modalTranDelete').style.display='block';
		}
		else {
			document.getElementById('modalTranDelete').style.display='none';
			$scope.transactionDeleteTran = null;
		}
	};
	
	//
	// Show Update Transaction dialog
	//
	$scope.showTransactionUpdateDialog = function(show, tranId) {
		if (show) {
			$scope.transactionUpdateTran = $scope.findTransactionInSelectedPosition(tranId);
			$scope.processTransactionUpdateTranSelection();
			document.getElementById('modalTranUpdate').style.display='block';
		}
		else {
			document.getElementById('modalTranUpdate').style.display='none';
			$scope.transactionUpdateTran = null;
			$scope.transactionUpdateNewPrice = null;
		}
	};
	
	$scope.processTransactionUpdateTranSelection = function() {
		$scope.transactionUpdateNewPrice = $scope.transactionUpdateTran.price;
	};
	
	$scope.findTransactionInSelectedPosition = function(tranId) {
		if ($scope.positionSelected != null && $scope.positionSelected.transactions != null) {
			var i;
			for (i = 0; i < $scope.positionSelected.transactions.length; i++) {
				if ($scope.positionSelected.transactions[i].id == tranId) {
					return $scope.positionSelected.transactions[i];
				}
			}
		}
		return null;
	};
	
	//
	// Add Transaction
	//
	$scope.addTransaction = function() {
		if ($scope.transactionAddDate == null || $scope.transactionAddType == "" || 
				$scope.transactionAddShares == "" || $scope.transactionAddPrice == "" ||
				$scope.positionSelectedPageIndex < 0 || $scope.positionSelectedPageIndex >= $scope.securities.length) {
			window.alert("Error adding transaction (invalid input): "+$scope.transactionAddDate+" "+$scope.transactionAddType+
				" "+$scope.transactionAddShares+" "+$scope.transactionAddPrice);
		}
		
		var secId = $scope.securities[$scope.positionSelectedPageIndex].id;
		var year = $scope.transactionAddDate.getFullYear();
		var month = $scope.transactionAddDate.getMonth() + 1;
		var monthStr = "" + month;
		if (month < 10) {
			monthStr = "0" + month;
		}
		var day = $scope.transactionAddDate.getDate();
		var dayStr = "" + day;
		if (day < 10) {
			dayStr = "0" + day;
		}
		var dateStr = year + "-" + monthStr + "-" + dayStr + "T14:30:00.000";
		
		var data = {
				    secId: secId,
				    userId: 1,
				    tranDate: dateStr,
				    type: $scope.transactionAddType,
				    shares: $scope.transactionAddShares,
				    price: $scope.transactionAddPrice
		};

		$http({
		    method: "POST",
		    url: "api/v1/transaction",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					$scope.reloadPosition();
				},
				// Error response
				function errorCallback(response) {
					window.alert("Error adding transaction: "+response.status+"; "+response.statusText);
					$scope.reloadPosition();
				}
		);

		
		$scope.showTransactionAddDialog(false);
	};
	
	
	//
	// Delete Transaction
	//
	$scope.deleteTransaction = function() {
		if ($scope.transactionDeleteTran == null) {
			return;
		}
		$http({
			  method: "DELETE",
			  url: "api/v1/transaction/" + $scope.transactionDeleteTran.id
			}).then(
				function successCallback(response) {
					$scope.reloadPosition();
				},
				function errorCallback(response) {
					window.alert("Error deleting transaction: "+response.status);
					$scope.reloadPosition();
				}
		);
		$scope.transactionDeleteTran = null;
		
		$scope.showTransactionDeleteDialog(false, null);
	};
	
	//
	// Update Transaction
	//
	$scope.updateTransaction = function() {
		if ($scope.transactionUpdateTran == null || $scope.transactionUpdateNewPrice == null ||
				$scope.positionSelectedPageIndex < 0 || $scope.positionSelectedPageIndex >= $scope.securities.length) {
			window.alert("Error updating transaction (invalid input): "+$scope.transactionUpdateNewPrice);
		}
				
		var data = {
			    secId: $scope.transactionUpdateTran.secId,
			    userId: $scope.transactionUpdateTran.userId,
			    tranDate: $scope.transactionUpdateTran.tranDate,
			    type: $scope.transactionUpdateTran.type,
			    shares: $scope.transactionUpdateTran.shares,
			    price: $scope.transactionUpdateNewPrice
		};
	
		$http({
		    method: "PUT",
		    url: "api/v1/transaction/" + $scope.transactionUpdateTran.id,
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					$scope.reloadPosition();
				},
				// Error response
				function errorCallback(response) {
					window.alert("Error updating transaction: "+response.status+"; "+response.statusText);
					$scope.reloadPosition();
				}
		);
		
		$scope.showTransactionUpdateDialog(false, null);
	};
		
});

