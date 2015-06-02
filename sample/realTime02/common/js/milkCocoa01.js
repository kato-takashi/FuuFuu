$(function() {
        //1.ミルクココアインスタンスを作成
        var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");

        //2."message"データストアを作成
        var ds = milkcocoa.dataStore("message");

        var windPower = document.getElementById('windPower');
        var windInterval;
        var windPowerArr = [];
        var chatTitle = "chat"
        var chatText = $("#content").val();
        var iteraterNum = 0;

        //3."message"データストアからメッセージを取ってくる
        ds.stream().sort("desc").next(function(err, datas) {
            datas.forEach(function(data) {
                renderMessage(data.value);
                console.log(data);
                console.log(data.value);
                console.log(data.value.content);
            });
        });

        //4."message"データストアのプッシュイベントを監視
        ds.on("push", function(e) {
            renderMessage(e.value);
        });

        var last_message = "dummy";

        //データの読み込み
        function renderMessage(message) {
            var message_html = '<p class="post-text">' + escapeHTML(message.content) + '</p>';
            var date_html = '';
            if(message.date) {
                date_html = '<p class="post-date">'+escapeHTML( new Date(message.date).toLocaleString())+'</p>';
            }
            $("#"+last_message).before('<div id="'+message.id+'" class="post">'+message_html + date_html +'</div>');
            last_message = message.id;
        }

        function post(titleStr, contentStr) {
            var titleStr = titleStr || 'タイトルなし'
            //5."message"データストアにメッセージをプッシュする
            var content = escapeHTML(contentStr);
            if (content && content !== "") {
                ds.push({
                    title: titleStr,
                    content: content,
                    date: new Date().getTime()
                }, function (e) {});
            }
            $("#content").val("");
        }

        $('#post').click(function () {
            post(chatTitle, chatText);
        })
        $('#content').keydown(function (e) {
            if (e.which == 13){
                post(chatTitle, chatText);
                return false;
            }
        });
        /////////風の値のダミー
        function randNum(){
          // var randNum = Math.floor( Math.random() * 100 );
          // console.log(randNum);
          // windPower.innerHTML = randNum;
          //   post('風の強さ', randNum);
          iteraterNum += 1;
          windPower.innerHTML = iteraterNum;
          windPowerArr.push(iteraterNum);
          //iteraterが10の倍数の時　post
          if(iteraterNum%10 == 0){
            post('風の強さ', windPowerArr);
            console.log('post' + iteraterNum);
            windPowerArr = [];  
          }
          
        }

        $('#startWind').click(function () {
            startWind();
        })

        $('#stopWind').click(function () {
            stopWind();
        })

        function startWind(){
          windInterval = setInterval(randNum,1000);
        }

        function stopWind(){
          clearInterval(windInterval);
          console.log('stop');
        }
        
    });
    //インジェクション対策
    function escapeHTML(val) {
        return $('<div>').text(val).html();
    };