// 分页器
function Pagination (data) {
    var isInit = true
    var ele = data.ele
    $(ele + '>div').html('')
    var currentPage = data.currentPage ? data.currentPage : 1
    var callback = data.callback ? data.callback : function(pageNum){
        console.log('当前是第' + pageNum + '页')
    }
    // 跳转指定页函数
    function inputPage () {
        if(!isInit){
            if(currentPage == $(ele + ' input').val()) return $(ele + ' input').val('')
            currentPage = $(ele + ' input').val()
        }
        if(($(ele + ' input').val() > 9 && data.totalPage > 9) || (isInit && currentPage > 10)){
            if(currentPage%10 < 5){
                var a = (currentPage - currentPage%10)/10
                var b = a - 1
                var c = a + b
                $(ele + ' ul').animate({left: -160 *c})
                if(currentPage - (Math.floor(data.totalPage/10)*10) >= 0 && currentPage - (Math.floor(data.totalPage/10)*10) <= 5){
                    if(data.totalPage%10 >= 5 && data.totalPage - currentPage <= data.totalPage - Math.floor(data.totalPage/10)*10 - 5){
                        $(ele + ' .list_box').animate({width: (data.totalPage - Math.floor(data.totalPage/10)*10 +5)*32})
                    } else if(data.totalPage%10 < 5 && data.totalPage - currentPage <= data.totalPage - Math.floor(data.totalPage/10)*10){
                        $(ele + ' .list_box').animate({width: (data.totalPage - Math.floor(data.totalPage/10)*10 + 5)*32})
                    } else {
                        $(ele + ' .list_box').width(320)
                    }
                } else {
                    $(ele + ' .list_box').animate({width: 320})
                }
            } else {
                $(ele + ' ul').animate({left: -160 *((currentPage - currentPage%10)/5)})
                if(currentPage - (Math.floor(data.totalPage/10)*10+5) >= 0 && currentPage - (Math.floor(data.totalPage/10)*10+5) <= 5){
                    $(ele + ' .list_box').animate({width: (data.totalPage - Math.floor(data.totalPage/10)*10)*32})
                } else if(currentPage - (Math.floor(data.totalPage/10)*10) >= 0 && currentPage - (Math.floor(data.totalPage/10)*10) <= 5){
                    $(ele + ' .list_box').animate({width: (data.totalPage - Math.floor(data.totalPage/10)*10)*32})
                } else {
                    $(ele + ' .list_box').animate({width: 320})
                }
            }

        } else {
            $(ele + ' ul').animate({left: 0})
            $(ele + ' .list_box').animate({width: 320})
        }
        $(ele + ' input').val('')
        $(ele + ' ul li').eq(currentPage-1).addClass('Pagination_active').siblings().removeClass('Pagination_active')
        callback(currentPage)
    }
    if(data.needTotalCount){
        $(ele + ' .Pagination').append('<span class="total_num">共<i></i>条</span>')
        $(ele +' .Pagination .total_num i').text(data.totalCount)
    }
    var PaginationLiList = ''
    if(data.totalCount != 0){
        $(ele + ' .Pagination').append('<span class="first_page">首页</span>' +
                                '<span class="prepage">上一页</span>' +
                                '<div class="list_box clearfix"><ul></ul></div>' +
                                '<span class="nextpage">下一页</span>' +
                                '<span class="last_page">尾页</span>' +
                                '<span class="total_page">共'+data.totalPage+'页</span>' +
                                '<input type="text">' +
                                '<span class="redir">跳转</span>')
        if(data.totalPage < 10){
            $(ele + ' .Pagination .list_box').width(data.totalPage*32)
        }
        inputPage()
        isInit = false
        for(var i = 0; i < data.totalPage; i++){
            var j = i + 1
            if(i==currentPage-1){
                var li = '<li class="Pagination_active" data-page="'+ j +'">' + j + '</li>'
            } else {
                var li = '<li data-page="'+ j +'">' + j + '</li>'
            }
            PaginationLiList += li
        }
        $(ele + ' .Pagination ul').html('')
        $(ele + ' .Pagination ul').html(PaginationLiList)
        $(ele + ' .Pagination ul').width(data.totalPage*32)
        // 页码点击跳到指定页数
        $(ele + ' .Pagination ul').on('click','li',function(){
            currentPage = $(this).data("page")
            if(!$(this).hasClass('Pagination_active')){
                $(this).addClass('Pagination_active').siblings().removeClass('Pagination_active')
                if(((currentPage/10)%1==0 || ((currentPage/5)%1==0 && currentPage > 5)) && currentPage != data.totalPage){
                    if(currentPage%2==0){
                        var a = currentPage/10
                        var b = a - 1
                        var c = a + b
                        $(ele + ' .Pagination ul').animate({left: -160 *c})
                    } else {
                        $(ele + ' .Pagination ul').animate({left: -160 *((currentPage/5)-1)})
                    }
                    if(data.totalPage - currentPage < 5){
                        $(ele + ' .Pagination .list_box').animate({width: (data.totalPage - currentPage + 5)*32})
                    }
                }
                if(((currentPage-1)/5)%1==0 && currentPage > 5 && currentPage != data.totalPage){
                    $(ele + ' .Pagination ul').animate({left: -160 *(((currentPage-1)/5)-1)})
                    if(data.totalPage - currentPage >= 5){
                        $(ele + ' .Pagination .list_box').animate({width: 320})
                    }
                }
                callback(currentPage)
            }
        })
        // 跳到第一页
        $(ele + ' .Pagination .first_page').on('click',function(){
            if(currentPage != 1){
                $(ele + ' .Pagination ul li').eq(0).addClass('Pagination_active').siblings().removeClass('Pagination_active')
                $(ele + ' .Pagination ul').animate({left: 0})
                if(data.totalPage > 9){
                    $(ele + ' .Pagination .list_box').animate({width: 320})
                } else {
                    $(ele + ' .Pagination .list_box').animate({width: data.totalPage*32})
                }
                currentPage = 1
                callback(currentPage)
            }
        })
        // 跳到最后一页
        $(ele + ' .Pagination .last_page').on('click',function(){
            if(currentPage != data.totalPage){
                currentPage = data.totalPage
                $(ele + ' .Pagination ul li').eq(data.totalPage-1).addClass('Pagination_active').siblings().removeClass('Pagination_active')
                if(data.totalPage > 10){
                    if(data.totalPage%10 <= 5){
                        var a = (data.totalPage - data.totalPage%10)/10
                        var b = a - 1
                        var c = a + b
                        $(ele + ' .Pagination ul').animate({left: -160 *c})
                        $(ele + ' .Pagination .list_box').stop().animate({width: (data.totalPage-Math.floor(data.totalPage/10)*10+5)*32})
                    } else {
                        $(ele + ' .Pagination ul').animate({left: -160 *((data.totalPage - data.totalPage%10)/5)})
                        $(ele + ' .Pagination .list_box').stop().animate({width: (data.totalPage-Math.floor(data.totalPage/10)*10)*32})
                    }
                }
                callback(currentPage)
            }
        })
        // 上一页
        $(ele + ' .Pagination .prepage').on('click',function(){
            if(currentPage > 1){
                currentPage--
                if(((currentPage-1)/5)%1==0 && currentPage > 5){
                    $(ele + ' .Pagination ul').animate({left: -160 *(((currentPage-1)/5)-1)})
                    if(data.totalPage - currentPage >= 4){
                        $(ele + ' .Pagination .list_box').animate({width: 320})
                    }
                }
                $(ele + ' .Pagination ul li').eq(currentPage-1).addClass('Pagination_active').siblings().removeClass('Pagination_active')
                callback(currentPage)
            }
        })
        // 下一页
        $(ele + ' .Pagination .nextpage').on('click',function(){
            if(currentPage < data.totalPage){
                if(currentPage > 9){
                    if(((currentPage/10)%1==0 || ((currentPage/5)%1==0 && currentPage > 5)) && currentPage != data.totalPage){
                        if(currentPage%2==0){
                            var a = currentPage/10
                            var b = a - 1
                            var c = a + b
                            $(ele + ' .Pagination ul').animate({left: -160 *c})                        
                        } else {
                            $(ele + ' .Pagination ul').animate({left: -160 *((currentPage/5)-1)})
                        }
                        if(data.totalPage - currentPage < 5){
                            $(ele + ' .Pagination .list_box').animate({width: (data.totalPage - currentPage + 5)*32})
                        }
                    }
                }
                currentPage++

                $(ele + ' .Pagination ul li').eq(currentPage-1).addClass('Pagination_active').siblings().removeClass('Pagination_active')
                callback(currentPage)
            }
        })
        // 输入跳转指定页
        $(ele + ' .Pagination input').on('keydown',function(e){
            if(e.keyCode == 13 && $(this).val() <= data.totalPage && $(this).val() != 0){
                inputPage()
            }
        })
        // 转跳按钮
        $(ele + ' .Pagination .redir').on('click', function(){
            if($(ele + ' .Pagination input').val() <= data.totalPage && $(ele + ' input').val() != 0){
                inputPage()
            }
        })
    }
}