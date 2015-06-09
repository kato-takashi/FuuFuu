$(function() {
    //auth0
	    var lock = new Auth0Lock(
	    	'LrUawuyEJfVMi0FKOY4AVB768duSqasd',
	    	'kato.auth0.com'
	    	);

    //var milkcocoa = new MilkCocoa("localhost", {app_id : "iotest0", port : 8001, useSSL : false});
    var milkcocoa = new MilkCocoa("maxiac8gg9b.mlkcca.com");

    //2."userData"データストアを作成
    var userDs = milkcocoa.dataStore("userData");


    function getUserDate(){
        //3."message"データストアからメッセージを取ってくる
        userDs.stream().sort("desc").next(function(err, datas) {
            datas.forEach(function(data) {
                // console.log(data.value);
                // console.log(data);
                // console.log(data.value);
                // console.log(data.value.content);
                milkcocoa.user(function(err, user) {
                    if(user)
                        // $("#output").html("ログイン済み" + JSON.stringify(user));
                        //user.subの値とmilkcocoaの登録上の値を判定
                        if(user.sub == data.value.id){
                            console.log("一致したよ", user.sub, data.value.id);
                            $('#titleName').append(data.value.name + 'の');
                            $('#userData').find("img").attr("src", data.value.picture);
                        }

                    else
                        console.log('not login')
                        // jump('../index.html');
                });
            });
        });    
        }
    getUserDate();
});

//インジェクション対策
function escapeHTML(val) {
    return $('<div>').text(val).html();
};
//urlへジャンプ
function jump(url){
      if (confirm("ログインしますか？")==true)
        //OKならTOPページにジャンプさせる
        location.href = url;
}