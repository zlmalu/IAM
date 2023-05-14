package com.sense.iam.portal.action;

import com.google.gson.JsonObject;
import com.sense.core.security.UIM;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.PasswordPolicyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.am.PwdPolicy;
import com.sense.iam.model.im.PwdLog;
import com.sense.iam.portal.res.model.PwdRep;
import com.sense.iam.portal.res.model.Validate;
import com.sense.iam.portal.res.model.ValidateRep;
import com.sense.iam.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RestController
@RequestMapping("pwdPolic")
public class PwdPolicAction extends BaseAction{

	@Resource
	PwdLogService pwdLogService;

	@Resource
	AmPwdPolicyService amPwdPolicyService;

	@Resource
	JdbcService jdbcService;

	@Resource
	AccountService accountService;
	@Resource
	UserService userService;

	@Resource
	private CompanyCache companyCache;

	@Resource
	PasswordPolicyCache passwordPolicyCache;
	/**
	 * 获取密码强度说明
	 * @return
	 */
	@ApiOperation(value="获取密码强度说明", notes="获取密码强度说明")
	@RequestMapping(value = "/findAll",produces={"text/html;charset=UTF-8;","application/json;"}, method=RequestMethod.GET)
	@ResponseBody
    public String pwdPopicy(){
		Long objId=request.getParameter("objId")==null?0L:Long.valueOf(request.getParameter("objId"));

		JSONArray policyData=new JSONArray();

		//是否开启用户类型策略
		boolean IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);

