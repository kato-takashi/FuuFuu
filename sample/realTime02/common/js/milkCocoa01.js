$(function() {
        //1.ミルクココアインスタンスを作成
        var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");

        //2."message"データストアを作成
        var ds = milkcocoa.dataStore("message");

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

        function renderMessage(message) {
            var message_html = '<p class="post-text">' + escapeHTML(message.content) + '</p>';
            var date_html = '';
            if(message.date) {
                date_html = '<p class="post-date">'+escapeHTML( new Date(message.date).toLocaleString())+'</p>';
            }
            $("#"+last_message).before('<div id="'+message.id+'" class="post">'+message_html + date_html +'</div>');
            last_message = message.id;
        }

        function post() {
            //5."message"データストアにメッセージをプッシュする
            var content = escapeHTML($("#content").val());
            if (content && content !== "") {
                ds.push({
                    title: "タイトル",
                    content: content,
                    date: new Date().getTime()
                }, function (e) {});
            }
            $("#content").val("");
        }

        $('#post').click(function () {
            post();
        })
        $('#content').keydown(function (e) {
            if (e.which == 13){
                post();
                return false;
            }
        });

        
    });
    //インジェクション対策
    function escapeHTML(val) {
        return $('<div>').text(val).html();
    };