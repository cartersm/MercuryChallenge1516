angular.module('Mercury.home', [
    'ngRoute',
    "firebase"
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/home', {
            templateUrl: 'home/home.html',
            controller: 'HomeCtrl'
        });
    }])
    .controller('HomeCtrl', ["$scope", "$firebaseAuth", "$firebaseArray", "$location", "CommonProp",
        function ($scope, $firebaseAuth, $firebaseArray, $location, CommonProp) {
            var firebase = new Firebase("https://mercury-robotics-16.firebaseio.com");
            var authObj = $firebaseAuth(firebase);
            if (authObj.$getAuth() === null) {
                $location.path("login");
            }
            if (CommonProp.getUser() == "") {
                CommonProp.setUser(authObj.$getAuth().password.email);
            }
            $scope.username = CommonProp.getUser();

            var motorQuery = firebase.child("motorCommands")
                .orderByChild("timestamp")
                .limitToLast(1);
            $firebaseArray(motorQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestMotorCommands = data;
                });

            var gripperQuery = firebase.child("gripperLauncherCommands")
                .orderByChild("timestamp")
                .limitToLast(1);
            $firebaseArray(gripperQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestGripperCommands = data;
                });

            var ledQuery = firebase.child("ledCommands")
                .orderByChild("timestamp")
                .limitToLast(1);
            $firebaseArray(ledQuery)
                .$loaded()
                .then(function (data) {
                    $scope.latestLedCommands = data;
                });

            $scope.status = "off";

            /* Sending Commands */
            $scope.sendStopCommand = function () {
                console.log("sending stop command");
                firebase
                    .child("motorCommands")
                    .push({
                        leftMotors: 0,
                        rightMotors: 0,
                        timestamp: new Date().getTime()
                    });
            };

            $scope.sendMotorCommand = function (leftPwm, rightPwm) {
                console.log("sending motor command (" + leftPwm + ", " + rightPwm + ")");
                firebase.child("motorCommands")
                    .push({
                        leftMotors: leftPwm,
                        rightMotors: rightPwm,
                        timestamp: new Date().getTime()
                    });
            };

            $scope.sendGripperCloseCommand = function () {
                console.log("sending gripper close command");
                firebase.child("gripperLauncherCommands")
                    .push({
                        angle: $scope.latestGripperCommands[0].angle,
                        position: "closed",
                        timestamp: new Date().getTime()
                    });
            };

            $scope.sendGripperOpenCommand = function () {
                console.log("sending gripper open command");
                firebase.child("gripperLauncherCommands")
                    .push({
                        angle: $scope.latestGripperCommands[0].angle,
                        position: "open",
                        timestamp: new Date().getTime()
                    });
            };

            $scope.sendGripperAngleCommand = function (degrees) {
                console.log("sending angle command (" + degrees + ")");
                firebase.child("gripperLauncherCommands")
                    .push({
                        angle: degrees,
                        position: $scope.latestGripperCommands[0].position,
                        timestamp: new Date().getTime()
                    });
            };

            $scope.sendLedCommand = function (ledNumber, status) {
                console.log("sending LED command (" + ledNumber + ", " + status + ")");
                firebase.child("ledCommands")
                    .push({
                        ledNumber: ledNumber,
                        status: status,
                        timestamp: new Date().getTime()
                    });
            };
        }]);