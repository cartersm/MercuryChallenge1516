'use strict';

// Declare app level module which depends on views, and components
angular.module('Mercury', [
    'ngRoute',
    'Mercury.login',
    'Mercury.home',
    'Mercury.history',
    'Mercury.services'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.otherwise({redirectTo: '/login'});
    }])
    .controller('RootCtrl', [
        '$scope',
        '$location',
        'AuthService',
        function ($scope, $location, AuthService) {
            $scope.isActive = function (viewLocation) {
                return viewLocation === $location.path();
            };

            if ($location.path() !== '/home') {
                $('#default-link').removeClass('active');
            }

            AuthService.checkAuth(function () {
                // the navbar is forcibly hidden by default so that
                //     it doesn't appear on initial load.
                // Remove the 'hidden' class so ngShow can take over.
                $('#page-header').removeClass('hidden');
                $('#nav-bar').removeClass('hidden');
                console.log(AuthService.getUser());
                $scope.username = AuthService.getUser();
            });

            $scope.hasAuth = function () {
                return AuthService.hasAuth();
            }

            $scope.logout = function () {
                AuthService.logoutUser();
            };
        }
    ]);