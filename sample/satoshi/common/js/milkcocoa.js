$(function() {
          var dataArray = [];
          var windPowerArray = [];
          //1.ミルクココアインスタンスを作成
          var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");

          //2."message"データストアを作成
          var ds = milkcocoa.dataStore("windData");
          //idナンバー
          var primaryId;

          //入力の風のカウンター
          var max = 50;

          // ds.stream().sort('desc').next(function(err, data) {
          //   console.log(data[0].id);
          // });

          // ds.get('iaky2cm6z6z5w6a',function(err, data) {
          //     console.log(data);
          // });

          ds.stream().sort("desc").size(999).next(function(err, datas) {
          // console.log('data.lengths'+ datas.length);
          // console.log(datas[49]);
          primaryId = datas.length;
          console.log("primaryId", primaryId);

          //milkcocoaからデータを取得->配列を個々のstringに変換し、1つづつ出力
          function getData(){
            datas.forEach(function(data) {
                // console.log(data.value);
                // console.log(data.value.title);
                dataArray.push(data.value);
                // console.log(data);
                // console.log(data.id+ ": " + data.value.content);
               
               });
             // 配列に格納
                var id = 49;//test id:49を取得
                var resArray = dataArray[id].content.split(",");
                // console.log(resArray);
                //配列を一つづつ出力
                for(var i = 0; i < resArray.length; i++){
                  // console.log("resArray"+ resArray[i]);
                  Native.showToast(resArray[i]);
                }
            }

              $("#outPutBtn").click(function() {
                onPushMeClicked();
              }); 

              function onPushMeClicked() {
                  getData();
                  return false;
              }
          });        
        
          //ネイティブからmilkcocoaに送信
          function addTextNode(windPower) {
              $(".nTOw").text(windPower);
              //windPowerを配列に
              //配列が50個になったら，milkcocoaに送信
              windPowerArray.push(windPower);
              if(windPowerArray.length == max){
                post("androidTestWind", windPowerArray);
                // 配列を初期化
                windPowerArray = [];
              }
          }
          // 送信テスト
          // $("#test").click(function() {
          //     for(var i = 0; i < max-5; i++){
          //       addTextNode(i);
          //     }
          // }); 

          //milkcocoaデータストアにプッシュ　引数1タイトル、引数2コンテンツ（風量）
          function post(titleStr, wind) {
              var titleStr = titleStr || 'androidTest'
              //5."message"データストアにメッセージをプッシュする
              console.log('milkcocoa push')
              var wind = wind;
              if (wind && wind !== "") { 
                  primaryId++;
                  ds.push({
                      id:primaryId,
                      title: titleStr,
                      wind: wind,
                      date: new Date().getTime()
                  }, function (e) {});
              }
          }
          addTextNode('CLOSE');
  });