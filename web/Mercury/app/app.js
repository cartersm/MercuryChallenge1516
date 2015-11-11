'use strict';

// Declare app level module which depends on views, and components
angular.module('Mercury', [
    'ngRoute',
    'Mercury.login',
    'Mercury.home',
    'Mercury.history'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.otherwise({redirectTo: '/login'});
    }])
    .controller('BlogNavCtrl', ['$scope', '$location', "CommonProp",
        function ($scope, $location, CommonProp) {
            $scope.isActive = function (viewLocation) {
                return viewLocation === $location.path();
            };

            $scope.logout = function () {
                CommonProp.logoutUser();
            };

            $scope.hasAuth = function () {
                var isAuthed = !(CommonProp.getUser() === '');
                if (isAuthed) {
                    // the navbar is forcibly hidden by default so that
                    //     it doesn't appear on initial load.
                    // Remove the 'hidden' class so ngShow can take over.
                    $("#primary-navbar").removeClass("hidden");
                }
                return isAuthed;
            }
        }
    ])
    .service('CommonProp', ["$window", "$firebaseAuth",
        function ($window, $firebaseAuth) {
            var user = "";
            var firebase = new Firebase("https://mercury-robotics-16.firebaseio.com");
            var authObj = $firebaseAuth(firebase);

            return {
                getUser: function () {
                    return user;
                },
                setUser: function (value) {
                    user = value;
                },
                logoutUser: function () {
                    authObj.$unauth();
                    console.log("Logout complete");
                    $window.location.reload();
                }
            }
        }]);
