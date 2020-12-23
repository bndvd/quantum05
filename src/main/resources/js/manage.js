// Manage Controller
app.controller("manageCtrl", function($rootScope, $scope, $http) {
	$scope.TRUE_STR = "true";
	$scope.FALSE_STR = "false";
	
	$scope.MANAGE_PAGE_SECURITIES = 1;
	$scope.MANAGE_PAGE_RATIOS = 2;
	$scope.MANAGE_PAGE_SETTINGS = 3;
	$scope.MANAGE_PAGE_BACKUP = 4;
	
	$scope.MANAGE_EXPORT_CSV = 1;
	$scope.MANAGE_EXPORT_JSON = 2;
	
	$scope.manageKeyvalMap;
	$scope.managePageIndex = 0;
	$scope.manageAssets;
	$scope.manageBasketIdToNameMap;
	$scope.manageTargetRatiosByAsset = null;
	$scope.manageSecurities;
	
	$scope.manageNewAsset = "";
	$scope.manageNewSecurity = "";
	$scope.manageNewSecurityBasketId = null;
	
	// saved properties (settings)
	$scope.propIexToken;
	$scope.propTaxRate;
	$scope.propContribution;
	$scope.propQPlotBenchmarkSymbol = null;
	$scope.propQPlotSimTargetPrinicipalInit = null;
	$scope.propQPlotSimTargetPrinicipalIncr = null;
	$scope.propQPlotSimTargetMonths = null;
	$scope.propQPlotSimTargetWholeShares = false;
	
	
	$scope.refreshManageKeyvalMap = function() {
		$http({
			  method: "GET",
			  url: "api/v1/keyval"
			}).then(
				function successCallback(response) {
					$scope.manageKeyvalMap = {};
					var i;
					for (i = 0; i < response.data.length; i++) {
						$scope.manageKeyvalMap[response.data[i].key] = response.data[i].value;
					}

					// read in specific values from keyval map
					$scope.propIexToken = $scope.manageKeyvalMap["pr.iextoken"];
					$scope.propTaxRate = $scope.manageKeyvalMap["pr.tax"];
					$scope.propContribution = $scope.manageKeyvalMap["pr.contr"];
					$scope.propQPlotBenchmarkSymbol = $scope.manageKeyvalMap["pr.qpbs"];
					if ($scope.propQPlotBenchmarkSymbol == null) {
						$scope.propQPlotBenchmarkSymbol = "VTI";
					}
					$scope.propQPlotSimTargetPrinicipalInit = $scope.manageKeyvalMap["pr.qpstpinit"];
					if ($scope.propQPlotSimTargetPrinicipalInit == null) {
						$scope.propQPlotSimTargetPrinicipalInit = 10000;
					}
					$scope.propQPlotSimTargetPrinicipalIncr = $scope.manageKeyvalMap["pr.qpstpincr"];
					if ($scope.propQPlotSimTargetPrinicipalIncr == null) {
						$scope.propQPlotSimTargetPrinicipalIncr = 20;
					}
					$scope.propQPlotSimTargetMonths = $scope.manageKeyvalMap["pr.qpstmos"];
					if ($scope.propQPlotSimTargetMonths == null) {
						$scope.propQPlotSimTargetMonths = 0;
					}
					var propWholeSharesStr = $scope.manageKeyvalMap["pr.qpstpwh"];
					if (propWholeSharesStr == $scope.TRUE_STR) {
						$scope.propQPlotSimTargetWholeShares = true;
					}
					else {
						$scope.propQPlotSimTargetWholeShares = false;
					}
				},
				function errorCallback(response) {
					window.alert("Error loading manage keyvals: "+response.status);
				}
		);
	};
	$scope.refreshManageKeyvalMap();
	
	$scope.refreshManageAssetsAndTargetRatios = function() {
		$http({
			  method: "GET",
			  url: "api/v1/assets"
			}).then(
				function successCallback(response) {
					$scope.manageAssets = response.data;
					$scope.manageBasketIdToNameMap = [];
					// refresh target ratios
					if ($scope.manageTargetRatiosByAsset == null) {
						$scope.manageTargetRatiosByAsset = [];
					}
					var i;
					for (i = 0; i < $scope.manageAssets.length; i++) {
						var basketId = $scope.manageAssets[i].basketId;
						var basketName = $scope.manageAssets[i].basketName;
						$scope.manageBasketIdToNameMap[basketId] = basketName;
						var basketRatio = $scope.manageKeyvalMap["pr.tr."+basketId];
						if (basketRatio == null) {
							basketRatio = 0;
						}
						if (i >= $scope.manageTargetRatiosByAsset.length) {
							$scope.manageTargetRatiosByAsset.push({
								basketId : basketId,
								ratio : basketRatio,
								basketName : basketName
							});
						}
						else {
							$scope.manageTargetRatiosByAsset[i].basketId = basketId;
							$scope.manageTargetRatiosByAsset[i].ratio = basketRatio;
							$scope.manageTargetRatiosByAsset[i].basketName = basketName;
						}
					}
					
					// read in all the securities
					$http({
						  method: "GET",
						  url: "api/v1/securities"
						}).then(
							function successCallback(response) {
								$scope.manageSecurities = [];
								var i;
								for (i = 0; i < response.data.length; i++) {
									var secId = response.data[i].id;
									var symbol = response.data[i].symbol;
									var basketId = response.data[i].basketId;
									var basketName = $scope.manageBasketIdToNameMap[basketId];
									
									var secItem = {
										secId : secId,
										symbol : symbol,
										basketId : basketId,
										basketName : basketName
									};
									$scope.manageSecurities.push(secItem);
								}
							},
							function errorCallback(response) {
								window.alert("Error loading manage securities: "+response.status);
							}
					);
					
				},
				function errorCallback(response) {
					window.alert("Error loading manage assets: "+response.status);
				}
		);
	};
	$scope.refreshManageAssetsAndTargetRatios();
	
	
	//
	// Show selected Manage page
	//
	$scope.showManagePageIndex = function(pageIndex) {
		if (pageIndex == $scope.MANAGE_PAGE_RATIOS) {
			$scope.refreshManageAssetsAndTargetRatios();
		}
		$scope.managePageIndex = pageIndex;
	};
	
	//
	// Save Asset
	//
	$scope.saveAsset = function() {
		// return if new asset is not defined or if it already exists
		if ($scope.manageNewAsset == null || $scope.manageNewAsset.trim() == "") {
			return;
		}
		$scope.manageNewAsset = $scope.manageNewAsset.trim();
		
		var i;
		for (i = 0; i < $scope.manageAssets.length; i++) {
			if ($scope.manageNewAsset == $scope.manageAssets[i].basketName) {
				window.alert("Asset Name already exists. Please enter a new Asset Name.");
				return;
			}
		}
		
		var data = {
				basketName : $scope.manageNewAsset
		};
	
		$http({
		    method: "POST",
		    url: "api/v1/asset",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					$scope.refreshManageAssetsAndTargetRatios();
				},
				// Error response
				function errorCallback(response) {
					window.alert("Error saving new asset: "+response.status+"; "+response.statusText);
					$scope.refreshManageAssetsAndTargetRatios();
				}
		);

		$scope.manageNewAsset = "";
	};
	
	//
	// Save Security
	//
	$scope.saveSecurity = function() {
		if ($scope.manageNewSecurity == null || $scope.manageNewSecurity.trim() == "" ||
				$scope.manageNewSecurityBasketId == null) {
			return;
		}
		$scope.manageNewSecurity = $scope.manageNewSecurity.trim();
		
		var i;
		for (i = 0; i < $scope.manageSecurities.length; i++) {
			if ($scope.manageNewSecurity == $scope.manageSecurities[i].symbol) {
				window.alert("Security symbol already exists. Please enter a new Security symbol.");
				return;
			}
		}
		
		var data = {
			    symbol: $scope.manageNewSecurity,
			    basketId: $scope.manageNewSecurityBasketId
		};
		$http({
		    method: "POST",
		    url: "api/v1/security",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					$scope.refreshManageAssetsAndTargetRatios();
				},
				// Error response
				function errorCallback(response) {
					window.alert("Error saving new security: "+response.status+"; "+response.statusText);
					$scope.refreshManageAssetsAndTargetRatios();
				}
		);
		
		$scope.manageNewSecurity = "";
		$scope.manageNewSecurityBasketId = null;
	};
	
	//
	// Save Target Ratios
	//
	$scope.saveTargetRatios = function() {
		var i;
		for (i = 0; i < $scope.manageTargetRatiosByAsset.length; i++) {
			var basketId = $scope.manageTargetRatiosByAsset[i].basketId;
			
			if (! isFinite($scope.manageTargetRatiosByAsset[i].ratio)) {
				$scope.manageTargetRatiosByAsset[i].ratio = 0;
			}
			var basketRatio = $scope.manageTargetRatiosByAsset[i].ratio;
			
			var data = {
				    key: "pr.tr."+basketId,
				    value: basketRatio
			};
			$http({
			    method: "POST",
			    url: "api/v1/keyval",
			    data: data,
			    headers: {"Content-Type": "application/json"}
			}).then(
					// Success response
					function successCallback(response) {
						$scope.refreshManageKeyvalMap();
						$scope.refreshManageAssetsAndTargetRatios();
					},
					// Error response
					function errorCallback(response) {
						window.alert("Error saving target ratios: "+response.status+"; "+response.statusText);
						$scope.refreshManageKeyvalMap();
						$scope.refreshManageAssetsAndTargetRatios();
					}
			);
		}
	};
	
	//
	// Save Settings
	//
	$scope.saveSettings = function() {
		// save IEX Token setting
		if ($scope.propIexToken == null || $scope.propIexToken.trim() == "") {
			$scope.propIexToken = null;
		}
		saveSetting("pr.iextoken", $scope.propIexToken);
		
		// save Tax Rate setting
		if (! isFinite($scope.propTaxRate)) {
			$scope.propTaxRate = 0;
		}
		saveSetting("pr.tax", $scope.propTaxRate);
		
		// save Contribution setting
		if (! isFinite($scope.propContribution)) {
			$scope.propContribution = 0;
		}
		saveSetting("pr.contr", $scope.propContribution);
		
		// save Chart Benchmark symbol
		if ($scope.propQPlotBenchmarkSymbol == null || $scope.propQPlotBenchmarkSymbol.trim() == "") {
			$scope.propQPlotBenchmarkSymbol = "VTI";
		}
		saveSetting("pr.qpbs", $scope.propQPlotBenchmarkSymbol.trim().toUpperCase());
		
		// save Chart Sim Target Initial Principal setting
		if (! isFinite($scope.propQPlotSimTargetPrinicipalInit)) {
			$scope.propQPlotSimTargetPrinicipalInit = 0;
		}
		saveSetting("pr.qpstpinit", $scope.propQPlotSimTargetPrinicipalInit);
		
		// save Chart Sim Target Incremental Principal setting
		if (! isFinite($scope.propQPlotSimTargetPrinicipalIncr)) {
			$scope.propQPlotSimTargetPrinicipalIncr = 0;
		}
		saveSetting("pr.qpstpincr", $scope.propQPlotSimTargetPrinicipalIncr);
		
		// save Chart Sim Target Months setting
		if (! isFinite($scope.propQPlotSimTargetMonths)) {
			$scope.propQPlotSimTargetMonths = 0;
		}
		saveSetting("pr.qpstmos", $scope.propQPlotSimTargetMonths);
		
		var propWholeSharesStr = $scope.FALSE_STR;
		if ($scope.propQPlotSimTargetWholeShares == true) {
			propWholeSharesStr = $scope.TRUE_STR;
		}
		saveSetting("pr.qpstpwh", propWholeSharesStr);
	};
	
	
	var saveSetting = function(key, value) {
		if (key == null) {
			return;
		}
		
		if (value == null) {
			var data = {
			};
			$http({
			    method: "DELETE",
			    url: "api/v1/keyval/"+key,
			    data: data,
			    headers: {"Content-Type": "application/json"}
			}).then(
					// Success response
					function successCallback(response) {
						$scope.refreshManageKeyvalMap();
					},
					// Error response
					function errorCallback(response) {
						window.alert("Error saving property: "+response.status+"; "+response.statusText);
						$scope.refreshManageKeyvalMap();
					}
			);
		}
		else {
			var data = {
				    key: key,
				    value: value
			};
			$http({
			    method: "POST",
			    url: "api/v1/keyval",
			    data: data,
			    headers: {"Content-Type": "application/json"}
			}).then(
					// Success response
					function successCallback(response) {
						$scope.refreshManageKeyvalMap();
					},
					// Error response
					function errorCallback(response) {
						window.alert("Error saving property: "+response.status+"; "+response.statusText);
						$scope.refreshManageKeyvalMap();
					}
			);
		}
	};
	
	//
	// Back up portfolio to local disk
	//
	$scope.backupPortfolio = function() {
		$http({
			  method: "GET",
			  url: "api/v1/portfolioData"
			}).then(
				function successCallback(response) {
					var portfolioData = response.data;
					if (portfolioData != null) {
						var exportFileType = document.querySelector("input[name = backupFileType]:checked").value;
						if (exportFileType != $scope.MANAGE_EXPORT_CSV && exportFileType != $scope.MANAGE_EXPORT_JSON) {
							exportFileType = $scope.MANAGE_EXPORT_CSV;
						}
						
						var portfolioDataText = $scope.preparePortfolioDataForExport(portfolioData, exportFileType);
						if (portfolioDataText == null) {
							window.alert("Error backing up data because data could not be prepared.");
							return;
						}

						var currentDate = new Date();
						var dateStr = "" + currentDate.getFullYear();
						var month = currentDate.getMonth() + 1;
						if (month < 10) {
							dateStr = dateStr + "0";
						}
						dateStr = dateStr + month;
						var day = currentDate.getDate();
						if (day < 10) {
							dateStr = dateStr + "0";
						}
						dateStr = dateStr + day;
						var filename = "qexport_" + dateStr;
						if (exportFileType == $scope.MANAGE_EXPORT_CSV) {
							filename = filename + ".csv";
						}
						else if (exportFileType == $scope.MANAGE_EXPORT_JSON) {
							filename = filename + ".json";
						}
						
						$scope.saveTextAsFile(portfolioDataText, filename);
					}
					else {
						window.alert("Error backing up data because returned data was null.");
					}
				},
				function errorCallback(response) {
					window.alert("Error backing up data: "+response.status);
				}
		);

	};
	

	$scope.saveTextAsFile = function(data, filename, fileType) {
		if (data == null || filename == null) {
			window.alert("Data backup: No data");
			return;
		}
		
		var mimeType = "text/plain";
		if (fileType == $scope.MANAGE_EXPORT_CSV) {
			mimeType = "text/csv";
		}
		else if (fileType == $scope.MANAGE_EXPORT_JSON) {
			mimeType = "application/json";
		}
	
	    var blob = new Blob([data], { type: mimeType }),
	    e = document.createEvent("MouseEvents"),
	    a = document.createElement("a");
	    // FOR IE:

	    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
	        window.navigator.msSaveOrOpenBlob(blob, filename);
	    }
	    else
	    {
	        var e = document.createEvent("MouseEvents"),
	        a = document.createElement("a");

	        a.download = filename;
	        a.href = window.URL.createObjectURL(blob);
	        a.dataset.downloadurl = [mimeType, a.download, a.href].join(":");
	        e.initEvent("click", true, false, window,
	        0, 0, 0, 0, 0, false, false, false, false, 0, null);
	        a.dispatchEvent(e);
	    }
	};
	
	
	$scope.preparePortfolioDataForExport = function(data, exportFileType) {
		var result = null;
		var jsonData = $scope.preparePortfolioDataJson(data);
		
		if (exportFileType == $scope.MANAGE_EXPORT_CSV) {
			result = $scope.convertJsonDataToCsv(jsonData);
		}
		else if (exportFileType == $scope.MANAGE_EXPORT_JSON) {
			result = JSON.stringify(jsonData);
		}
		
		return result;
	};
	
	
	$scope.preparePortfolioDataJson = function(data) {
		var jsonOutput = {
			    version: data.version,
			    lastDate: data.lastDate,
			    basketEntities: data.basketEntities,
			    securities: data.securities,
			    transactions: []
		};
		
		// copy over only data that's persisted in the db
		var i;
		for (i = 0; i < data.transactions.length; i++) {
			var t = data.transactions[i];
			jsonOutput.transactions.push({
	            id: t.id,
	            secId: t.secId,
	            userId: t.userId,
	            tranDate: t.tranDate,
	            type: t.type,
	            shares: t.shares,
	            price: t.price
			});
		}
		
		return jsonOutput;
	};
	
	$scope.convertJsonDataToCsv = function(jsonData) {
		if (jsonData == null) {
			return null;
		}
		
		var i, key, value;
		// basket ID to asset name map
		var basketIdToAssetMap = {};
		for (i = 0; i < jsonData.basketEntities.length; i++) {
			key = jsonData.basketEntities[i].id;
			value = jsonData.basketEntities[i].name;
			basketIdToAssetMap[key] = value;
		}
		
		// security ID to basket ID map
		var secIdToBasketIdMap = {};
		for (i = 0; i < jsonData.securities.length; i++) {
			key = jsonData.securities[i].id;
			value = jsonData.securities[i].basketId;
			secIdToBasketIdMap[key] = value;
		}
		
		// security ID to security symbol map
		var secIdToSymbolMap = {};
		for (i = 0; i < jsonData.securities.length; i++) {
			key = jsonData.securities[i].id;
			value = jsonData.securities[i].symbol;
			secIdToSymbolMap[key] = value;
		}
		
		var result = "";

		// header
		result = result + "Transaction ID,";
		result = result + "Basket ID,";
		result = result + "Asset,";
		result = result + "Security ID,";
		result = result + "Security Symbol,";
		result = result + "Transaction Date,";
		result = result + "Transaction Type,";
		result = result + "Shares,";
		result = result + "Price\n";
		
		for (i = 0; i < jsonData.transactions.length; i++) {
			var t = jsonData.transactions[i];

			result = result + t.id + ",";
			result = result + secIdToBasketIdMap[t.secId] + ",";
			result = result + basketIdToAssetMap[secIdToBasketIdMap[t.secId]] + ",";
			result = result + t.secId + ",";
			result = result + secIdToSymbolMap[t.secId] + ",";
			result = result + t.tranDate + ",";
			result = result + t.type + ",";
			result = result + t.shares + ",";
			result = result + t.price + "\n";
		}
		
		return result;
	};
	
});

