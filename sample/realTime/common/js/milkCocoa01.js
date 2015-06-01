var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");
/* your-app-id にアプリ作成時に発行されるapp-idを記入します */
var chatDataStore = milkcocoa.dataStore('windPower');
var textArea, board, windPower, windInterval;
window.onload = function(){
  textArea = document.getElementById('msg');
  board = document.getElementById('board');
  windPower = document.getElementById('windPower');
  }

function startWind(){
  windInterval = setInterval(randNum,1000);
}

function stopWind(){
  clearInterval(windInterval);
  console.log('stop');
}

function randNum(){
  var randNum = Math.floor( Math.random() * 100 );
  console.log(randNum);
  windPower.innerHTML = randNum;
  sendText(randNum);
}


function clickEvent(){
  var text = textArea.value;
  sendText(text);
}


function sendText(text){
  chatDataStore.push({power : text});
  console.log("送信完了!");
  textArea.value = "";
}

chatDataStore.on("push",function(data){
  addText(data.value.power);
});

function addText(text){
  var msgDom = document.createElement("li");
  msgDom.innerHTML = text;
  board.insertBefore(msgDom, board.firstChild);
}