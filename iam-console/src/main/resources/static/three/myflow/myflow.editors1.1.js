(function($){
var myflow = $.myflow;

$.extend(true, myflow.editors, {
	textEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        margins:'10 10 10 10',
    	        bodyStyle:'padding:0px 0px 0;',
    	        fieldDefaults: {labelWidth: 35},
    	        defaults: { anchor: '100%'},
    	        border: false,
    	        items: [{
				    fieldLabel: '编码',
				    name:'sn',
				    fieldWidth: 60,
				    allowBlank: false,
				    listeners:{
				    	'change':function(field,newValue,oldValue){
				            $(f).trigger("textchange", [d[e].value, g])
				    	}
				    }
				},{
    	            fieldLabel: '名称',
    	            name:'name',
    	            fieldWidth: 60,
    	            allowBlank: false,
    	            value:'',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        }]
    	    });
		}
	},
	iamEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        margins:'10 10 10 10',
    	        bodyStyle:'padding:0px 0px 0;',
    	        fieldDefaults: {labelWidth: 35},
    	        defaults: { anchor: '100%'},
    	        border: false,
    	        items: [{
    	            fieldLabel: '名称',
    	            name:'name',
    	            fieldWidth: 20,
    	            allowBlank: false,
    	            value:'统一身份认证平台',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        }]
    	    });
		}
	},
	dbEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        margins:'10 10 10 10',
    	        bodyStyle:'padding:0px 0px 0;',
    	        fieldDefaults: {labelWidth: 35},
    	        defaults: { anchor: '100%'},
    	        border: false,
    	        items: [{
    	            fieldLabel: '描述',
    	            name:'name',
    	            fieldWidth: 60,
    	            allowBlank: false,
    	            value:'数据库',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        }]
    	    });
		}
	},
	appEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
    	        id:g.getId(),
    	        layout:'fit',
    	        items: [Ext.create('Ext.tab.Panel', {
                    activeTab: 0,
                    items: [{
                        title: '基本信息',
                        margins:'10 10 10 10',
                        fieldDefaults: {labelWidth: 35},
            	        defaults: { anchor: '100%'},
                        items:[
                               {
                            	   border: false,
                            	   defaultType: 'textfield',
                            	   items:[
											{
											    fieldLabel: '应用编号',
											    name:'sn',
											    fieldWidth: 60,
											    allowBlank: false,
											    listeners:{
											    	'change':function(field,newValue,oldValue){
											            $(f).trigger("textchange", [d[e].value, g])
											    	}
											    }
											},
											{
											    fieldLabel: '应用名称',
											    name:'name',
											    fieldWidth: 60,
											    allowBlank: false,
											    value:'',
											    listeners:{
											    	'change':function(field,newValue,oldValue){
											    		d[e].value = newValue;
											            $(f).trigger("textchange", [d[e].value, g])
											    	}
											    }
											},
											 Ext.create('Ext.form.field.ComboBox', {
													fieldLabel: '列表显示',
													displayField: 'name',
													valueField:'id',
													name:'isView',
													value:'1',
													store:  Ext.create('Ext.data.Store',{ fields : ['id','name'],data:[
															{"id":'1',"name":"是"},
															{"id":'2',"name":"否"}
													]}),
													queryMode: 'local',
													editable:false 
												})
                            	          ]
                               }
                        ]
                    },
                    {
                    	title: '事件',
            	        defaults: { anchor: '100%'},
                        items:[
							Ext.create('Ext.grid.Panel', {
								columns:[{
							            header: '监听事件',
							            dataIndex: 'event',
							            width: 110
							    	},
							    	 {
							            header: '同步组件',
							            dataIndex: 'composieId',
							            width: 130
							        },
							    	 {
							            header: '同步实例',
							            dataIndex: 'email',
							            width: 120
							        }
								],
								tbar:Ext.create('Ext.toolbar.Toolbar',{
									items:[
									       	{text : '新增'},
									       	{text : '移除'}
									       ]
								})
							})
                        ]
                    }
                    ]
                })]
    	                
    	                
    	               
    	    });
		}
	},
	ldapEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
            	margins:'10 10 10 10',
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        border: false,
    	        defaults: { anchor: '100%'},
    	        fieldDefaults: {labelWidth: 65},
    	        items: [
						{
						    fieldLabel: '应用编号',
						    name:'sn',
						    allowBlank: false,
						    listeners:{
						    	'change':function(field,newValue,oldValue){
						            $(f).trigger("textchange", [d[e].value, g])
						    	}
						    }
						},
						{
						    fieldLabel: '应用名称',
						    name:'name',
						    allowBlank: false,
						    value:'',
						    listeners:{
						    	'change':function(field,newValue,oldValue){
						    		d[e].value = newValue;
						            $(f).trigger("textchange", [d[e].value, g])
						    	}
						    }
						},
						Ext.create('Ext.form.field.ComboBox', {
							fieldLabel: '列表显示',
							displayField: 'name',
							valueField:'id',
							name:'isView',
							value:'1',
							store:  Ext.create('Ext.data.Store',{ fields : ['id','name'],data:[
									{"id":'1',"name":"是"},
									{"id":'2',"name":"否"}
							]}),
							queryMode: 'local',
							editable:false 
						}),
						{
						    fieldLabel: '服务器地址',
						    name:'host',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '服务器端口',
						    name:'port',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '管理员',
						    name:'username',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '密码',
						    name:'password',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '根节点',
						    name:'baseDn',
						    allowBlank: false,
						    value:''
						}
        	          ]
    	    });
		}
	},
	adEditor : function(){
		this.init = function(d, e, c, g, f){
			return Ext.create('Ext.form.Panel', {
            	margins:'10 10 10 10',
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        border: false,
    	        defaults: { anchor: '100%'},
    	        fieldDefaults: {labelWidth: 65},
    	        items: [
						{
						    fieldLabel: '应用编号',
						    name:'sn',
						    allowBlank: false,
						    listeners:{
						    	'change':function(field,newValue,oldValue){
						            $(f).trigger("textchange", [d[e].value, g])
						    	}
						    }
						},
						{
						    fieldLabel: '应用名称',
						    name:'name',
						    allowBlank: false,
						    value:'',
						    listeners:{
						    	'change':function(field,newValue,oldValue){
						    		d[e].value = newValue;
						            $(f).trigger("textchange", [d[e].value, g])
						    	}
						    }
						},
						Ext.create('Ext.form.field.ComboBox', {
							fieldLabel: '列表显示',
							displayField: 'name',
							valueField:'id',
							name:'isView',
							value:'1',
							store:  Ext.create('Ext.data.Store',{ fields : ['id','name'],data:[
									{"id":'1',"name":"是"},
									{"id":'2',"name":"否"}
							]}),
							queryMode: 'local',
							editable:false 
						}),
						{
						    fieldLabel: '服务器地址',
						    name:'host',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '服务器端口',
						    name:'port',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '管理员',
						    name:'username',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '密码',
						    name:'password',
						    allowBlank: false,
						    value:''
						},
						{
						    fieldLabel: '根节点',
						    name:'baseDn',
						    allowBlank: false,
						    value:''
						}
        	          ]
    	    });
		}
	},
	rangeEditor : function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
            	margins:'10 10 10 10',
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        border: false,
    	        items: [{
				    fieldLabel: '编码',
				    name:'sn',
				    fieldWidth: 60,
				    allowBlank: false,
				    listeners:{
				    	'change':function(field,newValue,oldValue){
				            $(f).trigger("textchange", [d[e].value, g])
				    	}
				    }
				},{
    	            fieldLabel: '名称',
    	            name:'name',
    	            fieldWidth: 60,
    	            allowBlank: false,
    	            value:'',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        }]
    	    });
		}
	},
	timerEditor:function(){
		this.init = function(d, e, c, g, f){
            return Ext.create('Ext.form.Panel', {
            	margins:'10 10 10 10',
    	        defaultType: 'textfield',
    	        id:g.getId(),
    	        border: false,
    	        defaults: { anchor: '100%'},
    	        fieldDefaults: {labelWidth: 55},
    	        items: [{
				    fieldLabel: '任务编码',
				    name:'sn',
				    allowBlank: false,
				    listeners:{
				    	'change':function(field,newValue,oldValue){
				            $(f).trigger("textchange", [d[e].value, g])
				    	}
				    }
				},{
    	            fieldLabel: '任务名称',
    	            name:'name',
    	            allowBlank: false,
    	            value:'计划任务',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        },{
    	            fieldLabel: '表达式',
    	            name:'cronExpression',
    	            allowBlank: false,
    	            value:'',
    	            listeners:{
    	            	'change':function(field,newValue,oldValue){
    	            		d[e].value = newValue;
    	                    $(f).trigger("textchange", [newValue, g])
    	            	}
    	            }
    	        },Ext.create('Ext.form.field.ComboBox', {
			        fieldLabel: '任务组件',
			        name:'syncApiId',
			        editable:false,
			        displayField: 'name',
			        valueField:'id',
			        allowBlank:false,
			        emptyText:'请选择',
			        store:Ext.create('Ext.data.Store', {autoLoad: true,fields: ['id','name'],
							    proxy: {type: 'ajax',pageSize:100,url: 'sysCompoment.action?operate=findAll&type=2',getMethod: function(){ return 'POST'; }}
							}),
					listeners:{
						select: function(combo, record, index){
							this.up('form').getForm().findField("runClass").getStore().load();
						}
					}
			    }),Ext.create('Ext.form.field.ComboBox', {
			        fieldLabel: '任务实例',
			        name:'runClass',
			        editable:false,
			        allowBlank:false,
			        displayField: 'name',
			        valueField:'id',
					emptyText:'请选择',
			        store:Ext.create('Ext.data.Store', {autoLoad: false,fields: ['id','name','desc'],
							    proxy: {type: 'ajax',pageSize:100,url: 'sysCompoment.action?operate=loadRunClass&type=1',getMethod: function(){ return 'POST'; }},
							    listeners:{
							    	beforeload:function(a,b,c){
							    		this.proxy.extraParams.id = Ext.getCmp(g.getId()).getForm().findField("syncApiId").getValue();
							    	}
							    }
							}),
					listeners:{
						select: function(combo, record, index){
							for(var i=0;i < this.store.getCount();i++){
								if(this.value == this.store.getAt(i).get("id")){
									this.up('form').getForm().findField("configXml").setValue(this.store.getAt(i).get("desc"));
									return ;
								}
							}
						}
					}
			    }),
			    {
		            fieldLabel: '参数配置',
		            name:'configXml',
		            xtype:'textarea',
		            height:100
		        }]
    	    });
		}
	}
});

})(jQuery);