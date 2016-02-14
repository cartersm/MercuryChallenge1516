'use strict';
angular.module('Mercury')
    .service('Auth', [
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

            var self = this;

            this.checkAuth = function (onSuccess, onFailure) {
                var auth = authObj.$getAuth();
                if (auth === null || auth.expires <= Date.now() / 1000) {
                    if (auth) {
                        // The auth has expired
                        self.logoutUser();
                    }
                    // We're not logged in
                    $location.path('/login');
                    if (onFailure) onFailure('not logged in');
                } else {
                    if (onSuccess) onSuccess();
                }
            };

            this.getUser = function () {
                return user;
            };

            this.setUser = function (value) {
                user = value;
            };

            this.logoutUser = function () {
                authObj.$unauth();
                isEmployee = false;
                isAdmin = false;
                console.log('Logout complete');
                // Force reload to hide the navbar
                $location.path('/login');
            };

            var auth = authObj.$getAuth();
            this.checkAuth(function () {
                user = auth.password.email;
            });

            this.auth = function (username, password, callback) {
                authObj.$authWithPassword({
                    email: username,
                    password: password
                }).then(function (authData) {
                    callback(null, authData);
                }).catch(function (error) {
                    callback(error, null);
                });
            };

            this.hasAuth = function () {
                return authObj.$getAuth() !== null && auth.expires > Date.now() / 1000;
            };
        }
    ]);
