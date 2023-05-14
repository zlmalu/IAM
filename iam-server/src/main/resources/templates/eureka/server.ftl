<div class="tagDiv">
   <span>当前注册的实例</span>
</div>
<table class="table table-bordered">
    <thead>
        <th style="width: 25%;">服务名称</th>
        <th style="width: 25%;">AMIs</th>
        <th style="width: 20%;">可用区</th>
        <th style="width: 30%;">状态</th>
    </thead>
    <tbody>
     <#if apps?has_content>
        <#list apps as app>
           <tr>
	        <td>${app.name}</td>
	        <td><#list app.amiCounts as amiCount><b>${amiCount.key}</b> (${amiCount.value})<#if amiCount_has_next>,</#if></#list></td>
	        <td><#list app.zoneCounts as zoneCount> <b>${zoneCount.key}</b> (${zoneCount.value})<#if zoneCount_has_next>,</#if></#list></td>
	        <td><#list app.instanceInfos as instanceInfo>
	        		<#if instanceInfo.isNotUp><font color=red size=+1><b></#if>
                    <b>${instanceInfo.status}</b> (${instanceInfo.instances?size}) -
                    <#if instanceInfo.isNotUp>
                      </b></font>
                    </#if>
                    <#list instanceInfo.instances as instance>
                      <#if instance.isHref>
                        <a href="${instance.url}" target="_blank">${instance.id}</a>
                      <#else>
                        ${instance.id}
                      </#if><#if instance_has_next>,</#if>
                    </#list>
                  </#list></span>
	        </td>
		   </tr>
		</#list>
        <#else>
           <tr><td colspan="4">暂无实例</td></tr>
        </#if>
    </tbody>
</table>


<div class="tagDiv">
    <span>服务器信息</span>
</div>
 <table id='generalInfo' class="table table-striped table-hover">
	<thead>
	  <tr><th>名称</th><th>值</th></tr>
	</thead>
	<tbody>
	  <#list statusInfo.generalStats?keys as stat>
	    <tr>
	    	<#if stat == 'total-avail-memory'>
			 	<td>总可用内存</td><td>${statusInfo.generalStats[stat]!""}</td>
			<#elseif stat == 'environment'>
			  	<td>环境</td>
			  	<td><#if statusInfo.generalStats[stat] == 'test'>测试<#else>${statusInfo.generalStats[stat]!""}</#if></td>
			 <#elseif stat == 'num-of-cpus'>
			  	<td>当前的CPU</td>
			  	<td>${statusInfo.generalStats[stat]!""}</td>
			 <#elseif stat == 'current-memory-usage'>
			  	<td>当前内存使用</td>
			  	<td>${statusInfo.generalStats[stat]!""}</td>
			  <#elseif stat == 'server-uptime'>
			  	<td>服务器正常运行时间</td>
			  	<td>${statusInfo.generalStats[stat]!""}</td>
			 <#else>
				 <td>${stat}</td><td>${statusInfo.generalStats[stat]!""}111</td>
			 </#if>
	    </tr>
	  </#list>
	  
	</tbody>
</table>
      
      
<div class="tagDiv">
    <span>实例信息</span>
</div>
<table id='instanceInfo' class="table table-striped table-hover">
    <thead>
      <tr><th>名称</th><th>值</th></tr>
    <thead>
    <tbody>
      <#list instanceInfo?keys as key>
        <tr>
          	<#if key == 'ipAddr'>
			 	<td>服务地址</td><td>${instanceInfo[key]!""}</td>
			<#elseif key == 'status'>
			  	<td> 状态</td><td>
			  	<#if instanceInfo[key] == 'UP'>
			  		运行
			   	<#else>
				           停止
				</#if>
			  </td>
			
			<#else>
				<td>${key}</td><td>${instanceInfo[key]!""}</td>
			</#if>
        </tr>
      </#list>
    </tbody>
</table>
 
