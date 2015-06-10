var Base64Converter = function(){
	  // URLからファイルを取得
	  this.loadBinaryResource = function(url) {
	    var req = new XMLHttpRequest();
	    req.open('GET', url, false);
	    req.overrideMimeType('text/plain; charset=x-user-defined');
	    req.send(null);
	    if (req.status != 200) return '';
	    return req.responseText;
	  }
	 
	  // バイナリデータを文字列に変換
	  this.convertBinaryFile = function(url) {
	    var filestream = this.loadBinaryResource(url);
	    var bytes = [];
	    for (i = 0; i < filestream.length; i++){
	      bytes[i] = filestream.charCodeAt(i) & 0xff;
	    }
	    return String.fromCharCode.apply(String, bytes);
	  }
	 
	  // バイト文字列をbase64文字列に変換
	  this.convertImgDataURL = function(url){
	    var binary_file = this.convertBinaryFile(url);
	    var base64 = btoa(binary_file);
	    var head = binary_file.substring(0,9);
	    var exe = this.checkExe(head);
	    console.log(head);
	    console.log(exe);
	    //console.log(base64);
	    return 'data:image/' + exe + ';base64,' + base64;
	  }
	 
	  // バイナリの拡張子をチェック
	  this.checkExe = function(head){
	    if (head.match(/^\x89PNG/)) {
	      return 'png';
	    } else if (head.match(/^BM/)){
	      return 'bmp';
	    } else if (head.match(/^GIF87a/) || head.match(/^GIF89a/)) {
	      return 'gif';
	    } else if (head.match(/^\xff\xd8/)) {
	      return 'jpeg';
	    } else {
	      return false;
	    }
	  }
	}

	$(function(){
	  var bc = new Base64Converter();
	 
	    var url = "common/images/test.png";
	    url = encodeURI(url);
	    var src = bc.convertImgDataURL(url);
	    console.log(src);
	    $('#pct').append('<img src="' + src + '" alt=""/>');
	  });