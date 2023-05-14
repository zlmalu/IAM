<#import "/spring.ftl" as spring />
<img src="eureka/images/logo.png" alt="">
<style>
.navbar-right {
    float: right!important;
    margin-right: 0 !important;; 
}
</style>
<div class="titleBtn">
	 <ul class="nav navbar-nav navbar-right" style="display: none;">
        <li>
          <a href="<@spring.url dashboardPath/>">首页</a>
        </li>
        <li>
          <a href="<@spring.url dashboardPath/>/lastn">启动时起最后1000条</a>
        </li>
      </ul>
</div>



