(function($){
var myflow = $.myflow;
var initSn=100000001;
$.extend(true,myflow.config.rect,{
	attr : {
	r : 8,
	fill : '#F6F7FF',
	stroke : '#03689A',
	"stroke-width" : 2
}
});


$.extend(true,myflow.config.tools.states,{
			user : {showType: 'text',type : 'user',
				text : {text:'用户'},
				img : {src : 'img/48/task_empty.png',width :48, height:48},
				props : {
					sn: {name:'sn', label: '编码',autoSn:true, value:'', editor: function(){return new myflow.editors.inputEditor();}},
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.textEditor();}}
				}},
			
			func : {showType: 'text',type : 'func',
				text : {text:'权限'},
				img : {src : 'img/48/task_empty.png',width :48, height:48},
				props : {
					sn: {name:'sn', label: '编码',autoSn:true, value:'', editor: function(){return new myflow.editors.inputEditor();}},
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.textEditor();}}
				}}
});
})(jQuery);