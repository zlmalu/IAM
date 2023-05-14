package com.sense.iam.auth;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

import com.sense.iam.api.util.LimitQueue;
import com.sense.iam.config.RedisCache;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;




import com.sense.core.security.Md5;
import com.sense.core.util.CurrentAccount;
import com.sense.core.util.GatewayHttpUtil;
import com.sense.iam.api.SessionManager;
import com.sense.iam.api.action.BaseAction;
import com.sense.iam.api.action.sys.ResultCodeReq;
import com.sense.iam.api.model.LoginModel;
import com.sense.iam.cache.CompanyCache;
import com.sense.iam.cache.LoginPolicyCache;
import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Func;
import com.sense.iam.policy.RedisLoginaPolicy;
import com.sense.iam.service.SysAcctService;
import com.sense.iam.service.SysFuncService;
import com.sense.iam.service.SysRoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "API - 系统用户认证", tags = "用户认证")
@Controller
@RestController
@RequestMapping("sys")
public class LoginAction extends BaseAction {
    LimitQueue limitQueue = new LimitQueue(100);
    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysAcctService sysAcctService;

	@Resource
	private RedisLoginaPolicy redisLoginaPolicy;

	@Resource
	private CompanyCache companyCache;
    @Resource
    private LoginPolicyCache loginPolicyCache;
    @Resource
    private RedisCache redisCache;


