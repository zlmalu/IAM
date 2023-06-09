<#import "/spring.ftl" as spring />
<!doctype html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
  <head>
    <base href="<@spring.url basePath/>">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>服务注册中心</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width">
 	<link rel="stylesheet" href="eureka/css/bootstrap.min.css">
    <link rel="stylesheet" href="eureka/css/public.css">
    <link rel="stylesheet" href="eureka/css/appActivationProcess.css">
	<link rel="icon" type="image/png" href="eureka/images/favicon.png">
	<link rel="icon" type="image/png" href="eureka/images/favicon.png">
  	<link rel="bookmark" type="image/x-icon" href="eureka/images/favicon.png">
  </head>
<body>
    <div class="titleDiv">
    	<#include "header.ftl">
     	<div class="container-fluid xd-container">
	      	<div class="contentDiv">
		      	<#include "navbar.ftl">
		      	<#include "server.ftl">
	      	</div>
     	</div>
    </div>
</body>
</html>
