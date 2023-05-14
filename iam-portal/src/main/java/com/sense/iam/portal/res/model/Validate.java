package com.sense.iam.portal.res.model;


import com.sense.iam.cache.PasswordPolicyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.portal.util.WeakPwdUtil;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validate {

	/**
	 * 获取重复次数
	 * @param list
	 * @return
	 */
	public static int gatVpwdC(List<PwdPolicy> list){
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				if(list.get(i).getName().equals("PWD_NOT_REPEAT_NUM")){
					return  Integer.valueOf(list.get(i).getValue());
				}
			}
		}
		return 0;
	}


	/**
	 * 获取是否和密码用户名相同判断，1是，2否
	 * @param list
	 * @return
	 */
	public static int gatVpwdCusername(List<PwdPolicy> list){
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				if(list.get(i).getName().equals("IS_ALLOW_USERNAME_SAME")){
					return  Integer.valueOf(list.get(i).getValue());
				}
			}
		}
		return 0;
	}


	//数字
    public static final String REG_NUMBER = ".*\\d+.*";
    //小写字母
    public static final String REG_UPPERCASE = ".*[A-Z]+.*";
    //大写字母
    public static final String REG_LOWERCASE = ".*[a-z]+.*";
    //特殊符号
    public static final String REG_SYMBOL = ".*[-=~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*";

	/**
	 * 修改密码判断
	 */
	public static JSONObject cheack(String newPwd,
									Integer PWD_MIN_LEN,
									Integer PWD_MAX_LEN,
									Integer PWD_MIN_CHAR_LEN,
									Integer PWD_MIN_SPACIL_CHAR_LEN,
									Integer PWD_MIN_NUM_LEN,
									Integer PWD_VALID_DAY,
									Integer PWD_NOT_REPEAT_NUM,
									boolean IS_DISABLED){
		JSONObject result=new JSONObject();
		if(!IS_DISABLED){
			result.put("code", 0);
		}
		Integer NUMBER = 0;
		Integer UPPERCASE = 0;
		Integer LOWERCASE = 0;
		Integer SYMBOL = 0;

		//判断密码遍历后所属符号对应长度
		for (int i = 0; i < newPwd.length(); i++) {
			String charAt = newPwd.charAt(i)+"";
			if (charAt.matches(REG_NUMBER)) {NUMBER++;};
			if (charAt.matches(REG_UPPERCASE)) {UPPERCASE++;};
	        if (charAt.matches(REG_LOWERCASE)){LOWERCASE++;};
	        if (charAt.matches(REG_SYMBOL)) {SYMBOL++;};
		}

		if(PWD_MIN_LEN > newPwd.length()){
			result.put("code", 200001);
			result.put("msg", "密码不能小于"+PWD_MIN_LEN+"位数");
		}
		else if(PWD_MAX_LEN < newPwd.length()){
			result.put("code", 200002);
			result.put("msg", "密码不能大于"+PWD_MAX_LEN+"位数");
		}
		else if(PWD_MIN_CHAR_LEN!=0 && PWD_MIN_CHAR_LEN > UPPERCASE+LOWERCASE){
			result.put("code", 200003);
			result.put("msg", "密码包含的字符不能小于"+PWD_MIN_CHAR_LEN+"位数");
		}
		else if(PWD_MIN_SPACIL_CHAR_LEN!=0 && PWD_MIN_SPACIL_CHAR_LEN > SYMBOL){
			result.put("code", 200004);
			result.put("msg", "密码包含的特殊字符不能小于"+PWD_MIN_SPACIL_CHAR_LEN+"位数");
		}

		else if(PWD_MIN_NUM_LEN!=0 && PWD_MIN_NUM_LEN > NUMBER){
			result.put("code", 200005);
			result.put("msg", "密码包含的数字不能小于"+PWD_MIN_NUM_LEN+"位数");
		}
		else{
			result.put("code", 0);
		}
		return result;
	}


	public static JSONObject cheack(String newPwd,
									Integer PWD_MIN_LEN,
									Integer PWD_MAX_LEN,
									Integer PWD_MIN_CHAR_LEN,
									Integer PWD_MIN_SPACIL_CHAR_LEN,
									Integer PWD_MIN_NUM_LEN,
									Integer PWD_VALID_DAY,
									Integer PWD_NOT_REPEAT_NUM,
									boolean IS_DISABLED,
									Integer WEAK_DISCONTINUOUS_LATERAL_KEYBOARD,
									Integer WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD,
									Integer WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS,
									Integer WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY) {
		JSONObject result = cheack(newPwd, PWD_MIN_LEN, PWD_MAX_LEN, PWD_MIN_CHAR_LEN, PWD_MIN_SPACIL_CHAR_LEN, PWD_MIN_NUM_LEN, PWD_VALID_DAY, PWD_NOT_REPEAT_NUM, IS_DISABLED);
		if(result.getInt("code")!=0){
			return result;
		}

		ResultCode resultCode = WeakPwdUtil.checkLateralKeyboardSite(newPwd, WEAK_DISCONTINUOUS_LATERAL_KEYBOARD, false);
		if(resultCode.getCode()!= Constants.OPERATION_SUCCESS){
			result.put("code", 200006);
			result.put("msg", resultCode.getMsg());
			return result;
		}

		resultCode = WeakPwdUtil.checkKeyboardSlantSite(newPwd, WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD, false);
		if(resultCode.getCode()!= Constants.OPERATION_SUCCESS){
			result.put("code", 200007);
			result.put("msg", resultCode.getMsg());
			return result;
		}

		resultCode = WeakPwdUtil.checkSequentialSameChars(newPwd, WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS);
		if(resultCode.getCode()!= Constants.OPERATION_SUCCESS){
			result.put("code", 200008);
			result.put("msg", resultCode.getMsg());
			return result;
		}

		resultCode = WeakPwdUtil.checkSequentialChars(newPwd, WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY,false);
		if(resultCode.getCode()!= Constants.OPERATION_SUCCESS){
			result.put("code", 200009);
			result.put("msg", resultCode.getMsg());
			return result;
		}
		return result;
	}
}
