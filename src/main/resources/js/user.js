//Navigation Controller (user session, page redirect)
app.controller("navCtrl", function($rootScope, $scope, $http, $location) {
	$scope.LOGIN_MODE_SIGNIN = 1;
	$scope.LOGIN_MODE_CHGPASSWORD = 2;
	$scope.LOGIN_MODE_CREATE = 3;
	
	$rootScope.authSuccess = false;
	$rootScope.authSession = null;
	
	$scope.authMsg = null;
	$scope.createMsg = null;
	
	$scope.loginMode = $scope.LOGIN_MODE_SIGNIN;
	$scope.loginUsername = null;
	$scope.loginPassword = null;
	$scope.loginChgPassword = null;
	$scope.loginChgPasswordRetype = null;
	$scope.loginNewUsername = null;
	$scope.loginNewPassword = null;
	$scope.loginNewPasswordRetype = null;
	
	
	$scope.selectLoginMode = function(newMode) {
		$scope.authMsg = null;
		$scope.createMsg = null;
		$scope.loginUsername = null;
		$scope.loginPassword = null;
		$scope.loginChgPassword = null;
		$scope.loginChgPasswordRetype = null;
		$scope.loginNewUsername = null;
		$scope.loginNewPassword = null;
		$scope.loginNewPasswordRetype = null;
		
		$scope.loginMode = newMode;
	};
	
	$scope.loginSubmit = function() {
		if ($scope.loginMode == $scope.LOGIN_MODE_SIGNIN) {
			$scope.login();
		}
		else if ($scope.loginMode == $scope.LOGIN_MODE_CHGPASSWORD) {
			$scope.changePassword();
		}
	};
	
	//
	// Login
	//
	$scope.login = function() {
		if ($scope.loginUsername == null || $scope.loginUsername.trim() == "" ||
				$scope.loginPassword == null || $scope.loginPassword.trim() == "") {
			$scope.authMsg = "Username and password must be non-blank. Passwords must match.";
			return;
		}
		
		var data = {
			    username: $scope.loginUsername,
			    wp1: $scope.loginPassword,
			    wp2: null
		};
	
		$http({
		    method: "POST",
		    url: "api/v1/login",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					if (response.data != null && response.data.username != null && response.data.valid) {
						$rootScope.authSuccess = true;
						$rootScope.authSession = response.data;
						$scope.authMsg = null;
						$scope.loginUsername = null;
						$scope.loginPassword = null;
						$location.path("/dashboard");
					}
					else {
						$rootScope.authSuccess = false;
						$rootScope.authSession = null;
						$scope.authMsg = "Login failed";
						$scope.loginUsername = null;
						$scope.loginPassword = null;
					}
				},
				// Error response
				function errorCallback(response) {
					$rootScope.authSuccess = false;
					$rootScope.authSession = null;
					$location.path("/login");
					$scope.authMsg = "Login failed";
					$scope.loginUsername = null;
					$scope.loginPassword = null;
				}
		);
	};
	
	//
	// Change Password
	//
	$scope.changePassword = function() {
		if ($scope.loginUsername == null || $scope.loginUsername.trim() == "" ||
				$scope.loginPassword == null || $scope.loginPassword.trim() == "" ||
				$scope.loginChgPassword == null || $scope.loginChgPassword.trim() == "" ||
				$scope.loginChgPasswordRetype == null || $scope.loginChgPasswordRetype.trim() == "" ||
				($scope.loginChgPassword != $scope.loginChgPasswordRetype)) {
			$scope.authMsg = "Username and password must be non-blank. New passwords must match.";
			return;
		}

		var data = {
			    username: $scope.loginUsername,
			    wp1: $scope.loginPassword,
			    wp2: $scope.loginChgPassword
		};
	
		$http({
		    method: "PUT",
		    url: "api/v1/user/" + $scope.loginUsername,
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					if (response.data != null && response.data.username != null) {
						$scope.authMsg = "Password changed successfully";
					}
					else {
						$scope.authMsg = "Password change failed. Make sure you entered the correct existing password.";
					}
					$scope.loginUsername = null;
					$scope.loginPassword = null;
					$scope.loginChgPassword = null;
					$scope.loginChgPasswordRetype = null;
				},
				// Error response
				function errorCallback(response) {
					$scope.authMsg = "Password change failed";
					$scope.loginUsername = null;
					$scope.loginPassword = null;
					$scope.loginChgPassword = null;
					$scope.loginChgPasswordRetype = null;
				}
		);	
	};
	
	//
	// Create New User
	//
	$scope.createUsername = function() {
		if ($scope.loginNewUsername == null || $scope.loginNewUsername.trim() == "" ||
				$scope.loginNewPassword == null || $scope.loginNewPassword.trim() == "" ||
				$scope.loginNewPasswordRetype == null || $scope.loginNewPasswordRetype.trim() == "" ||
				$scope.loginNewPassword != $scope.loginNewPasswordRetype) {
			$scope.createMsg = "Username and password must be non-blank";
			return;
		}

		var data = {
			    username: $scope.loginNewUsername,
			    wp1: $scope.loginNewPassword,
			    wp2: null
		};
	
		$http({
		    method: "POST",
		    url: "api/v1/user",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					if (response.data != null && response.data.username != null) {
						$scope.createMsg = "User created successfully";
					}
					else {
						$scope.createMsg = "User creation failed. Make sure the user does not already exist.";
					}
					$scope.loginNewUsername = null;
					$scope.loginNewPassword = null;
					$scope.loginNewPasswordRetype = null;
				},
				// Error response
				function errorCallback(response) {
					$scope.createMsg = "User creation failed";
					$scope.loginNewUsername = null;
					$scope.loginNewPassword = null;
					$scope.loginNewPasswordRetype = null;
				}
		);	
	};
	
	//
	// Logout
	//
	$rootScope.logout = function() {
		var data = $rootScope.authSession;
	
		$http({
		    method: "POST",
		    url: "api/v1/logout",
		    data: data,
		    headers: {"Content-Type": "application/json"}
		}).then(
				// Success response
				function successCallback(response) {
					$rootScope.authSuccess = false;
					$rootScope.authSession = null;
					$location.path("/login");
				},
				// Error response
				function errorCallback(response) {
					$rootScope.authSuccess = false;
					$rootScope.authSession = null;
					$location.path("/login");
				}
		);
	};
	
});

