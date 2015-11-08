'use strict';

angular.module('Mercury.login', [
    'ngRoute',
    "firebase"
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/login', {
            templateUrl: 'login/login.html',
            controller: 'LoginCtrl'
        });
    }])
    .controller('LoginCtrl', ["$scope", "$firebaseAuth", "$location", "CommonProp",
        function ($scope, $firebaseAuth, $location, CommonProp) {
            var firebase = new Firebase("https://mercury-robotics-16.firebaseio.com");
            var authObj = $firebaseAuth(firebase);
            $scope.user = {};

            if (authObj.$getAuth() !== null) {
                $location.path("home");
            }

            $scope.SignIn = function (event) {
                event.preventDefault();
                var username = $scope.user.email;
                var password = $scope.user.password;

                authObj.$authWithPassword({
                    email: username,
                    password: password
                }).then(function (authData) {
                    console.log("Logged in as: " + authData.uid);
                    $location.path("home");
                    CommonProp.setUser($scope.user.email);
                }).catch(function (error) {
                    console.error("Authentication failed: ", error);
                });

            }
        }]);