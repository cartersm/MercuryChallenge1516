angular.module('Mercury.history', [
    'ngRoute',
    'firebase'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/history', {
            templateUrl: 'history/history.html',
            controller: 'HistoryCtrl'
        });
    }])
    .controller('HistoryCtrl', [
        '$scope',
        '$firebaseAuth',
        '$firebaseArray',
        '$location',
        'Auth',
        function ($scope, $firebaseAuth, $firebaseArray, $location, Auth) {
            var firebase = new Firebase('https://mercury-robotics-16.firebaseio.com');
            Auth.checkAuth(function () {
                if (!Auth.hasAuth()) {
                    $location.path('/login');
                }
            });

            $scope.logout = function () {
                Auth.logoutUser();
            };

            var motorQuery = firebase.child('motorCommands')
                .orderByChild('timestamp')
                .limitToLast(10);
            $firebaseArray(motorQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestMotorCommands = data.slice().reverse();
                });

            var gripperQuery = firebase.child('gripperLauncherCommands')
                .orderByChild('timestamp')
                .limitToLast(10);
            $firebaseArray(gripperQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestGripperCommands = data.slice().reverse();
                });

            var ledQuery = firebase.child('ledCommands')
                .orderByChild('timestamp')
                .limitToLast(10);
            $firebaseArray(ledQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestLedCommands = data.slice().reverse();
                });
        }]);
