var stompClient = null;

function setConnected(connected) {
  $("#connect").prop("disabled", connected);
  $("#disconnect").prop("disabled", !connected);
  if (connected) {
    $("#conversation").show();
  }
  else {
    $("#conversation").hide();
  }
  $("#greetings").html("");
}

function connect() {
  var socket = new SockJS('/gs-guide-websocket');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/greetings', function(greeting) {
      showGreeting(JSON.parse(greeting.body).content);
    });
  });
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  setConnected(false);
  console.log("Disconnected");
}

function sendName() {
  stompClient.send("/app/hello", {}, JSON.stringify({ 'name': $("#name").val() }));
}

function showGreeting(message) {
  $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function() {
  $("form").on('submit', function(e) {
    e.preventDefault();
  });
  $("#connect").click(function() { connect(); });
  $("#disconnect").click(function() { disconnect(); });
  $("#send").click(function() { sendName(); });
});


/*** PICTO ****/

var editor = null;

function convertToPictoRequest(type, message) {
  return JSON.stringify(
    {
      "type": type,
      "code": message
    }
  );
}

function executeCode() {
  console.log("Get TreeView ...");
  stompClient.send("/app/treeview", {}, convertToPictoRequest("TreeView", editor.getValue()));
  console.log("Code sent.");
}

function projectTree() {
  console.log("Get Project Tree ...");
  stompClient.send("/app/projecttree", {}, convertToPictoRequest("ProjectTree", ""));
  console.log("Code sent.");
}

function openFile(filename) {
  console.log("Open File ...");
  stompClient.send("/app/openfile", {}, convertToPictoRequest("OpenFile", filename));
  console.log("Code sent.");
}

function displayResult(message) {
  $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function connectToServer() {
  var socket = new SockJS('/gs-guide-websocket');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/picto', function(picto) {
      console.log(picto);
      console.log(picto.body);
      var json = JSON.parse(picto.body);

      if (json.type == 'TreeView') {
        var xml = json.content;
        // console.log(xml);
        var parser = new DOMParser();
        var xmlDoc = parser.parseFromString(xml, "text/xml");
        var svg = xmlDoc.getElementsByTagName("svg")[0];
        var svgStr = new XMLSerializer().serializeToString(svg);
        console.log(svgStr);

        var container = document.getElementById("visualization");
        container.innerHTML = svgStr;
      }
      else if (json.type == 'ProjectTree') {
        //console.log(json.content);
        var jsonTree = JSON.parse(json.content);
        console.log(jsonTree);
        var x = {
          'core': {
            'data': jsonTree
          },
           "plugins" : [ "search" ]
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
      }
    });

    // get all files in a project
    projectTree();

  });
}

function updateFlexmiEditorSyntaxHighlighting(editor) {
  var val = editor.getSession().getValue();
  if ((val.trim() + "").startsWith("<")) {
    editor.getSession().setMode("ace/mode/xml");
  }
  else {
    editor.getSession().setMode("ace/mode/yaml");
  }
}

connectToServer();

