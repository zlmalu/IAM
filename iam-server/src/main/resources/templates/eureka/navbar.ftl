 <div class="messageDiv">
	<div class="tagDiv" style="float: left;width: 100%;margin: 1%;">
  	 	<span>系统状况</span>
	</div>
	<div class="infoDiv">
	    <p>环境：${(environment=='test')?string('测试',environment)}</p>
	    <p>启用租约到期：${(registry.leaseExpirationEnabled?c=='false')?string('否','是')}</p>
	</div>
	<div class="infoDiv">
	    <p>数据中心：${(datacenter=='default')?string('默认',datacenter!)} </p>
	    <p>更新阈值：${registry.numOfRenewsPerMinThreshold}</p>
	</div>
	<div class="infoDiv">
	    <p>当前时间：${currentTime}</p>
	    <p>更新（最后一分钟）：${registry.numOfRenewsInLastMin}</p>
	</div>
	<div class="infoDiv">
	    <p>正常运行时间：${upTime}</p>
	</div>
</div>


