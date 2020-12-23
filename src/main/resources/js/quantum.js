// Main Quantum Angular app
var app = angular.module("qApp", ["ngRoute"]);

// Routing
app.config(function($routeProvider, $httpProvider) {
    $routeProvider
    .when("/", {
        templateUrl : "login.html",
        controller : "navCtrl"
    })
    .when("/login", {
        templateUrl : "login.html",
        controller : "navCtrl"
    })
    .when("/dashboard", {
        templateUrl : "pages/dashboard.html",
        controller : "dashboardCtrl"
    })
    .when("/charts", {
        templateUrl : "pages/charts.html",
        controller : "chartsCtrl"
    })
    .when("/transactions", {
        templateUrl : "pages/transactions.html",
        controller : "transactionsCtrl"
    })
    .when("/manage", {
        templateUrl : "pages/manage.html",
        controller : "manageCtrl"
    })
    .when("/help", {
        templateUrl : "pages/help.html",
        controller : "helpCtrl"
    })
    .otherwise("/");
    
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    
});

