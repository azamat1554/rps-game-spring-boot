"use strict";

let socket;
let chatInput;
let urlInput;
let currentChoice;
// let gameId;
let playerId;

window.onload = function init() {
  //инициализация
  urlInput = document.querySelector('#url');
  chatInput = document.querySelector('#chat-input > input');

  //установка обработчиков
  urlInput.onfocus = function (event) {
    var r = document.createRange();
    r.selectNode(event.target);
    window.getSelection().addRange(r);
  };

  chatInput.onkeydown = function (event) {
    if (event.keyCode === 13) {
      clickSend();
    }
  };

  document.querySelector('#chat-input > span').onclick = clickSend;
  document.querySelector('#select-box').onclick = clickImage;

  connectToGame().then(createWebSocketConnection)
};

async function connectToGame() {
  let hash = location.hash.replace('#', '');
  let response = await fetch(
      "http://" + location.host + "/connection?gameId=" + hash);

  let json = await response.json();
  if (response.status === 201) {
    window.location.hash = json.gameId;
    urlInput.value = window.location.href;
  }
  return json.gameId;
}

function createWebSocketConnection(gameId) {
  let url = "ws://" + location.host + "/game/" + gameId;
  socket = new WebSocket(url);
  // socket.onopen = onOpen;
  socket.onclose = onClose;
  socket.onerror = onError;
  socket.onmessage = onMessage;
}

//============================================
//            WebSocket methods
//============================================
// function onOpen(event) {
//   var hash = location.hash.replace('#', '');
//   var object = {
//     gameId: hash,
//     type: 'ID'
//   };
//
//   socket.send(JSON.stringify(object));
// }

function onClose(event) {
  if (event.wasClean) {
    console.log('Соединение закрыто чисто');
  } else {
    console.log('Обрыв соединения'); // например, "убит" процесс сервера
  }
  console.log('Код: ' + event.code + ' причина: ' + event.reason);

  if (event.reason) {
    showCover('You were inactive for too long.');
  } else {
    showCover('Your opponent is disconnected');
  }
}

function onError(error) {
  console.log("Ошибка " + error.message);
}

//метод который вызывается, когда приходят сообщения
function onMessage(event) {
  console.log('onMessage: ' + event.data.toString());

  var incomingMessage = JSON.parse(event.data);
  switch (incomingMessage.type) {
    case 'MESSAGE':
      showMessage(incomingMessage.message, false);
      break;
    case 'RESULT':
      showResult(incomingMessage);
      break;
    case 'CONNECTION':
      // gameId = incomingMessage.gameId;
      playerId = incomingMessage.id;
      showConnection(incomingMessage.connection);
      break;
      // case 'ID':
      // window.location.hash = incomingMessage.gameId;
      // urlInput.value = window.location.href;
      // playerId = incomingMessage.id;
  }
}

//===========================================
//   Methods for handling messages
//===========================================

function showMessage(message, isYour) {
  var newMessageElem = document.createElement('div');
  newMessageElem.classList.add('message-style');
  newMessageElem.classList.add(isYour ? 'your-message' : 'opp-message');
  newMessageElem.appendChild(document.createTextNode(message));

  var parentElem = document.createElement('div');
  parentElem.classList.add('media');
  parentElem.appendChild(newMessageElem);

  var block = document.getElementById('message-body');
  block.appendChild(parentElem);
  block.scrollTop = block.scrollHeight; //чтобы прокручивалось в конец
}

function sendMessage(message) {
  var msgJObj = {
    id: playerId,
    type: "MESSAGE",
    message: message
  };

  try {
    socket.send(JSON.stringify(msgJObj));
  } catch (exp) {
    console.log(exp)
  }
}

function showResult(resultObj) {
  var header = document.querySelector('.result-header');

  header.textContent = resultObj.result;

  header.hidden = false;
  document.getElementById('opp-choice-image').src = 'images/'
      + resultObj.playerChoice.toLowerCase() + '.png';

  if (resultObj.result === 'WIN') {
    document.getElementById('your-score').textContent++;
  } else if (resultObj.result === "LOSE") {
    document.getElementById('opp-score').textContent++;
  }

  setTimeout(restartGame, 2000);
}

function restartGame() {
  var children = document.querySelector('#select-box').children;

  toggleHidden(children, 1, 4);
  children[0].firstElementChild.hidden = false;
  children[0].lastElementChild.hidden = true;
  currentChoice.parentNode.hidden = false;

  currentChoice.parentNode.classList.toggle('icon-animate');
  currentChoice.style.cssFloat = '';
  currentChoice.parentNode.style.left = '';

  currentChoice.classList.toggle('icon');
  document.querySelector('#select-box').onclick = clickImage;

  document.getElementById('opp-choice-image').src = 'images/preloader.gif';

  toggleHidden(children, 4, children.length);
}

function toggleHidden(elements, start, end) {
  for (var i = start; i < end; i++) {
    elements[i].hidden = !elements[i].hidden;
  }
}

function sendResult(choice) {
  var object = {
    // gameId: gameId,
    id: playerId,
    type: "RESULT",
    choice: choice
  };

  socket.send(JSON.stringify(object));
}

function showConnection(connection) {
  if (connection) {
    if (!document.getElementById('cover')) {
      document.getElementById('main-box').hidden = false;
      document.getElementById('url-box').hidden = true;
    }
  }
}

//=========================================
//   Methods for event handling
//=========================================

function clickSend() {
  var message = chatInput.value.trim();
  if (!message) {
    return;
  }

  //очистить поле ввода
  chatInput.value = '';

  showMessage(message, true);
  sendMessage(message);
}

function clickImage(event) {
  let target = event.target;

  let currentTarger = target;
  while (currentTarger.tagName !== 'IMG') {
    if (currentTarger == this) {
      return;
    }
    currentTarger = currentTarger.parentNode;
  }

  currentChoice = target;

  var children = document.querySelector('#select-box').children;

  toggleHidden(children, 1, 4);
  children[0].firstElementChild.hidden = true;
  currentChoice.parentNode.hidden = false;

  currentChoice.parentNode.classList.toggle('icon-animate');
  currentChoice.parentNode.style.left = 0 + 'px';
  currentChoice.style.cssFloat = 'right';

  currentChoice.classList.toggle('icon');
  document.querySelector('#select-box').onclick = null;

  if (currentChoice.parentNode == children[1]) {
    toggleHidden(children, 4, children.length);
  } else {
    setTimeout(toggleHidden, 500, children, 4, children.length);
  }

  setTimeout(sendResult, 500, target.getAttribute('data-choice'));
}

//==============================================
//          Methods for cover window
//==============================================

// Показать полупрозрачный DIV, затеняющий всю страницу
function showCover(reason) {
  var cover = document.createElement('div');
  cover.id = 'cover';
  cover.classList.add('cover');

  var windowDiv = document.createElement('div');
  windowDiv.classList.add('window');
  windowDiv.textContent = reason;
  cover.appendChild(windowDiv);

  document.body.appendChild(cover);
}
