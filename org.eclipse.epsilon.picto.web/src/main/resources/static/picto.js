/*function connect() {
  var socket = new SockJS('/picto-web');
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
Picto.selectedPathOld = null;
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
    if (Picto.selectedPathOld == null)
      Picto.selectedPathOld = p;
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

Picto.render = function(view) {
  var container = document.getElementById("visualization");
  container.innerHTML = '';
  console.log(view);
  if (view.type == 'svg') {
    var text = view.content;
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(text, "text/xml");
    var svg = xmlDoc.getElementsByTagName("svg")[0];
    container.appendChild(svg);
    svgPanZoom(svg, {zoomEnabled: true,fit: true,center: true});
  } else if (view.type == 'html') {
    var text = view.content;
    if (text.trim() == "") {
      text = "<body></body>";
    }
    console.log(text);
    container.innerHTML = text;
    //var parser = new DOMParser();
    //var xmlDoc = parser.parseFromString(text, "text/xml");
    //fragment = xmlDoc.innerHTML;
    //fragment = xmlDoc.getElementsByTagName("body")[0];
    //container.innerHTML = fragment.innerHTML;
  } else if (view.type == 'markdown') {
    var text = view.content;
    console.log(text);
    var md = marked.parse(text);
    console.log(md);
    container.innerHTML = md;
  }
}

Picto.getView = function(event) {
  console.log("PICTO: receiving when an element is clicked");
  if (event.target.readyState == 4 && event.target.status == 200) {
    if (event.target.responseText == null || event.target.responseText == "") {
      return;
    } 
    var view = JSON.parse(event.target.responseText);
    Picto.render(view);
  }
}

Picto.draw = function(label, url) {
  console.log('PICTO: element clicked - ' + label + ", " + url);

  Picto.selectedPath = label.split('#')[1];

  var request = new XMLHttpRequest();
  request.addEventListener("load", Picto.getView);
  request.open("GET", "/pictojson" + url);
  request.send();

  window.history.pushState(null, label, url);

  return false;
}

Picto.connectToServer = function(pictoFile) {
  this.pictoFile = pictoFile;
  this.socket = new SockJS('/picto-web');
  this.stompClient = Stomp.over(this.socket);
  this.stompClient.connect({}, function(frame) {
    //setConnected(true);
    console.log('PICTO - Connected to server: ' + frame);
    Picto.stompClient.subscribe('/topic/picto/' + Picto.pictoFile, function(message) {
      console.log("PICTO - Receiving messages ... ");
      //console.log(message);
      //console.log(message.body);
      var view = JSON.parse(message.body);
      console.log(view);
      if (view.path != Picto.selectedPath) {
        return;
      }
      Picto.render(view);

    });
  }
  );
}


