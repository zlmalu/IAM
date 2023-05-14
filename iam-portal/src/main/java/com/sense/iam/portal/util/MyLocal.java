package com.sense.iam.portal.util;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

public class MyLocal implements LocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
    	Cookie[] cookies = request.getCookies();
    	String lang = "zh_CN";
		for (Cookie cookie : cookies) {
			if(cookie.getName().equals("lang")){
				lang = cookie.getValue();
			}
		}
        Locale locale = Locale.getDefault();
        if (!StringUtils.isEmpty(lang)) {
            String[] s = lang.split("_");
            locale = new Locale(s[0], s[1]);
        }
		return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

    }
}