    @ApiOperation(value = "注销令牌", notes = "注销令牌")
    @RequestMapping(value = "logout/{token}", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "唯一标识", required = true, paramType = "path", dataType = "String")
    })
    public Object logout(@PathVariable String token) {
        writerLog(CurrentAccount.getCurrentAccount().getLoginName(), "logout", Constants.OPT_SUCCESS, "");
        SessionManager.removeBySessionId(token);
        return new ResultCode(Constants.OPERATION_SUCCESS);
    }

    @ApiOperation(value = "获取令牌接口", notes = "获取令牌接口")
    @RequestMapping(value="getToken", method=RequestMethod.POST)
	@ResponseBody
    public Object getToken(@RequestBody LoginModel loginModel){
		String username=loginModel.getUsername();
		String password=loginModel.getPassword();
		if(username!=null && password!=null){
			log.info("loginModel:"+loginModel.getUsername());
			Acct sysAcct=new Acct();
			sysAcct.setLoginName(username);
			sysAcct.setCompanySn(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			sysAcct=sysAcctService.findByObject(sysAcct);
			if(sysAcct!=null){
				if(sysAcct.getStatus()!=Constants.ACCOUNT_ENABLED){
					writerLog(username,"login",Constants.OPT_FAIL,"账号被禁用");
					return new ResultCode(Constants.LOGIN_STATUS_ACCOUNT_DISABLED,"账号被禁用");
				}
				try {
					if(!sysAcct.getLoginPwd().equals(Md5.encode(password.getBytes("UTF-8")))){
						String msg=validateLoginPolicy(sysAcct);
						writerLog(username,"login",Constants.OPT_FAIL,msg);
						return new ResultCode(Constants.LOGIN_STATUS_AUTH_ERROR,msg);
					}

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return initUser(sysAcct);
			}
		}
		return new ResultCode(Constants.LOGIN_STATUS_NOT_EXIST,"账号被禁用");
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    @ApiIgnore
    public Object login(@RequestBody LoginModel loginModel) {
    	if(loginModel.getVerificationCode().length()==0){
			return new ResultCode(Constants.LOGIN_STATUS_NOEXIST_LOGINMODULE,"请输入验证码");
    	}
    	Boolean ifTrueofcode = ifTrueofcode(loginModel.getVerificationCode(),loginModel.getTokenid());
    	if(!ifTrueofcode){
			return new ResultCode(Constants.LOGIN_STATUS_VERIFICATION_WRONG,"验证码错误");
    	}

        //登录安全策略--判断用户是被锁住
        if(redisLoginaPolicy.isLock("system",loginModel.getUsername())){
            return new ResultCode(Constants.LOGIN_REDIS_AUTH_LOCK, "超过最大错误次数，账号被锁定");
        }

		String username=loginModel.getUsername();
		String password=loginModel.getPassword();
		if(username!=null && password!=null){
			log.info("loginModel:"+loginModel.getUsername());
			Acct sysAcct=new Acct();
			sysAcct.setLoginName(username);
			sysAcct.setCompanySn(companyCache.getCompanySn(GatewayHttpUtil.getKey("RemoteHost", request)));
			sysAcct=sysAcctService.findByObject(sysAcct);
			if(sysAcct!=null){
				if(sysAcct.getStatus()!=Constants.ACCOUNT_ENABLED){
					writerLog(username,"login",Constants.OPT_FAIL,"账号被禁用");
					return new ResultCode(Constants.LOGIN_STATUS_ACCOUNT_DISABLED,"账号被禁用");
				}
				log.info("密码比较："+sysAcct.getLoginPwd()+":"+password);
                if(!sysAcct.getLoginPwd().equals(password)){
                    String msg=validateLoginPolicy(sysAcct);
                    writerLog(username,"login",Constants.OPT_FAIL,msg);
                    return new ResultCode(Constants.LOGIN_STATUS_AUTH_ERROR,msg);
                }
				return initUser(sysAcct);
			}
		}
		return new ResultCode(Constants.LOGIN_STATUS_NOT_EXIST,"账号被禁用");
    }

    public static void main(String[] args) {
        String a = "WEAK_IS_DISABLED,WEAK_DISCONTINUOUS_LATERAL_KEYBOARD,WEAK_DISCONTINUOUS_VERTICAL_KEYBOARD,WEAK_NO_CONTINUOUS_LOGICAL_CHARACTERS,WEAK_PROHIBIT_CHARACTER_SET_CONTINUITY";
        System.out.println(a.toLowerCase());

    }

    @RequestMapping(value = "ssoLogin", method = RequestMethod.GET)
    @ResponseBody
    @ApiIgnore
    public Object ssoLogin() {
        Object username = request.getSession().getAttribute("CURRENT_USER");
        log.info("username: " + username == null ? "" : username.toString());
        if (username != null) {
            Acct sysAcct = new Acct();
            sysAcct.setLoginName(username.toString());
            sysAcct = sysAcctService.findByObject(sysAcct);
            if (sysAcct != null) {
                if (sysAcct.getStatus() != Constants.ACCOUNT_ENABLED) {
                    writerLog(username.toString(), "login", Constants.OPT_FAIL, "账号被禁用");
                    return new ResultCode(Constants.LOGIN_STATUS_ACCOUNT_DISABLED, "账号被禁用");
                }
                return initUser(sysAcct);
            }
        }
        return new ResultCode(Constants.LOGIN_STATUS_NOT_EXIST, "账号不存在");
    }


    /*获取随机验证码数据*/
    @RequestMapping(value = "verificationCode", method = RequestMethod.GET)
    @ResponseBody
    @ApiIgnore
    public String[] getverificationCode() {
        String[] arr = new String[2];
        Map<String, String> code = createYcode();
        //缓存code 30 分钟
        redisCache.setCacheObject("sys:login:verificationcode:"+code.get("code").toLowerCase()+"_"+code.get("tokenid").toLowerCase(),"code",30, TimeUnit.MINUTES);
        //this.limitQueue.offer(code);
        arr[0] = code.get("code");
        arr[1] = code.get("tokenid");
        return arr;

    }

    /*判断验证码是否正确*/
    boolean ifTrueofcode(String code,String tokenid) {
        boolean flag = false;
        String redisCode = redisCache.getCacheObject("sys:login:verificationcode:"+code.toLowerCase()+"_"+tokenid.toLowerCase());
        if(StringUtils.isEmpty(redisCode)){
            return false;
        } else if(StringUtils.isNotEmpty(redisCode) && "code".equals(redisCode)){
            return true;
        }
        return flag;
    }

    /**
     * 校验登陆策略
     */
    private String validateLoginPolicy(Acct account) {
		String returnMsg="";
		if (account == null)returnMsg="用户名或者密码错误";
		int errorCount = redisLoginaPolicy.getLockCount("system",account.getLoginName());
		// 如果大于最大失败次数，锁定账号
		if (errorCount==-1 || errorCount >= loginPolicyCache.LOGIN_ERROR_NUM()) {
			returnMsg = "超过最大错误次数，账号被锁定";
			redisLoginaPolicy.lock("system",account.getLoginName());
		}else{
			redisLoginaPolicy.set("system",account.getLoginName());
			errorCount=errorCount+1;
			returnMsg = "登录错误次数:" + errorCount;
		}
		return returnMsg;
	}

    /**
     * 初始化用户登录信息
     *
     * @param sysAcct
     * @return
     */
    private ResultCodeReq initUser(Acct sysAcct) {
        /**HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();  //废除掉登陆前的session
        }*/
        // 重新创建session,防止登录前后session变化
        //request.getSession(true);
        CurrentAccount account = CurrentAccount.getCurrentAccount();
        account.setSessionId(request.getSession().getId());
        account.setId(sysAcct.getId());
        account.setLoginName(sysAcct.getLoginName());
        account.setName(sysAcct.getName());
        account.setRemoteHost(request.getRemoteAddr());
        account.setLastLoginTime(System.currentTimeMillis());
        account.setValid(true);
        writerLog(sysAcct.getLoginName(), "login", Constants.OPT_SUCCESS, request.getHeader("Referer"));
        //设置安全cookie
        log.info("current login Id=" + request.getSession().getId());


        if (sysAcct != null) {
            sysAcct.setRoles(sysAcctService.findSysRoles(sysAcct.getId()));
        }
        sysAcct.setLoginPwd("");
        List<String> pfs = initPfs();
        //移除的密码错误次数
        redisLoginaPolicy.delete("system",account.getLoginName());

        //PolicyCache.clearErrorCount(sysAcct.getLoginName());
        account = CurrentAccount.getCurrentAccount();
        SessionManager.putSession(account.getSessionId(), account, request);

        return new ResultCodeReq(Constants.OPERATION_SUCCESS, account.getSessionId(), sysAcct, pfs);
    }

    @Resource
    private SysFuncService sysFuncService;

    /**
     * 初始化用户权限
     * <p>
     * description :
     * wenjianfeng 2019年10月22日
     */
    private List<String> initPfs() {
        List<String> pfs = new ArrayList<String>();
        List<Func> sysFuncList = sysFuncService.findAll();
        Set<String> funcs = new HashSet<String>();
        for (Func sysFunc : sysFuncList) {
            if (sysFunc.getClazzName() != null) {
                funcs.add(sysFunc.getClazzName().concat("!").concat(sysFunc.getMethodName()));
            }
        }
        CurrentAccount.getCurrentAccount().setSysAllFuncs(funcs);
        List<Func> sysFuncs = sysAcctService.findSysFuncs(CurrentAccount.getCurrentAccount().getId());
        CurrentAccount.getCurrentAccount().getPfs().clear();
        for (Func sysFunc : sysFuncs) {
            if (sysFunc.getClazzName() != null)

                CurrentAccount.getCurrentAccount().getPfs().put(sysFunc.getClazzName().concat("!").concat(sysFunc.getMethodName()), "");
            //判断权限PATH是否为空，如果为null,则返回类路径和方法名称
            if (sysFunc.getPath() == null) {
                pfs.add(sysFunc.getClazzName().concat("!").concat(sysFunc.getMethodName()));
            } else {
                pfs.add(sysFunc.getPath());
            }
        }
        return pfs;
    }

    /**
     * 生成随机验证码
     * 返回Map<tokenid,code>
     */
    public Map<String, String> createYcode() {
        // 验证码字符个数
        int codeCount = 4;
        char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        // 创建一个随机数生成器类
        Random random = new Random();
        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        String randomCode = "";
        for (int i = 0; i < codeCount; i++) {
            // 得到随机产生的验证码数字。
            String strRand = String.valueOf(codeSequence[random.nextInt(36)]);
            // 将产生的四个随机数组合在一起。
            randomCode = randomCode + strRand;
        }
        // 返回值map创建
        Map<String, String> back_map = new HashMap<>();


        back_map.put("tokenid", String.valueOf(UUID.randomUUID()));
        back_map.put("code", randomCode);
        return back_map;


    }


}
