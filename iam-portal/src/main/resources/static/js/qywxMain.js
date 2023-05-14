$(window).resize(function () {          //当浏览器大小变化时
    var menuWidth=$('.menu_info').width();
    var contentWidth=$(window).width()- menuWidth;
    $('.content').css('width',contentWidth);

});



$('#myCarousel').carousel();
$('#appCarousel').carousel({
    interval: 0
});
$('#ca').calendar({
    width: 300,
    height: 300,
    data: [
        {
            date: '2015/12/24',
            value: 'Christmas Eve'
        },
        {
            date: '2015/12/25',
            value: 'Merry Christmas'
        },
        {
            date: '2020/09/10',
            value: 'Happy New Year'
        }
    ],
    onSelected: function (view, date, data) {
        console.log('view:' + view)
        console.log('date:' + date)
        console.log('data:' + (data || 'None'));
    }
});

$('.menu_icon a').click(function () {
    var menuWidth=$('.menu_info').width();
    var contentWidth=$(window).width();
    if(menuWidth==240){
        $('.menu_info').css('width','80px');
        $('.content').css({
            'width':contentWidth - 80,
            "left":'80px'
        });
        $(".ewm").hide();
        $('.menu_icon').css('width','calc(100% - 80px)');
        $('.logo img').attr('src','./images/img_logo_b.png');
        $('.logo img').css('width','40px');
        $('.user_info').css('padding','24px 16px');
        $('.menu_content > ul > li').css('padding-left','12px');
        $('.user_info>ul').toggle();
        $('.close_user').toggle();
    }else {
        $('.menu_info').css('width','240px');
        $('.content').css({
            'width':contentWidth - 240,
            "left":'240px'
        });
        $(".ewm").show();
        $('.menu_icon').css('width','calc(100% - 240px)');
        $('.logo img').attr('src','./images/img_logo.png');
        $('.logo img').css('width','144px');
        $('.user_info').css('padding','24px');
        $('.menu_content > ul > li').css('padding-left','24px');
        $('.user_info>ul').toggle();
        $('.close_user').toggle();
    }
});