$(function(){
    
    $(".intro-header").fadeOut(800, function(){
    	$(".player").mb_YTPlayer();
		$(".intro-header").css("backgroundImage", "url()").fadeIn(800); 
		});
  });