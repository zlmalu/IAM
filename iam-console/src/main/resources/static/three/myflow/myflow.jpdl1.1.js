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
			db : {type : 'db',
				text : {text:'数据库'},
				img : {src : 'three/myflow/img/48/db.png',width :48, height:48},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.dbEditor();}}
				}},
			app : {showType: 'text',type : 'app',
				text : {text:'应用系统'},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.appEditor();}}
				}},
			iam : {showType: 'text',type : 'iam',
				text : {text:'统一身份认证平台'},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.iamEditor();}}
				}},
			ldap : {showType: 'text',type : 'ldap',
				text : {text:'企业目录'},
				img : {src : 'img/48/task_empty.png',width :48, height:48},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.ldapEditor();}}
				}},
			ad : {showType: 'text',type : 'ad',
				text : {text:'AD域控'},
				img : {src : 'img/48/task_empty.png',width :48, height:48},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.adEditor();}}
				}},
			range : {showType: 'text',type : 'range',fill:'#FFFFFF',
				text : {text:'分组'},
				img : {src : 'img/48/task_empty.png',width :48, height:48},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.rangeEditor();}}
				}},
			timer : {showType: 'image',type : 'timer',fill:'#FFFFFF',
				text : {text:'计划任务'},
				img : {src : 'three/myflow/img/48/timer.gif',width :48, height:48},
				props : {
					text: {name:'name', label : '名称', value:'', editor: function(){return new myflow.editors.timerEditor();}}
				}}
});
})(jQuery);