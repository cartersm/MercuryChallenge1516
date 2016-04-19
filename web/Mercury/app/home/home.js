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
        'hotkeys',
        function ($scope, $firebaseAuth, $firebaseArray, $location, Auth, hotkeys) {
            Auth.checkAuth(function () {
                if (!Auth.hasAuth()) {
                    $location.path('/login');
                }
            });
            $scope.username = Auth.getUser();
            $scope.motor = {
                distance: 0,
                angle: 0,
                serpentine: false,
                seesaw: false
            };

            $scope.gripper = {
                launch: false,
                location: '',
                position: ''
            };

            $scope.led = {
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
                        seesaw: false,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false,
                    seesaw: false
                };
            };

            $scope.sendSerpentineCommand = function () {
                console.log('sending serpentine command');
                firebase
                    .child('motorCommands')
                    .push({
                        distance: 0,
                        angle: 0,
                        serpentine: true,
                        seesaw: false,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false,
                    seesaw: false
                };
            };

            $scope.sendSeesawCommand = function () {
                console.log('sending Seesaw command');
                firebase
                    .child('motorCommands')
                    .push({
                        distance: 0,
                        angle: 0,
                        serpentine: false,
                        seesaw: true,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false,
                    seesaw: false
                };
            };

            $scope.sendMotorCommand = function (cmd) {
                console.log('sending motor command (' +
                    cmd.distance + 'in, ' +
                    cmd.angle + ' deg, ' +
                    cmd.serpentine + ', ' +
                    cmd.seesaw + ')');
                firebase.child('motorCommands')
                    .push({
                        distance: cmd.distance,
                        angle: cmd.angle,
                        serpentine: cmd.serpentine,
                        seesaw: cmd.seesaw,
                        timestamp: new Date().getTime()
                    });
                $scope.motor = {
                    distance: 0,
                    angle: 0,
                    serpentine: false,
                    seesaw: false
                };
            };

            $scope.sendMotorCommandIfValid = function (cmd) {
                if (!$scope.isMotorCommandInvalid(cmd)) {
                    $scope.sendMotorCommand(cmd);
                }
            }

            $scope.sendGripperCommand = function (cmd) {
                console.log('sending gripper command (' +
                    cmd.launch + ', ' +
                    cmd.location + ', ' +
                    cmd.position + ')');
                firebase.child('gripperLauncherCommands')
                    .push({
                        launch: false,
                        location: validateLocation(cmd.location),
                        position: validatePosition(cmd.position),
                        timestamp: new Date().getTime()
                    });
                $scope.gripper = {
                    launch: false,
                    location: '',
                    position: ''
                };
            };

            var validateLocation = function (location) {
                if (location && (location.toLowerCase() === 'lowered' || location.toLowerCase() === 'raised')) {
                    return location;
                } else {
                    return $scope.latestGripperCommands[0].location;
                }
            };

            var validatePosition = function (position) {
                if (position && (position.toLowerCase() === 'open' || position.toLowerCase() === 'closed')) {
                    return position;
                } else {
                    return $scope.latestGripperCommands[0].position;
                }
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

            $scope.canLaunch = function () {
                if ($scope.latestGripperCommands) {
                    var cmd = $scope.latestGripperCommands[0];
                    return cmd.location.toLowerCase() === 'lowered' ||
                        cmd.position.toLowerCase() === 'open';
                }
                return false;
            };

            $scope.isMotorCommandInvalid = function (motor) {
                return (!motor.distance && motor.distance !== 0) || (!motor.angle && motor.angle !== 0);
            };

            $scope.sendLedCommand = function (cmd) {
                console.log('sending LED command (' + cmd + ')');
                firebase.child('ledCommands')
                    .push({
                        status: cmd,
                        timestamp: new Date().getTime()
                    });
                $scope.led = {
                    status: ''
                };
            };

            // Hotkeys
            hotkeys.bindTo($scope)
                // forward
                .add({
                    combo: 'up',
                    description: 'Nudge forward (1 inch)',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 1,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // backward
                .add({
                    combo: 'down',
                    description: 'Nudge backward (1 inch)',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: -1,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // left
                .add({
                    combo: 'left',
                    description: 'Nudge left (5 degrees)',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: 5,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // right
                .add({
                    combo: 'right',
                    description: 'Nudge right (5 degrees)',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: -5,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // stop
                .add({
                    combo: 's',
                    description: 'E-STOP',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // 1 foot
                .add({
                    combo: '1',
                    description: 'Forward 1 foot',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 12,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // 6 feet
                .add({
                    combo: '6',
                    description: 'Forward 6 feet',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 72,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // 45 feet
                .add({
                    combo: '4',
                    description: 'Forward 45 feet',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 540,
                            angle: 0,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // Turn 180
                .add({
                    combo: 't',
                    description: 'Turn 180 degrees',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: 180,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // Turn Left 90
                .add({
                    combo: 'shift+left',
                    description: 'Turn Left 90 degrees',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: 90,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
                // Turn Right 90
                .add({
                    combo: 'shift+right',
                    description: 'Turn Right 90 degrees',
                    callback: function () {
                        $scope.sendMotorCommand({
                            distance: 0,
                            angle: -90,
                            serpentine: false,
                            seesaw: false
                        });
                    }
                })
        }]);