		if(!IS_DISABLED){
			objId = 0L;
			//是否开启全局密码策略
			IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);
			if(!IS_DISABLED){
				return policyData.toString();
			}
		}

		//获取密码最小长度
		Integer PWD_MIN_LEN = passwordPolicyCache.PWD_MIN_LEN(objId);
		//获取密码最大长度
		Integer PWD_MAX_LEN =passwordPolicyCache.PWD_MAX_LEN(objId);
		//获取字符最短长度
		Integer PWD_MIN_CHAR_LEN =passwordPolicyCache.PWD_MIN_CHAR_LEN(objId);
		//获取特殊字符最短长度
		Integer PWD_MIN_SPACIL_CHAR_LEN =passwordPolicyCache.PWD_MIN_SPACIL_CHAR_LEN(objId);
		//获取数字最少长度
		Integer PWD_MIN_NUM_LEN = passwordPolicyCache.PWD_MIN_NUM_LEN(objId);

		Integer PWD_VALID_DAY = passwordPolicyCache.PWD_VALID_DAY(objId);

		Integer PWD_NOT_REPEAT_NUM =passwordPolicyCache.PWD_NOT_REPEAT_NUM(objId);

		//是否允许和用户名相同
		boolean countusername= passwordPolicyCache.IS_ALLOW_USERNAME_SAME(objId);

		//规则1
		int number=1;
		JSONObject json=new JSONObject();
		json.put("msg", number+"、新密码长度为 "+PWD_MIN_LEN+"~"+PWD_MAX_LEN+"位字符");
		policyData.add(json);
		number++;
		//规则2
		if(PWD_MIN_NUM_LEN!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码必须包含至少"+PWD_MIN_NUM_LEN+"位 0~9之间的数字");
			policyData.add(json);
			number++;
		}
		//规则3
		if(PWD_MIN_CHAR_LEN!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码必须包含至少"+PWD_MIN_CHAR_LEN+"位 A~Z 或 a~z 之间的字母");
			policyData.add(json);
			number++;
		}

		//规则4 特殊字符
		if(PWD_MIN_SPACIL_CHAR_LEN!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码必须包含至少"+PWD_MIN_SPACIL_CHAR_LEN+"位  # ￥ &  ^  < > | - + @ ！* 特殊字符");
			policyData.add(json);
			number++;
		}
		//规则5 重复次数
		if(PWD_NOT_REPEAT_NUM!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码不能重复前"+PWD_NOT_REPEAT_NUM+"次密码");
			policyData.add(json);
			number++;
		}
		//规则6 密码有效期
		if(PWD_VALID_DAY!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码有效期为"+PWD_VALID_DAY+"天");
			policyData.add(json);
			number++;
		}


		//规则7 密码是否和用户名相同
		if(countusername==false){
			json=new JSONObject();
			json.put("msg", number+"、新密码不能和用户名相同");
			policyData.add(json);
		}

		//判断弱密码是否开启
		Integer WEAK_IS_DISABLED = passwordPolicyCache.WEAK_IS_DISABLED(-1);
		//进行弱密码校验
		if(WEAK_IS_DISABLED!=1){
			return policyData.toString();
		}

		//禁止在键盘横向字符中连续出现次数
		Integer WEAK_DISCONTINUOUS_LATERAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_LATERAL_KEYBOARD(-1);
		//密码禁止键盘竖向连续次数
		Integer WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD(-1);
		//密码禁止逻辑字符连续次数
		Integer WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS = passwordPolicyCache.WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS(-1);
		//密码禁止单个字符集连续次数
		Integer WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY = passwordPolicyCache.WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY(-1);

		//规则8 禁止在键盘横向字符中连续出现次数
		if(WEAK_DISCONTINUOUS_LATERAL_KEYBOARD!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码禁止在键盘横向字符中连续出现"+WEAK_DISCONTINUOUS_LATERAL_KEYBOARD+"次");
			policyData.add(json);
			number++;
		}

		//规则9 密码禁止键盘竖向连续次数
		if(WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码禁止在键盘竖向字符中连续出现"+WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD+"次");
			policyData.add(json);
			number++;
		}

		//规则10 密码禁止逻辑字符连续次数
		if(WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码禁止出现逻辑字符连续出现"+WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS+"次");
			policyData.add(json);
			number++;
		}

		//规则11 密码禁止单个字符集连续次数
		if(WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY!=0){
			json=new JSONObject();
			json.put("msg", number+"、新密码字符串单字符禁止连续出现"+WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY+"次");
			policyData.add(json);
			number++;
		}
		return policyData.toString();
    }

	/**
	 * 验证新密码是否符合策略
	 * @return
	 */
	@ApiOperation(value="验证新密码是否符合策略", notes="验证新密码是否符合策略")
	@RequestMapping(value = "/validate.action",produces={"text/html;charset=UTF-8;","application/json;"}, method=RequestMethod.POST)
	@ResponseBody
	public ResultCode validate(@RequestBody ValidateRep model){
		JSONObject judgePwd;
		log.info("userId="+model.getUserId());
		CurrentAccount account=null;
		if(model.cheack().getSuccess()){
			Long userId=model.getUserId();
			Long objId=0L;
			boolean IS_DISABLED;

			if(userId!=null&&userId.longValue()!=0){
				objId=userService.findById(userId).getUserTypeId();
				IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);
				if(!IS_DISABLED){
					objId = 0l;
				}
			}

			//是否开启策略
			IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);

			//如果全局的策略也未开启，则结束
			if(!IS_DISABLED){
				return model.ok();
			}

			//获取密码最小长度
			Integer PWD_MIN_LEN = passwordPolicyCache.PWD_MIN_LEN(objId);
			//获取密码最大长度
			Integer PWD_MAX_LEN =passwordPolicyCache.PWD_MAX_LEN(objId);
			//获取字符最短长度
			Integer PWD_MIN_CHAR_LEN =passwordPolicyCache.PWD_MIN_CHAR_LEN(objId);
			//获取特殊字符最短长度
			Integer PWD_MIN_SPACIL_CHAR_LEN =passwordPolicyCache.PWD_MIN_SPACIL_CHAR_LEN(objId);
			//获取数字最少长度
			Integer PWD_MIN_NUM_LEN = passwordPolicyCache.PWD_MIN_NUM_LEN(objId);

			Integer PWD_VALID_DAY = passwordPolicyCache.PWD_VALID_DAY(objId);

			Integer PWD_NOT_REPEAT_NUM =passwordPolicyCache.PWD_NOT_REPEAT_NUM(objId);

			//是否允许和用户名相同
			boolean IS_ALLOW_USERNAME_SAME= passwordPolicyCache.IS_ALLOW_USERNAME_SAME(objId);

			//判断弱密码是否开启
			Integer WEAK_IS_DISABLED = passwordPolicyCache.WEAK_IS_DISABLED(-1);

			if(WEAK_IS_DISABLED!=1){
				judgePwd = Validate.cheack(model.getNewpassword(),PWD_MIN_LEN,PWD_MAX_LEN,PWD_MIN_CHAR_LEN,PWD_MIN_SPACIL_CHAR_LEN,PWD_MIN_NUM_LEN,PWD_VALID_DAY,PWD_NOT_REPEAT_NUM,IS_DISABLED);
			}
			else{
				//禁止在键盘横向字符中连续出现次数
				Integer WEAK_DISCONTINUOUS_LATERAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_LATERAL_KEYBOARD(-1);
				//密码禁止键盘竖向连续次数
				Integer WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD(-1);
				//密码禁止逻辑字符连续次数
				Integer WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS = passwordPolicyCache.WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS(-1);
				//密码禁止单个字符集连续次数
				Integer WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY = passwordPolicyCache.WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY(-1);
				judgePwd = Validate.cheack(model.getNewpassword(),PWD_MIN_LEN,PWD_MAX_LEN,PWD_MIN_CHAR_LEN,PWD_MIN_SPACIL_CHAR_LEN,PWD_MIN_NUM_LEN,PWD_VALID_DAY,PWD_NOT_REPEAT_NUM,IS_DISABLED,WEAK_DISCONTINUOUS_LATERAL_KEYBOARD,WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD,WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS,WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY);
			}

			if(judgePwd.getInt("code") != 0){
				return new ResultCode(Constants.OPERATION_FAIL,judgePwd.getString("msg"));
			}
			if(model.getUserId()==0)return new ResultCode(Constants.OPERATION_SUCCESS);
			long userIds=Long.valueOf(model.getUserId());
			String domain = request.getParameter("domain");
			account = new CurrentAccount(
					companyCache.getCompanySn(domain == null ? (request
							.getHeader("RemoteHost") == null ? request
							.getHeader("Host") : request
							.getHeader("RemoteHost")) : domain));
			account.setUserId(userIds);
			List<Map<String, Object>>  maps=jdbcService.findList("select a.ID,a.LOGIN_NAME from im_account a left join im_app b on a.app_id=b.id where a.user_id=? and b.sn=?",userIds,"APP001");

			if(maps!=null&&maps.size()>0){
				account.setId(Long.valueOf(maps.get(0).get("ID").toString()));
				account.setLoginName(maps.get(0).get("LOGIN_NAME").toString());
			}else{
				account.setId(null);
			}
			if(account!=null){
				log.info("account.id="+account.getId());
				//密码不能和登录名相同
				if(!IS_ALLOW_USERNAME_SAME){
					if(model.getNewpassword().equals(account.getLoginName())){
						return new ResultCode(Constants.OPERATION_FAIL,"新密码不能和用户名相同");
					}
				}

				if(PWD_NOT_REPEAT_NUM==0)return new ResultCode(Constants.OPERATION_FAIL,judgePwd.getString("msg"));
				int countoid=PWD_NOT_REPEAT_NUM;
				//验证密码历次次数是否重复
				if(judgePwd.getInt("code")==0){
					//sql 根据账号ID查询历史密码库，获取密码修改记录
					List<Map<String, Object>> mapsd=jdbcService.findList("select NAME from im_pwdlog where account_id=? ORDER BY CREATE_TIME desc",account.getId());
					if(mapsd!=null&&mapsd.size()>0){
						if(mapsd.size() >= PWD_NOT_REPEAT_NUM){
							PWD_NOT_REPEAT_NUM=PWD_NOT_REPEAT_NUM;
						}else{
							PWD_NOT_REPEAT_NUM=mapsd.size();
						}
						for(int i=0;i<PWD_NOT_REPEAT_NUM;i++){
							String pwd=UIM.decode(mapsd.get(i).get("NAME").toString());
							if(model.getNewpassword().equals(pwd)){
								judgePwd=new JSONObject();
								judgePwd.put("code", 200007);
								judgePwd.put("msg", "新密码不能重复前"+countoid+"次密码");
								return new ResultCode(Constants.OPERATION_FAIL,judgePwd.getString("msg"));
							}
						}
					}
				}
			}
			return model.ok();
		}else{
			return model.cheack();
		}
	}



	/**
	 * 验证新密码是否符合策略
	 * @return
	 */
	@ApiOperation(value="自助密码修改", notes="自助密码修改")
	@RequestMapping(value = "/updatePwd.action",produces={"text/html;charset=UTF-8;","application/json;"}, method=RequestMethod.POST)
	@ResponseBody
	public ResultCode updatePwd(@RequestBody PwdRep model){
		log.info("userId="+model.getUserId());
		JSONObject judgePwd;
		long userId=model.getUserId();
		if(model.cheack().getSuccess()){
			Long objId=0L;
			boolean IS_DISABLED = true;

			if(userId!=0){
				objId=userService.findById(userId).getUserTypeId();
				IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);
				if(!IS_DISABLED){
					objId = 0l;
				}
			}

			//是否开启策略
			IS_DISABLED=passwordPolicyCache.IS_DISABLED(objId);

			//如果全局的策略也未开启，则结束
			if(!IS_DISABLED){
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}

			//获取密码最小长度
			Integer PWD_MIN_LEN = passwordPolicyCache.PWD_MIN_LEN(objId);
			//获取密码最大长度
			Integer PWD_MAX_LEN =passwordPolicyCache.PWD_MAX_LEN(objId);
			//获取字符最短长度
			Integer PWD_MIN_CHAR_LEN =passwordPolicyCache.PWD_MIN_CHAR_LEN(objId);
			//获取特殊字符最短长度
			Integer PWD_MIN_SPACIL_CHAR_LEN =passwordPolicyCache.PWD_MIN_SPACIL_CHAR_LEN(objId);
			//获取数字最少长度
			Integer PWD_MIN_NUM_LEN = passwordPolicyCache.PWD_MIN_NUM_LEN(objId);

			Integer PWD_VALID_DAY = passwordPolicyCache.PWD_VALID_DAY(objId);

			Integer PWD_NOT_REPEAT_NUM =passwordPolicyCache.PWD_NOT_REPEAT_NUM(objId);

			//是否允许和用户名相同
			boolean IS_ALLOW_USERNAME_SAME= passwordPolicyCache.IS_ALLOW_USERNAME_SAME(objId);

			//判断弱密码是否开启
			Integer WEAK_IS_DISABLED = passwordPolicyCache.WEAK_IS_DISABLED(-1);
			if(WEAK_IS_DISABLED!=1){
				judgePwd = Validate.cheack(model.getNewpassword(),PWD_MIN_LEN,PWD_MAX_LEN,PWD_MIN_CHAR_LEN,PWD_MIN_SPACIL_CHAR_LEN,PWD_MIN_NUM_LEN,PWD_VALID_DAY,PWD_NOT_REPEAT_NUM,IS_DISABLED);
			}
			else{
				//禁止在键盘横向字符中连续出现次数
				Integer WEAK_DISCONTINUOUS_LATERAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_LATERAL_KEYBOARD(-1);
				//密码禁止键盘竖向连续次数
				Integer WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD = passwordPolicyCache.WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD(-1);
				//密码禁止逻辑字符连续次数
				Integer WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS = passwordPolicyCache.WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS(-1);
				//密码禁止单个字符集连续次数
				Integer WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY = passwordPolicyCache.WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY(-1);
				judgePwd = Validate.cheack(model.getNewpassword(),PWD_MIN_LEN,PWD_MAX_LEN,PWD_MIN_CHAR_LEN,PWD_MIN_SPACIL_CHAR_LEN,PWD_MIN_NUM_LEN,PWD_VALID_DAY,PWD_NOT_REPEAT_NUM,IS_DISABLED,WEAK_DISCONTINUOUS_LATERAL_KEYBOARD,WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD,WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS,WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY);
			}

			if(judgePwd.getInt("code")==0){
				//判断修改的应用
				List<Map<String, Object>> maps =jdbcService.findList("select a.ID,a.LOGIN_NAME,a.LOGIN_PWD,b.SN from im_account a left join im_app b on a.app_id=b.id where a.user_id=? and b.sn=?",userId,model.getAppSn());

				if(maps==null || maps.size()==0){
					return new ResultCode(Constants.OPERATION_FAIL,"系统没有找到："+userId+",应用标识："+model.getAppSn()+",对应的账号信息！");
				}
				CurrentAccount account=null;
				Long[] ids=new Long[maps.size()];
				long accId=0;
				String loginName=null;
				String loginPwd=null;

				for(int i=0;i<maps.size();i++){

					accId=Long.valueOf(maps.get(i).get("ID").toString());
					loginName=maps.get(i).get("LOGIN_NAME").toString();

					loginPwd=maps.get(i).get("LOGIN_PWD").toString();
					//解密对比旧密码
					loginPwd=UIM.decode(loginPwd);

					if(!loginPwd.equals(model.getOldPassword())){
						return new ResultCode(Constants.OPERATION_FAIL,"原密码错误");
					}
					ids[i]=accId;
				}

				String domain = request.getParameter("domain");
				account = new CurrentAccount(
						companyCache.getCompanySn(domain == null ? (request
								.getHeader("RemoteHost") == null ? request
								.getHeader("Host") : request
								.getHeader("RemoteHost")) : domain));
				account.setUserId(model.getUserId());
				account.setId(accId);
				account.setLoginName(loginName);
				account.setRemoteHost(GatewayHttpUtil.getKey("RemoteIp", request));
				account.setLastLoginTime(new Date().getTime());
				account.setSessionId(GatewayHttpUtil.getKey(Constants.CURRENT_SESSION_ID, request));
				CurrentAccount.setCurrentAccount(account);



				log.info("account.id="+account.getId());

				//密码不能和登录名相同
				if(!IS_ALLOW_USERNAME_SAME){
					if(model.getNewpassword().equals(account.getLoginName())){
						CurrentAccount.setCurrentAccount(null);
						return new ResultCode(Constants.OPERATION_FAIL,"新密码不能和用户名相同");
					}
				}
				int count=PWD_NOT_REPEAT_NUM.intValue();
				if(count==0){
					if("APP001".equals(model.getAppSn())){
						//插入历史密码库值记录APP001的密码
						PwdLog entity=new PwdLog();
						entity.setAcctId(accId);
						entity.setCreateTime(new Date());
						entity.setOperate(loginName);
						entity.setName(UIM.encode(model.getNewpassword()));
						pwdLogService.save(entity);
					}
					accountService.updatePwd(ids,model.getNewpassword());
					CurrentAccount.setCurrentAccount(null);
					return new ResultCode(Constants.OPERATION_SUCCESS);
				}
				int countoid=count;
				//验证密码历次次数是否重复

				//sql 根据账号ID查询历史密码库，获取密码修改记录
				List<Map<String, Object>> mapsd=jdbcService.findList("select NAME from im_pwdlog where account_id=? ORDER BY CREATE_TIME desc",account.getId());
				if(mapsd!=null&&mapsd.size()>0){
					if(mapsd.size()>=count){
						count=count;
					}else{
						count=mapsd.size();
					}
					for(int i=0;i<count;i++){
						String pwd=UIM.decode(mapsd.get(i).get("NAME").toString());
						if(model.getNewpassword().equals(pwd)){
							CurrentAccount.setCurrentAccount(null);
							return new ResultCode(Constants.OPERATION_FAIL,"新密码不能重复前"+countoid+"次密码");
						}
					}
				}

				if("APP001".equals(model.getAppSn())){
					//插入历史密码库值记录APP001的密码
					PwdLog entity=new PwdLog();
					entity.setAcctId(accId);
					entity.setCreateTime(new Date());
					entity.setOperate(loginName);
					entity.setName(UIM.encode(model.getNewpassword()));
					pwdLogService.save(entity);
				}

				accountService.updatePwd(ids,model.getNewpassword());
				CurrentAccount.setCurrentAccount(null);
				return new ResultCode(Constants.OPERATION_SUCCESS);
			}else{
				return new ResultCode(Constants.OPERATION_FAIL,judgePwd.getString("msg"));
			}
		}else{
			return model.cheack();
		}
	}
}
