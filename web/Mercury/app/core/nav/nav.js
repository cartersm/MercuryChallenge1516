angular.module('Mercury')
    .controller('NavCtrl', [
        '$scope',
        '$location',
        'Auth',
        function ($scope, $location, Auth) {
            $scope.isActive = function (viewLocation) {
                return viewLocation === $location.path();
            };

            if ($location.path() !== '/home') {
                $('#default-link').removeClass('active');
            }

            Auth.checkAuth(function () {
                // the navbar is forcibly hidden by default so that
                //     it doesn't appear on initial load.
                // Remove the 'hidden' class so ngShow can take over.
                $('#page-header').removeClass('hidden');
                $('#nav-bar').removeClass('hidden');
                $scope.username = Auth.getUser();
            });

            $scope.hasAuth = function () {
                return Auth.hasAuth();
            };

            $scope.logout = function () {
                Auth.logoutUser();
            };
        }
    ])