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
        'Auth',
        function ($scope, $firebaseAuth, $firebaseArray, $location, Auth) {
            Auth.checkAuth(function () {
                if (!Auth.hasAuth()) {
                    $location.path('/login');
                }
            });
            $scope.username = Auth.getUser();
            $scope.motor = {
                distance: 0,
                angle: 0,
                serpentine: false
            };

            $scope.gripper = {
                launch: false,
                location: '',
                position: ''
            };

            $scope.led = {
                ledNumber: 0,
                status: ''
            };

            var firebase = new Firebase('https://mercury-robotics-16.firebaseio.com');
            var motorQuery = firebase.child('motorCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(motorQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestMotorCommands = data;
                });

            var gripperQuery = firebase.child('gripperLauncherCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(gripperQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestGripperCommands = data;
                });

            var ledQuery = firebase.child('ledCommands')
                .orderByChild('timestamp')
                .limitToLast(1);
            $firebaseArray(ledQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestLedCommands = data;
                });

            /* Sending Commands */
            $scope.sendStopCommand = function () {
                console.log('sending stop command');
                firebase
                    .child('motorCommands')
                    .push({
                        distance: 0,
                        angle: 0,
                        serpentine: false,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false
                };
            };

            $scope.sendMotorCommand = function (cmd) {
                console.log('sending motor command (' +
                    cmd.distance + 'in, ' +
                    cmd.angle + ' deg, ' +
                    cmd.serpentine + ')');
                firebase.child('motorCommands')
                    .push({
                        distance: cmd.distance,
                        angle: cmd.angle,
                        serpentine: cmd.serpentine,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false
                };
            };

            $scope.sendGripperCommand = function (cmd) {
                console.log('sending gripper command (' +
                    cmd.launch + ', ' +
                    cmd.location + ', ' +
                    cmd.position + ')');
                firebase.child('gripperLauncherCommands')
                    .push({
                        launch: false,
                        location: cmd.location,
                        position: cmd.position,
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
                return $scope.latestGripperCommands &&
                    $scope.latestGripperCommands[0].location.toLowerCase() === 'raised';
            };

            $scope.isMotorCommandInvalid = function (motor) {
                return (!motor.distance && motor.distance !== 0) || (!motor.angle && motor.angle !== 0);
            };

            $scope.sendLedCommand = function (cmd) {
                console.log('sending LED command (' + cmd.ledNumber + ', ' + cmd.status + ')');
                firebase.child('ledCommands')
                    .push({
                        ledNumber: cmd.ledNumber,
                        status: cmd.status,
                        timestamp: new Date().getTime()
                    });
                $scope.led = {
                    ledNumber: 0,
                    status: ''
                };
            };
        }]);
