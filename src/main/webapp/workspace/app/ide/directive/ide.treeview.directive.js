'use strict';

/**
 * IDE files treeview
 */
angular.module('ide').directive('treeview', ['$uibModal', function ($uibModal) {
  return {
    scope: {
      data: '='
    },
    templateUrl: 'app/ide/directive/ide.treeview.directive.html',

    link: function ($scope, element, attrs) {

      $scope.events = $scope.data.events;

      /**
       * Create a file
       */
      $scope.createFile = function () {
        var nodeParent = $scope.data.selectedElement;
        if (nodeParent == null) {
          nodeParent = {
            id: '@@_root_@@'
          };
        }
        $scope.onCreateFile(nodeParent)();

      };

      /**
       * During file creation
       * @param nodeParent Node parent
       * @returns {Function}
       */
      $scope.onCreateFile = function (nodeParent) {
        return (function (obj) {
          // Modal window to create a new file
          $uibModal.open({
            templateUrl: 'app/modal/modal.createfile.html',
            controller: 'modalCtrl',
            resolve: {
              data: {
                nodeParent: nodeParent,
                project: $scope.data.project,
                openCreatedFile: function (fileId) {
                  $scope.data.events.refreshAll(function () {
                    var tree = $(element[0].children[1]).jstree();
                    tree.settings.core.data = $scope.data.tree;
                    tree.refresh();
                    $scope.data.events.onDoubleClickFile($scope.data, fileId);
                  });
                }
              }
            }
          });
        })
      };

      /**
       * Create a folder
       */
      $scope.createFolder = function () {
        console.log('create Folder');
        var nodeParent = $scope.data.selectedElement;
        if (nodeParent == null) {
          nodeParent = {
            id: '@@_root_@@'
          };
        }
        $scope.onCreateFolder(nodeParent)();
      };

      /**
       * During folder creation
       * @param nodeParent Node parent
       */
      $scope.onCreateFolder = function (nodeParent) {
        return (function (obj) {
          // Modal window to create a new file
          $uibModal.open({
            templateUrl: 'app/modal/modal.createfolder.html',
            controller: 'modalCtrl',
            resolve: {
              data: {
                nodeParent: nodeParent,
                project: $scope.data.project,
                refreshAll: $scope.refreshAll
              }
            }
          });
        })
      };

      /**
       * Manage the bundles for the current project
       */
      $scope.manageBundle = function () {
        $uibModal.open({
          templateUrl: 'app/modal/modal.managebundle.html',
          controller: 'modalCtrl',
          resolve: {
            data: {
              project: $scope.data.project,
              bundlesOfProject: $scope.data.bundlesOfProject,
              allBundles: $scope.data.allBundles,
              githubUserName: $scope.data.githubUserName,
              refreshAll: $scope.refreshAll,
              removeBundle: $scope.events.removeBundle
            }
          }
        });
      };

      /**
       * Manage the bundles for the current project
       */
      $scope.createEntity = function () {
        $uibModal.open({
          templateUrl: 'app/modal/modal.createentity.html',
          controller: 'modalCtrl',
          resolve: {
            data: {
              models: $scope.data.tree,
              project: $scope.data.project,
              selectedModel : $scope.data.selectedModel,
              openCreatedFile: function (fileId) {
                $scope.data.events.refreshAll(function () {
                  var tree = $(element[0].children[1]).jstree();
                  tree.settings.core.data = $scope.data.tree;
                  tree.refresh();
                  $scope.data.events.onDoubleClickFile($scope.data, fileId);
                });
              }
            }
          }
        });
      };

      /**
       * Open a modal window to create a new model
       */
      $scope.createModel = function () {
        $uibModal.open({
          templateUrl: 'app/modal/modal.createmodel.html',
          controller: 'modalCtrl',
          resolve: {
            data: {
              project: $scope.data.project,
              refreshAll: $scope.refreshAll
            }
          }
        });
      };

      /**
       * Collapse all elements in the treeview
       */
      $scope.collapseAll = function () {
        console.log('collapseAll');
        var tree = $(element[0].children[1]).jstree();
        if (tree) {
          tree.close_all();
        }
      };

      /**
       * Force a refresh the treeview
       */
      $scope.refreshAll = function () {
        console.log('refreshAll');
        var tree = $(element[0].children[1]).jstree();
        $scope.events.refreshAll(function () {
          tree.settings.core.data = $scope.data.tree;
          tree.refresh();
        });
      };


      /**
       * During file remove
       * @param node File node to remove
       * @param tree Treeview
       */
      $scope.onRemove = function (node, tree) {
        return (function (obj) {
          console.log('onRemove');
          var modalInstance = $uibModal.open({
            templateUrl: 'app/modal/modal.confirmremove.html',
            controller: 'modalCtrl',
            resolve: {
              data: {
                elementName: node.text
              }
            }
          });
          modalInstance.result.then(function () {
            tree.delete_node(node);
            if (node.type == 'model') {
              var folderId = 'TelosysTools/' + node.text + '_model';
              var fileModelId = 'TelosysTools/' + node.text + '.model';
              $scope.events.onDeleteFolder($scope.data, folderId);
              $scope.events.onDeleteFile($scope.data, fileModelId);
            }
            if (node.type == 'bundle') {
              $scope.events.removeBundle(node.text);
            }
            if (node.type == 'folder') {
              var folderId = node.id;
              $scope.events.onDeleteFolder($scope.data, folderId)
            }
            if (node.type == 'file' || node.type == 'entity') {
              var fileId = node.id;
              $scope.events.onDeleteFile($scope.data, fileId);
            }
            $scope.refreshAll();
          })
        })
      };

      /**
       * Delete the selected element (folder or file) in the treeview
       */
      $scope.deleteSelectedElement = function () {
        var elementToDelete = $scope.data.selectedElement;
        var tree = $(element[0].children[1]).jstree();
        console.log('deleteSelectedElement', $scope.data.selectedElement);
        if (elementToDelete && elementToDelete != null && elementToDelete.id != '@@_root_@@') {
            $scope.onRemove(elementToDelete, tree)();
            $scope.data.selectedElement = null;
            console.log('deleteSelectedElement',$scope.data.selectedElement);
        }
      };

      /**
       * Treeview initialization
       */
      function init() {
        console.log('init treeview', $scope.data.name, $scope.data.tree);
        $(element[0].children[1]).jstree({
          'core': {
            'data': $scope.data.tree,
            // so that create works
            "check_callback": true
          },
          "types": {
            "default": {
              "icon": "fa fa-exclamation"
            },
            "folder": {
              "icon": "fa fa-folder-o"
            },
            "file": {
              "icon": "fa fa-file-text-o"
            },
            "bundle": {
              "icon": "fa fa-archive"
            },
            "model": {
              "icon": "fa fa-cubes"
            },
            "entity": {
              "icon": "fa fa-cube"
            }
          },
          "contextmenu": {
            // Customize context menu items : http://stackoverflow.com/questions/21096141/jstree-and-context-menu-modify-items
            "items": function (node) {
              var tree = $(element[0].children[1]).jstree(true);
              var items = {};
              if ($scope.data.name == 'files' && node.id == '@@_root_@@') {
                items.CreateFile = {
                  label: "Create file",
                  action: $scope.onCreateFile(node)
                };
                items.CreateFolder = {
                  label: "Create folder",
                  action: $scope.onCreateFolder(node)
                };

              } else {
                if (node.type == 'folder' || node.type == 'bundle') {
                  items.CreateFile = {
                    label: "Create file",
                    action: $scope.onCreateFile(node)
                  };
                  items.CreateFolder = {
                    label: "Create folder",
                    action: $scope.onCreateFolder(node)
                  };
                }
                if (node.type == 'folder') {
                  items.RemoveFolder = {
                    label: "Remove folder",
                    action: $scope.onRemove(node, tree)
                  }
                }
                if (node.type == 'bundle') {
                  items.RemoveFolder = {
                    label: "Remove bundle",
                    action: $scope.onRemove(node, tree)
                  }
                }
                if (node.type == 'file') {
                  items.RemoveFile = {
                    label: "Remove file ",
                    action: $scope.onRemove(node, tree)
                  }
                }
                if (node.type == 'model') {
                  items.CreateFolder = {
                    label: "Create entity",
                    action: $scope.createEntity
                  };
                  items.RemoveFolder = {
                    label: "Remove model",
                    action: $scope.onRemove(node, tree)
                  }
                }
                if (node.type == 'entity') {
                  items.RemoveFolder = {
                    label: "Remove entity",
                    action: $scope.onRemove(node, tree)
                  }
                }
              }
              return items;
            }.bind(this)
          },
          "plugins": ["contextmenu", "types"]
        });

        /** Functions to detect one click and double click on one node in the treeview */
        $(element[0].children[1]).bind("activate_node.jstree", function (e, data) {
          $scope.data.selectedElement = data.node;
          console.log('treeview one click', $scope.data.selectedElement);
          if(data.node.type == 'model'){
            $scope.data.selectedModel = data.node.text;
          }
          if(data.node.type == 'entity'){
            $scope.data.selectedModel = $scope.data.allFiles[$scope.data.selectedElement.id].modelName;
          }
        }.bind(this));

        /** Functions to detect double click and double click on one node in the treeview */
        $(element[0].children[1]).bind("dblclick.jstree", function () {
          console.log('treeview double click', $scope.data.selectedElement);
          if ($scope.events.onDoubleClickFile) {
            var file = $scope.data.allFiles[$scope.data.selectedElement.id];
            if (file) {
              $scope.events.onDoubleClickFile($scope.data, file.id);
            }
          }
        }.bind(this));
      }

      init();
    }
  }
}])
;