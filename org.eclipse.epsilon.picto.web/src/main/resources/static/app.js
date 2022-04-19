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

function editorsToJson() {
  return JSON.stringify(
    {
      "code": editor.getValue()
    }
  );
}

function executeCode() {
  console.log("Execute Code ...");
  stompClient.send("/app/picto", {}, editorsToJson());
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
      var xml = JSON.parse(picto.body).content;
      // console.log(xml);
      var parser = new DOMParser();
      var xmlDoc = parser.parseFromString(xml, "text/xml");
      var svg = xmlDoc.getElementsByTagName("svg")[0];
      var svgStr = new XMLSerializer().serializeToString(svg);
      console.log(svgStr);
      // console.log(container.innerHTML);

      var container = document.getElementById("visualization");
      container.innerHTML = svgStr;
      // '<svg height="30" width="200">' +
      // '<text x="0" y="15" fill="red">I love SVG!</text>' +
      // 'Sorry, your browser does not support inline SVG.' +
      // '</svg>';
      // container.children.append(svg);
      // console.log(container.innerHTML);
      // showGreeting(JSON.parse(picto.body).content);
    });
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

