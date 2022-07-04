/*function connect() {
  var socket = new SockJS('/gs-guide-websocket');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/greetings', function(greeting) {
      showGreeting(JSON.parse(greeting.body).content);
    });
  });
}*/

/*** PICTO ****/
var Picto = new Object();

Picto.pictoFile = null;
Picto.socket = null;
Picto.stompClient = null;
Picto.treeView = null;
Picto.selectedPath = null;
Picto.viewContents = new Map();

Picto.convertToPictoRequest = function(pictoFile, type, message) {
  return JSON.stringify(
    {
      "pictoFile": pictoFile,
      "type": type,
      "code": message
    }
  );
}

Picto.executeCode = function() {
  console.log("Get TreeView ...");
  //this.stompClient.send("/app/treeview", {}, this.convertToPictoRequest(this.pictoFile, "TreeView", editor.getValue()));
  this.stompClient.send("/app/treeview", {}, this.convertToPictoRequest(this.pictoFile, "TreeView", ""));
  console.log("Code sent.");
}

Picto.projectTree = function() {
  console.log("Get Project Tree ...");
  this.stompClient.send("/app/projecttree", {}, this.convertToPictoRequest(this.pictoFile, "ProjectTree", ""));
  console.log("Code sent.");
}

Picto.getTreeView = function() {
  console.log("Get Treeview ...");
  this.stompClient.send("/app/treeview", {}, this.convertToPictoRequest(this.pictoFile, "TreeView", ""));
  console.log("Code sent.");
}

Picto.openFile = function(filename) {
  console.log("Open File ...");
  this.stompClient.send("/app/treeview", {}, this.convertToPictoRequest(this.pictoFile, "TreeView", filename));
  //this.stompClient.send("/app/openfile", {}, this.convertToPictoRequest(this.pictoFile, "OpenFile", filename));
  console.log("Code sent.");
}

