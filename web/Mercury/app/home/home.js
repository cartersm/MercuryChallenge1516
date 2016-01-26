angular.module('Mercury.home', [
    'ngRoute',
    'firebase'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/home', {
            templateUrl: 'home/home.html',
            controller: 'HomeCtrl'
        });
    }])
    .controller('HomeCtrl', [
        '$scope',
        '$firebaseAuth',
        '$firebaseArray',
        '$location',
        'AuthService',
        function ($scope, $firebaseAuth, $firebaseArray, $location, AuthService) {
            AuthService.checkAuth(function () {
                if (!AuthService.hasAuth()) {
                    $location.path('/login');
                }
            });
            $scope.username = AuthService.getUser();

            var firebase = new Firebase('https://mercury-robotics-16.firebaseio.com');
            var motorQuery = firebase.child('motorCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(motorQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestMotorCommands = data;
                    $scope.motor = {
                        distance: 0,
                        angle: 0
                    };
                });

            var gripperQuery = firebase.child('gripperLauncherCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(gripperQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestGripperCommands = data;
                    $scope.gripper = {
                        launch: false,
                        location: '',
                        position: ''
                    };
                });

            var ledQuery = firebase.child('ledCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(ledQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestLedCommands = data;
                    $scope.led = {
                        ledNumber: 0,
                        status: ''
                    };
                });

            /* Sending Commands */
            $scope.sendStopCommand = function () {
                console.log('sending stop command');
                firebase
                    .child('motorCommands')
                    .push({
                        distance: 0,
                        angle: 0,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0
                };
            };

            $scope.sendMotorCommand = function (distance, angle) {
                console.log('sending motor command (' + distance + 'cm, ' + angle + 'deg)');
                firebase.child('motorCommands')
                    .push({
                        distance: distance,
                        angle: angle,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: '',
                    angle: ''
                };
            };

            $scope.sendGripperCommand = function (location, position) {
                console.log('sending gripper command (' + location + ', ' + position + ')');
                firebase.child('gripperLauncherCommands')
                    .push({
                        launch: false,
                        location: location,
                        position: position,
                        timestamp: new Date().getTime()
                    });
                $scope.gripper = {
                    launch: false,
                    location: '',
                    position: ''
                };
            };

            $scope.sendGripperLaunchCommand = function () {
                console.log('sending gripper launch command');
                firebase.child('gripperLauncherCommands')
                    .push({
                        launch: true,
                        location: $scope.latestGripperCommands[0].location,
                        position: $scope.latestGripperCommands[0].position,
                        timestamp: new Date().getTime()
                    });
                $scope.gripper = {
                    launch: false,
                    location: '',
                    position: ''
                };
            };

            $scope.isGripperRaised = function () {
                return $scope.latestGripperCommands[0].location.toLowerCase() === 'raised';
            };

            $scope.isMotorCommandInvalid = function (motor) {
                return (!motor.distance && motor.distance !== 0) || (!motor.angle && motor.angle !== 0);
            };

            $scope.sendLedCommand = function (ledNumber, status) {
                console.log('sending LED command (' + ledNumber + ', ' + status + ')');
                firebase.child('ledCommands')
                    .push({
                        ledNumber: ledNumber,
                        status: status,
                        timestamp: new Date().getTime()
                    });
                $scope.led = {
                    ledNumber: 0,
                    status: ''
                };
            };
        }]);
