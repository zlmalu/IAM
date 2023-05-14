package com.sense.iam.portal.util;

import com.sense.iam.cam.Constants;
import com.sense.iam.cam.ResultCode;

/**
 * 弱密码校验类
 */
public class WeakPwdUtil {

    //定义竖向连续字符串校验规则
    public static String[] KEYBOARD_SLOPE_ARR = {
            "!qaz", "1qaz", "@wsx","2wsx", "#edc", "3edc", "$rfv", "4rfv", "%tgb", "5tgb",
            "^yhn", "6yhn", "&ujm", "7ujm", "*ik,", "8ik,", "(ol.", "9ol.", ")p;/", "0p;/",
            "+[;.", "=[;.",  "_pl,", "-pl,", ")okm", "0okm", "(ijn", "9ijn", "*uhb", "8uhb",
            "&ygv", "7ygv", "^tfc", "6tfc", "%rdx","5rdx", "$esz","4esz"
    };

    //定义横向连续字符串校验规则
    public static String[] KEYBOARD_HORIZONTAL_ARR = {
            "01234567890-=",
            "!@#$%^&*()_+",
            "qwertyuiop[]",
            "QWERTYUIOP{}",
            "asdfghjkl;'",
            "ASDFGHJKL:",
            "zxcvbnm,./",
            "ZXCVBNM<>?",
    };


    public static void main(String[] args) {
//        System.out.print(checkLateralKeyboardSite("qwe!@#$", 4, false).getMsg());
        System.out.println(checkKeyboardSlantSite("Password@123456789", 3, false).getMsg());
//        System.out.println(checkSequentialSameChars("123qqqq",5).getMsg());
//        System.out.println(checkSequentialChars("abcD", 4, false).getMsg());
    }

    /**
     * 校验字符串单字符禁止连续出现i次
     * @param str 字符串
     * @param i   禁止连续出现i次
     * @return    返回结果
     */
    public static ResultCode checkSequentialSameChars(String str, int i) {
        char[] charArr = str.toCharArray();
        int count = 0;
        int t = charArr[0];
        for (char c : charArr) {
            if (t == c) {
                count++;
                if (count == i - 1) {
                    return new ResultCode(Constants.OPERATION_FAIL, "新密码字符串单字符禁止连续出现" + i + "次");
                }
            } else {
                t = c;
                count = 0;
            }
        }
        return new ResultCode(Constants.OPERATION_SUCCESS);
    }

    /**
     * 键盘规则匹配器 横向连续检测
     * @param password      密码字符串
     * @param repetitions   检测连续出现次数
     * @param isLower       是否校验大小写
     * @return  返回结果
     */
    public static ResultCode checkLateralKeyboardSite(String password, int repetitions, boolean isLower) {
        String t_password = password;
        //将所有输入字符转为小写
        t_password = t_password.toLowerCase();
        int n = t_password.length();
        /*
          键盘横向规则检测
         */
        for (int i = 0; i + repetitions <= n; i++) {
            String str = t_password.substring(i, i + repetitions);
            String distinguishStr = password.substring(i, i + repetitions);

            for (String configStr : KEYBOARD_HORIZONTAL_ARR) {
                String revOrderStr = new StringBuffer(configStr).reverse().toString();

                //检测包含字母(区分大小写)
                if (isLower) {
                    //考虑 大写键盘匹配的情况
                    String UpperStr = configStr.toUpperCase();
                    if ((configStr.contains(distinguishStr)) || (UpperStr.contains(distinguishStr))) {
                        return new ResultCode(Constants.OPERATION_FAIL, "新密码禁止在键盘横向字符中连续出现" + repetitions + "次");
                    }
                    //考虑逆序输入情况下 连续输入
                    String revUpperStr = new StringBuffer(UpperStr).reverse().toString();
                    if ((revOrderStr.contains(distinguishStr)) || (revUpperStr.contains(distinguishStr))) {
                        return new ResultCode(Constants.OPERATION_FAIL, "新密码禁止在键盘横向字符中连续出现" + repetitions + "次");
                    }
                } else {
                    if (configStr.contains(str)) {
                        return new ResultCode(Constants.OPERATION_FAIL, "新密码禁止在键盘横向字符中连续出现" + repetitions + "次");
                    }
                    //考虑逆序输入情况下 连续输入
                    if (revOrderStr.contains(str)) {
                        return new ResultCode(Constants.OPERATION_FAIL, "新密码禁止在键盘横向字符中连续出现" + repetitions + "次");
                    }
                }
            }
        }
        return new ResultCode(Constants.OPERATION_SUCCESS);
    }



