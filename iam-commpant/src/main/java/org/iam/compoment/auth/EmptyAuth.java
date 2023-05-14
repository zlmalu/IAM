package org.iam.compoment.auth;

import java.util.Map;

import com.sense.iam.compoment.AuthInterface;
import com.sense.iam.compoment.Name;
import com.sense.iam.compoment.Param;

@Name("空密码认证")
public class EmptyAuth  implements AuthInterface{

	@Param("描述")
	private String description;
	
	@Override
	public void authentication(String uid, String password, Map<String, Object> params) {
		
	}

}
