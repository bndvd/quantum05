// Charts Controller
app.controller("chartsCtrl", function($rootScope, $scope, $http) {
	
	$scope.CHART_ENUM_STD_GROWTH = 1;
	$scope.CHART_ENUM_SIM_TARGET = 2;

	$scope.chartToNameMap = {};
	$scope.chartToNameMap[$scope.CHART_ENUM_STD_GROWTH] = "stdgrowth";
	$scope.chartToNameMap[$scope.CHART_ENUM_SIM_TARGET] = "simtarget";
	
	$scope.chartToGraphIdMap = {};
	$scope.chartToGraphIdMap[$scope.CHART_ENUM_STD_GROWTH] = "graphIdStdGrowth";
	$scope.chartToGraphIdMap[$scope.CHART_ENUM_SIM_TARGET] = "graphIdSimTarget";
	
	$scope.chartToGraphLegendIdMap = {};
	$scope.chartToGraphLegendIdMap[$scope.CHART_ENUM_STD_GROWTH] = "graphIdStdGrowthLegend";
	$scope.chartToGraphLegendIdMap[$scope.CHART_ENUM_SIM_TARGET] = "graphIdSimTargetLegend";
	
	$scope.DATEAXIS_NAME = "Date";
	$scope.CHARTSERIES_PRINCIPAL_ID = 1;
	$scope.CHARTSERIES_BENCHMARK_ID = 2;
	$scope.CHARTSERIES_USERPORTFOLIO_ID = 3;
	$scope.CHARTSERIES_SIMTARGETPORTFOLIO_ID = 4;
	
	$scope.chartSeriesIdToNameMap = {};
	$scope.chartSeriesIdToNameMap[$scope.CHARTSERIES_PRINCIPAL_ID] = "Cash";
	$scope.chartSeriesIdToNameMap[$scope.CHARTSERIES_BENCHMARK_ID] = "Benchmark";
	$scope.chartSeriesIdToNameMap[$scope.CHARTSERIES_USERPORTFOLIO_ID] = $rootScope.authSession.username;
	$scope.chartSeriesIdToNameMap[$scope.CHARTSERIES_SIMTARGETPORTFOLIO_ID] = "Target Portfolio";

	$scope.graphMsgStdGrowth = null;
	$scope.graphMsgSimTarget = null;
	
	
	$scope.loadGraph = function(chartEnum, graphData) {
		var gStdGrowth = new Dygraph(
				document.getElementById($scope.chartToGraphIdMap[chartEnum]),
				graphData,
				{
					includeZero: true,
					colors: ["rgb(200,200,200)", "rgb(100,100,100)", "rgb(0,0,0)"],
					gridLineColor: "rgb(220,220,220)",
					axisLabelWidth: 80,
					axisLabelFontSize: 12,
					digitsAfterDecimal: 0,
					labelsSeparateLines: false,
					labelsDiv: $scope.chartToGraphLegendIdMap[chartEnum],
					hideOverlayOnMouseOut: false
				}
		);
	};
	
	//
	// Utility function to parse graph data and load graph
	// Returns true if data was loaded successfully; false otherwise
	//
	$scope.parseAndLoadGraphData = function(chartEnum, series) {
		var result = false;
		
		var i;
		var j;
		var validData = true;
		for (i = 0; i < series.length; i++) {
			if (series[i].points == null || series[i].points.length < 1) {
				validData = false;
				break;
			}
		}
		
		if (validData) {
			var gData = "";

			// dygraphs format:
			// 1st line: Date,Col2,Col3\n
			// 2nd line: date,ser1pt1,ser2pt1\n
			// 3rd line: date,ser1pt2,ser2pt2\n
			// ...
			
			gData = gData + $scope.DATEAXIS_NAME;
			for (i = 0; i < series.length; i++) {
				var seriesId = series[i].type;
				gData = gData + "," + $scope.chartSeriesIdToNameMap[seriesId];
			}
			gData = gData + "\n";
			
			for (i = 0; i < series[0].points.length; i++) {
				gData = gData + series[0].points[i].date;
				for (j = 0; j < series.length; j++) {
					gData = gData + "," + series[j].points[i].value;
				}
				gData = gData + "\n";
			}
			
			// load graph object
			$scope.loadGraph(chartEnum, gData);
			result = true;
		}
		
		return result;
	};
	
	//
	// Generate Graphs
	//
	$scope.generateGraphs = function() {
		
		// STD GROWTH GRAPH
		$scope.graphMsgStdGrowth = "Building Portfolio Benchmark Chart...";
		$http({
			  method: "GET",
			  url: "api/v1/chart/" + $scope.chartToNameMap[$scope.CHART_ENUM_STD_GROWTH]
			}).then(
				function successCallback(response) {
					// non-empty series data
					if (response.data != null && response.data.seriesList != null && 
									response.data.seriesList.length > 0) {
						var series = response.data.seriesList;
						
						var graphSuccess = $scope.parseAndLoadGraphData($scope.CHART_ENUM_STD_GROWTH, series);
						if (graphSuccess) {	
							$scope.graphMsgStdGrowth = "Portfolio vs Benchmark";
						}
						else {
							$scope.graphMsgStdGrowth = "Portfolio Benchmark Chart Not Available";
						}
					}
					else {
						$scope.graphMsgStdGrowth = "Portfolio Benchmark Chart Not Available";
					}
				},
				function errorCallback(response) {
					$scope.graphMsgStdGrowth = "Portfolio Benchmark Chart Not Available";
				}
		);
		
		// SIMULATED TARGET PORTFOLIO GRAPH
		$scope.graphMsgSimTarget = "Building Simulated Target Chart...";
		$http({
			  method: "GET",
			  url: "api/v1/chart/" + $scope.chartToNameMap[$scope.CHART_ENUM_SIM_TARGET]
			}).then(
				function successCallback(response) {
					// non-empty series data
					if (response.data != null && response.data.seriesList != null && 
									response.data.seriesList.length > 0) {
						var series = response.data.seriesList;
						
						var graphSuccess = $scope.parseAndLoadGraphData($scope.CHART_ENUM_SIM_TARGET, series);
						if (graphSuccess) {	
							$scope.graphMsgSimTarget = "Simulated Target Portfolio vs Benchmark";
						}
						else {
							$scope.graphMsgSimTarget = "Simulated Target Chart Not Available";
						}
					}
					else {
						$scope.graphMsgSimTarget = "Simulated Target Chart Not Available";
					}
				},
				function errorCallback(response) {
					$scope.graphMsgSimTarget = "Simulated Target Chart Not Available";
				}
		);
	};
	
	$scope.generateGraphs();
	
});