    /**
     * 物理键盘，斜向连接校验， 如1qaz,4rfv, !qaz,@WDC,zaq1 返回true
     * @param password    字符串
     * @param repetitions    重复次数
     * @param isLower        是否区分大小写 true:区分大小写， false:不区分大小写
     * @return boolean    如1qaz,4rfv, !qaz,@WDC,zaq1 返回true
     */
    public static ResultCode checkKeyboardSlantSite(String password, int repetitions, boolean isLower) {
        String t_password = password;
        t_password = t_password.toLowerCase();
        int n = t_password.length();
        /*
          键盘斜线方向规则检测
         */
        for(int i = 0; i+ repetitions <=n; i++) {
            String str = t_password.substring(i, i+ repetitions);
            String distinguishStr = password.substring(i, i+ repetitions);
            for (String configStr : KEYBOARD_SLOPE_ARR) {
                String revOrderStr = new StringBuffer(configStr).reverse().toString();
                //检测包含字母(区分大小写)
                if (isLower) {
                    //考虑 大写键盘匹配的情况
                    String UpperStr = configStr.toUpperCase();
                    if ((configStr.contains(distinguishStr)) || (UpperStr.contains(distinguishStr))) {
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止在键盘竖向字符中连续出现"+repetitions+"次");
                    }
                    //考虑逆序输入情况下 连续输入
                    String revUpperStr = new StringBuffer(UpperStr).reverse().toString();
                    if ((revOrderStr.contains(distinguishStr)) || (revUpperStr.contains(distinguishStr))) {
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止在键盘竖向字符中连续出现"+repetitions+"次");
                    }
                } else {
                    if (configStr.contains(str)) {
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止在键盘竖向字符中连续出现"+repetitions+"次");
                    }
                    //考虑逆序输入情况下 连续输入
                    if (revOrderStr.contains(str)) {
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止在键盘竖向字符中连续出现"+repetitions+"次");
                    }
                }
            }
        }
        return new ResultCode(Constants.OPERATION_SUCCESS);
    }

    /**
     * 检测a-z,z-a,0-9这样的连续字符,
     * @param password    字符串
     * @param repetitions    连续个数
     * @param isLower        是否区分大小写 true:区分大小写， false:不区分大小写
     * @return boolean    含有a-z,z-a连续字符串 返回结果
     */
    public static ResultCode checkSequentialChars(String password, int repetitions, boolean isLower) {
        String t_password = password;
        boolean flag = false;
        int limit_num = repetitions;
        int normal_count = 0;
        int reversed_count = 0;
        //检测包含字母(区分大小写)
        if (!isLower) {
            t_password = t_password.toLowerCase();
        }
        int n = t_password.length();
        char[] pwdCharArr = t_password.toCharArray();

        for (int i=0; i+limit_num<=n; i++) {
            normal_count = 0;
            reversed_count = 0;
            for (int j=0; j<limit_num-1; j++) {
                if (pwdCharArr[i+j+1]-pwdCharArr[i+j]==1) {
                    normal_count++;
                    if(normal_count == limit_num -1){
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止出现逻辑字符连续出现"+repetitions+"次");
                    }
                }

                if (pwdCharArr[i+j]-pwdCharArr[i+j+1]==1) {
                    reversed_count++;
                    if(reversed_count == limit_num -1){
                        return new ResultCode(Constants.OPERATION_FAIL,"新密码禁止出现逻辑字符连续出现"+repetitions+"次");
                    }
                }
            }
        }
        return new ResultCode(Constants.OPERATION_SUCCESS);
    }
}