Picto.displayResult = function(message) {
  $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

Picto.createTree = function(treeView) {
  var tree = [];
  path = '';
  tree = Picto.recursiveTree(tree, treeView, path);
  return tree;
}

Picto.recursiveTree = function(tree, treeView, path) {
  for (key in treeView.children) {
    var child = treeView.children[key];
    var object = {};
    tree.push(object);
    object['text'] = child['name'];
    object['state'] = { "opened": true };
    object['children'] = [];
    var p = path + "/" + object['text'];
    if (Picto.selectedPath == null)
      Picto.selectedPath = p;
    Picto.viewContents.set(p, child.content);
    Picto.recursiveTree(object['children'], child, p);
  }
  return tree;
}

Picto.getSelectedViewPath = function(data) {
  var path = '';
  var node = data.instance.get_node(data.selected[0]);
  path = path + node.text;
  while (node.parent != '#') {
    path = '/' + path;
    var parent = data.instance.get_node(node.parent);
    path = parent.text + path;
    node = parent;
  }
  path = '/' + path;
  return path;
}

Picto.render = function(path) {
  var container = document.getElementById("visualization");
  container.innerHTML = '';
  var fragment;
  var viewContent = Picto.viewContents.get(path);
  if (viewContent.format == 'svg') {
    var text = viewContent.text;
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(text, "text/xml");
    fragment = xmlDoc.getElementsByTagName("svg")[0];
    container.appendChild(fragment);
  } else if (viewContent.format == 'html') {
    var text = viewContent.text;
    if (text.trim() == "") {
      text = "<body></body>";
    }
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(text, "text/xml");
    fragment = xmlDoc.getElementsByTagName("body")[0];
    container.innerHTML = fragment.innerHTML;
  } else if (viewContent.format == 'markdown') {
    var text = viewContent.text;
    console.log(text);
    var md = marked.parse(text);
    console.log(md);
    container.innerHTML = md;
  }
}


Picto.getView = function(event) {
  console.log("PICTO: receiving when an element is clicked");
  if (event.target.readyState == 4 && event.target.status == 200) {
    var container = document.getElementById("visualization");
    container.innerHTML = '';
    var response = JSON.parse(event.target.responseText);
    console.log(response);
    if (response.type == 'svg') {
      var text = response.content;
      var parser = new DOMParser();
      var xmlDoc = parser.parseFromString(text, "text/xml");
      fragment = xmlDoc.getElementsByTagName("svg")[0];
      container.appendChild(fragment);
    } else if (response.type == 'html') {
      var text = response.content;
      if (text.trim() == "") {
        text = "<body></body>";
      }
      var parser = new DOMParser();
      var xmlDoc = parser.parseFromString(text, "text/xml");
      fragment = xmlDoc.getElementsByTagName("body")[0];
      container.innerHTML = fragment.innerHTML;
    } else if (response.type == 'markdown') {
      var text = response.content;
      console.log(text);
      var md = marked.parse(text);
      console.log(md);
      container.innerHTML = md;
    }
  }
}

Picto.draw = function(label, uri) {
  console.log('PICTO: element clicked - ' + label + ", " + uri);

  var request = new XMLHttpRequest();
  request.addEventListener("load", Picto.getView);
  request.open("GET", "/pictojson" + uri);
  request.send();

  window.history.pushState(null, label, uri);

  return false;
}

Picto.connectToServer = function(pictoFile) {
  this.pictoFile = pictoFile;
  this.socket = new SockJS('/gs-guide-websocket');
  this.stompClient = Stomp.over(this.socket);
  this.stompClient.connect({}, function(frame) {
    //setConnected(true);
    console.log('PICTO - Connected to server: ' + frame);
    Picto.stompClient.subscribe('/topic/picto/' + Picto.pictoFile, function(message) {
      console.log("PICTO - Receiving messages ... ");
      //console.log(message);
      //console.log(message.body);
      var json = JSON.parse(message.body);
      console.log(json);
      
      // TreeView
      if (json.type == 'TreeView') {

        var treeView = JSON.parse(json.content);
        //console.log(json.content);
        //console.log(treeView);

        var jsTreeTree = Picto.createTree(treeView);

        console.log(jsTreeTree);

        var tree = $.jstree.reference("#tree");
        if (tree != null) {
          //var gigi = tree.get_node("#");
          //tree._model.data = null;
          //tree.clear_buffer();
          //tree.refresh();
          //tree.redraw();
          //tree.delete_node(gigi);
          //console.log(tree);
          tree.destroy();
          console.log("PICTO - Destroy");
        }

        var jsTreeConfig = {
          'core': {
            'data': jsTreeTree
          },
          "plugins": ["search"]
        };
        $('#tree').jstree(jsTreeConfig);

        //console.log(x);
        $('#tree').on("select_node.jstree", function(event, data) {
          if (data.selected.length) {
            var path = Picto.getSelectedViewPath(data);
            Picto.selectedPath = path;
            console.log(path);
            Picto.render(path);
          }
        });

        $('#tree').bind("ready.jstree", function(event) {
          /*if (selectedPath != null) {
            render(selectedPath);
          }*/
          console.log("PICTO - Ready");
        });

        $('#tree').bind("dblclick.jstree", function(event) {
          var text = event.target.outerText;
          console.log(text);
        });

        var to = false;
        $('#searchTree').keyup(function() {
          if (to) { clearTimeout(to); }
          to = setTimeout(function() {
            var v = $('#searchTree').val();
            console.log(v);
            $('#tree').jstree(true).search(v, false, true);
          }, 250);
        });



        Picto.render(Picto.selectedPath);

        /*for (var entry of viewContents.entries()) {
          console.log(entry);
        }*/
        /*for (var entry of viewContents.entries()) {
          xml = entry[1];
          break;
        }*/
      }

      // ProjectTree
      else if (json.type == 'ProjectTree') {
        var jsonTree = JSON.parse(json.content);
        console.log(jsonTree);
        console.log("");
      }

      /*//ProjectTree
      else if (json.type == 'ProjectTree') {
        //console.log(json.content);
        var jsonTree = JSON.parse(json.content);
        console.log(jsonTree);
        var x = {
          'core': {
            'data': jsonTree
          },
          "plugins": ["search"]
        };
        console.log(x);
        $('#project').jstree(x);

        $('#project').bind("dblclick.jstree", function(event) {
          var node = $(event.target).closest("li");
          if (node[0] != null) {
            var temp = node[0].innerText.split(/\r?\n/);
            if (temp.length == 1) {
              var filename = node[0].innerText;
              console.log(filename);
              openFile(filename);
            }
          }
        });

        var to = false;
        $('#searchText').keyup(function() {
          if (to) { clearTimeout(to); }
          to = setTimeout(function() {
            var v = $('#searchText').val();
            console.log(v);
            $('#project').jstree(true).search(v, false, true);
          }, 250);
        });
      }
      else if (json.type == 'OpenFile') {
        console.log(json.content);
        editor.setValue(json.content);
      }*/
    }
    );

    // get all files in a project
    // projectTree();
    // executeCode("");
    // Picto.getTreeView();
    // projectTree();

  });
}

Picto.updateFlexmiEditorSyntaxHighlighting = function(editor) {
  var val = editor.getSession().getValue();
  if ((val.trim() + "").startsWith("<")) {
    editor.getSession().setMode("ace/mode/xml");
  }
  else {
    editor.getSession().setMode("ace/mode/yaml");
  }
}
