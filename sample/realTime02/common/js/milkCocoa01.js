$(function() {
        //1.ミルクココアインスタンスを作成
        var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");

        //2."message"データストアを作成
        var ds = milkcocoa.dataStore("message");

        var windPower = document.getElementById('windPower');
        var windInterval, recordInterval;
        var windPowerArr = [];
        var iteraterNum = 0;
        var recordTimerNum = 10;
        var sortDataArr = [];
        var setTitleStr = '';

        function getAllDate(){
            resetHTML();
            //3."message"データストアからメッセージを取ってくる
            ds.stream().sort("desc").next(function(err, datas) {
                console.log('data.lengths'+ datas.length);
                // console.log(datas);
                datas.forEach(function(data) {
                    renderMessage(data.value);
                    console.log(data.value.title);
                    // console.log(data);
                    // console.log(data.value);
                    // console.log(data.value.content);
                });
            });    
        }

        $('#getAllDateBtn').click(function () {
            getAllDate();
        })

        function getSortData(sortStr){

            resetHTML();
            var sortArrNum = 0;
            //"message"データストアからtitleを検索した文字でメッセージを取ってくる
            ds.stream().sort("desc").next(function(err, datas) {
                // console.log('data.lengths'+ datas.length);
                // console.log(datas);
                datas.forEach(function(data) {
                    // console.log(data.value.title);
                    if(data.value.title == sortStr){
                        sortDataArr.push(data);   
                        // console.log(sortDataArr);
                        // console.log('配列数'+sortDataArr.length);
                        // console.log('sortArrNum '+sortArrNum);
                        // console.log(sortDataArr[sortArrNum].value);
                        renderMessage(sortDataArr[sortArrNum].value);
                        sortArrNum++ ;
                    }
                });
                if(sortDataArr.length == 0){
                    console.log('そんなのないみたい。');
                    alert('そんなのないみたい。')
                }
            });


        }

        $('#getSortDataBtn').click(function () {
            console.log('search');
            sortDataArr = [];
            // var searchStr = 'リアルタイム　風の強さ';
            var searchStr = $('#searchText').val();
            getSortData(searchStr);
        })

        //4."message"データストアのプッシュイベントを監視
        ds.on("push", function(e) {
            renderMessage(e.value);
        });
        //html表示メッセージの最後の行
        var last_message = "dummy";

        //データの読み込み
        function renderMessage(message) {
            var message_html = '<p class="post-text">' + escapeHTML(message.content) + '</p>';
            var date_html = '';
            if(message.date) {
                date_html = '<p class="post-date">'+escapeHTML(message.title)+' : '+escapeHTML( new Date(message.date).toLocaleString())+'</p>';
            }
            $("#"+last_message).before('<div id="'+message.id+'" class="post">'+message_html + date_html +'</div>');
            last_message = message.id;
        }

        //すべてを消去
        function resetHTML(){
            $(".post").remove();
            //html表示メッセージの最後の行を初期化
            last_message = "dummy";
            console.log('reset');
        }
        $('#resetAll').click(function(){
            resetHTML();
        });


        function post(titleStr, contentStr) {
            var titleStr = titleStr || 'タイトルなし'
            //5."message"データストアにメッセージをプッシュする
            console.log('milkcocoa push')

            var content = escapeHTML(contentStr);
            if (content && content !== "") {
                ds.push({
                    title: titleStr,
                    content: content,
                    date: new Date().getTime()
                }, function (e) {});
            }
            $("#content").val("");
            setTitleStr = '';
        }

        // $('#post').click(function () {
        //     var chatTitle = "chat"
        //     var chatText = $("#content").val();
        //     post(chatTitle, chatText);
        //     console.log('chat push'+chatText);
        // })
        // $('#content').keydown(function (e) {
        //     if (e.which == 13){
        //         post(chatTitle, chatText);
        //         return false;
        //     }
        // });
        /////////タイトルを決定
        $('#titlePostBtn').click(function () {
            setTitle();
        })
        $('#content').keydown(function (e) {
            if (e.which == 13){
                setTitle();                
                return false;
            }
        });

        function setTitle(){
            setTitleStr = '';
            setTitleStr = $("#content").val();
            $('#postedTitle').text(setTitleStr);
            $("#content").val("");
        }
        /////////風の値のダミー
        function randNum(){
          var randNum = Math.floor( Math.random() * 100 );
          console.log(randNum);
          // windPower.innerHTML = randNum;
          $("#windPower").text(randNum);
          windPowerArr.push(randNum);    
        }

        function randNumOne(){
          var randNum = Math.floor( Math.random() * 100 );
          console.log(randNum);
          // windPower.innerHTML = randNum;
          $("#windPower").text(randNum);
          post(setTitleStr, randNum);     
        }

        ////wind　event
        ///

        $('#startWind').click(function () {
            windInterval = setInterval(randNumOne,1500);
            $("#myTimer").text('データベースの容量食うので，早くStopを押すように');

        })

        $('#stopWind').click(function () {
            stopWind();
            $("#myTimer").text('10');
        })
        //リアルタイムにmilkCocoaへpost
        function startWind(){
          windInterval = setInterval(randNum,100);
        }

        function stopWind(){
          clearInterval(windInterval);
          console.log('stop');
        }

        ///Record event　配列に格納してmilkCocoaにpost
        $('#startRecord').click(function () {
            startRecord();
        })

        $('#stopRecord').click(function () {
            stopRecord();
        })

        function startRecord(){
          recordInterval = setInterval(recordTimer,1000);
          startWind();
        }

        function stopRecord(){
          clearInterval(recordInterval);
          console.log('stop record');
          stopWind();
          post(setTitleStr, windPowerArr);
          //ストップした時に配列の初期化
          windPowerArr = [];
        }
        //配列に格納してmilkCocoaにpostする際のストップウォッチ
        function recordTimer(){
            recordTimerNum--;
            if(recordTimerNum < 0){
                stopRecord();
                recordTimerNum = 10;
            }
            $("#myTimer").text(recordTimerNum);
        }
        
    });
    //インジェクション対策
    function escapeHTML(val) {
        return $('<div>').text(val).html();
    };