'use strict';
angular.module('Mercury.services', [])
    .service('AuthService', [
        '$firebaseAuth',
        '$window',
        '$firebaseObject',
        '$location',
        function ($firebaseAuth, $window, $firebaseObject, $location) {
            var user = '';
            var isEmployee = false;
            var isAdmin = false;
            var firebase = new Firebase('https://mercury-robotics-16.firebaseio.com');
            var authObj = $firebaseAuth(firebase);

            this.checkAuth = function (onSuccess, onFailure) {
                var auth = authObj.$getAuth();
                if (auth === null) {
                    // We're not logged in
                    $location.path('/login');
                    if (typeof onFailure === 'function') onFailure('not logged in');
                } else {
                    if (typeof onSuccess === 'function') onSuccess();
                }
            }.bind(this);

            this.getUser = function () {
                return user;
            }.bind(this);

            this.setUser = function (value) {
                user = value;
            }.bind(this);

            this.logoutUser = function () {
                authObj.$unauth();
                isEmployee = false;
                isAdmin = false;
                console.log('Logout complete');
                // Force reload to hide the navbar
                $location.path('/login');
            }.bind(this);

            var auth = authObj.$getAuth();
            this.checkAuth(function () {
                user = auth.password.email;
            });

            this.getAuthObject = function () {
                return authObj;
            };

            this.hasAuth = function () {
                return authObj.$getAuth() !== null;
            }
        }
    ]);
