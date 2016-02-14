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
    }]);