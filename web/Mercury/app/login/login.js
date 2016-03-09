'use strict';

angular.module('Mercury.login', [
    'ngRoute',
    'firebase'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/login', {
            templateUrl: 'login/login.html',
            controller: 'LoginCtrl'
        });
    }])
    .controller('LoginCtrl', [
        '$scope',
        '$firebaseAuth',
        '$location',
        '$window',
        'Auth',
        function ($scope, $firebaseAuth, $location, $window, Auth) {
            $scope.signinFailed = false;
            Auth.checkAuth(function () {
                $window.location.reload();
                $location.path('/home');
            });

            $scope.user = {};
            $scope.emailSuffix = '@rose-hulman.edu';

            $scope.SignIn = function (event) {
                event.preventDefault();
                var username = $scope.user.email + $scope.emailSuffix;
                var password = $scope.user.password;

                Auth.auth(username, password,
                    function (err, authData) {
                        if (err) {
                            console.error('Authentication failed: ', err);
                            $scope.signinFailed = true;
                            return;
                        }
                        Auth.checkAuth(function () {
                            $scope.signinFailed = false;
                            Auth.setUser(authData.password.email);
                            $('#page-header').removeClass('hidden');
                            $('#nav-bar').removeClass('hidden');
                            $window.location.reload();
                            $location.path('/employees');
                        }, function (error) {
                            console.warn(error);
                            $scope.isEmployee = false;
                        });
                    });
            };
        }
    ]);
