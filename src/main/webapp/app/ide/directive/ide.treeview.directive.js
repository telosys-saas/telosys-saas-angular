'use strict';

angular.module('ide').directive('treeview', function () {
    return {
        scope: {
            data: '=',
            onaddfile: '&'
        },
        templateUrl: 'app/ide/directive/ide.treeview.directive.html',

        link: function ($scope, element, attrs) {
            function init() {
                console.log('data :', $scope.data);
                console.log('event:', $scope.events);
                $(element[0].children[0]).jstree({
                    'core': {
                        'data': [
                            $scope.data
                        ],
                        // so that create works
                        "check_callback": true
                    },
                    "types": {
                        "folder": {
                            "icon": "fa fa-folder-o"
                        },
                        "file": {
                            "icon": "fa fa-file-text-o"
                        }
                    },
                    "contextmenu": {
                        // Customize context menu items : http://stackoverflow.com/questions/21096141/jstree-and-context-menu-modify-items
                        "items": function (node) {
                            var tree = $(element[0].children[0]).jstree(true);
                            console.log(tree);
                            /*
                             separator_before - a boolean indicating if there should be a separator before this item
                             separator_after - a boolean indicating if there should be a separator after this item
                             _disabled - a boolean indicating if this action should be disabled
                             label - a string - the name of the action (could be a function returning a string)
                             action - a function to be executed if this item is chosen
                             icon - a string, can be a path to an icon or a className, if using an image that is in the current directory use a ./ prefix, otherwise it will be detected as a class
                             shortcut - keyCode which will trigger the action if the menu is open (for example 113 for rename, which equals F2)
                             shortcut_label - shortcut label (like for example F2 for rename)
                             */
                            console.log(node);
                            var items = {};

                            items.CreateFile = {
                                label: "Create file",
                                action: $scope.onCreateFile(node, tree)
                            };
                            items.CreateFolder = {
                                label: "Create folder",
                                action: $scope.onCreateFolder(node, tree)
                            };
                            return items;
                        }.bind(this)
                    },
                    "plugins": ["contextmenu", "types"]
                });
                // double click
                $(element[0].children[0]).bind("dblclick.jstree", function (event) {
                    console.log('double click');
                    workingFileCtrl.addFile('coucou');
                });
            }
            init();
            
            $scope.onCreateFile = function (nodeParent, tree) {
                return (function (obj) {
                    var node = {
                        type: 'file'
                    };
                    node = tree.create_node(nodeParent, node);
                    tree.edit(node, null, function (node, status) {
                        if (nodeParent.id == '@@_root_@@') {
                            var file = {
                                id: node.text,
                                name: node.text,
                                folderParentId: ''
                            };
                        } else {
                            var file = {
                                id: nodeParent.id + '/' + node.text,
                                name: node.text,
                                folderParentId: nodeParent.id
                            };
                        }
                        tree.set_id(node, file.id);
                        console.log('file created');
                        // Ajouter un controller dans la directive vers un service
                        if($scope.onaddfile) {
                            $scope.onaddfile(file);
                        }
                    });
                });
            };

            $scope.onCreateFolder = function (nodeParent, tree) {
                return (function (obj) {
                    var node = {
                        type: 'folder'
                    };
                    node = tree.create_node(nodeParent, node);
                    tree.edit(node, null, function (node, status) {
                        if (nodeParent.id == '@@_root_@@') {
                            var folder = {
                                id: node.text,
                                name: node.text,
                                folderParentId: ''
                            };
                        } else {
                            var folder = {
                                id: nodeParent.id + '/' + node.text,
                                name: node.text,
                                folderParentId: nodeParent.id
                            };
                        }
                        tree.set_id(node, folder.id);
                        console.log('folder created');
                        // Ajouter un controller dans la directive vers un service
                    });
                });
            };
        }
    };
});