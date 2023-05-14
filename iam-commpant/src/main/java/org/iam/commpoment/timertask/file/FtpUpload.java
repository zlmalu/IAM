package org.iam.commpoment.timertask.file;

import java.util.Date;

import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;
import com.sense.iam.compoment.TaskInterface;

@Name("FTP文件上传")
public class FtpUpload implements TaskInterface{

	@Param("服务器地址")
	private String host;
	@Param("用户名")
	private String username;
	@Param("密码")
	private String password;
	@Param("文件名称")
	private String fileName;
	@Override
	public void run(Long timerTaskId, Date runTime) {
		System.out.println("计划任务已执行!"+timerTaskId+":::"+runTime);
	}

}
