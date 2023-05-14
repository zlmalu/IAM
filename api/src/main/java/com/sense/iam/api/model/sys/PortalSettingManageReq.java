package com.sense.iam.api.model.sys;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sense.core.db.DBField;
import com.sense.core.serializer.JsonDate;
import com.sense.iam.model.sys.Event;
import com.sense.iam.model.sys.Message;
import com.sense.iam.model.sys.PortalSettingManage;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class PortalSettingManageReq {

	/**唯一标识*/
	private Long id;

	/*是否启动用户个性化*/
	private Integer isUserEnable;

	/*是否启动幻灯片*/
	private Integer isSlideEnable;

	private String temploginSlideImage1;
	/*图片类型*/
	private String temploginSlideImage2;
	/*图片类型*/
	private String temploginSlideImage3;

	/*幻灯片3*/
	private String templogoImage;

	private String copyright;

	private String title;


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private String theme;
	private String themeConfig;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getUserEnable() {
		return isUserEnable;
	}

	public void setUserEnable(Integer userEnable) {
		isUserEnable = userEnable;
	}

	public Integer getSlideEnable() {
		return isSlideEnable;
	}

	public void setSlideEnable(Integer slideEnable) {
		isSlideEnable = slideEnable;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getThemeConfig() {
		return themeConfig;
	}

	public void setThemeConfig(String themeConfig) {
		this.themeConfig = themeConfig;
	}

	public String getTemploginSlideImage1() {
		return temploginSlideImage1;
	}

	public void setTemploginSlideImage1(String temploginSlideImage1) {
		this.temploginSlideImage1 = temploginSlideImage1;
	}

	public String getTemploginSlideImage2() {
		return temploginSlideImage2;
	}

	public void setTemploginSlideImage2(String temploginSlideImage2) {
		this.temploginSlideImage2 = temploginSlideImage2;
	}

	public String getTemploginSlideImage3() {
		return temploginSlideImage3;
	}

	public void setTemploginSlideImage3(String temploginSlideImage3) {
		this.temploginSlideImage3 = temploginSlideImage3;
	}

	public String getTemplogoImage() {
		return templogoImage;
	}

	public void setTemplogoImage(String templogoImage) {
		this.templogoImage = templogoImage;
	}

	@ApiModelProperty(hidden=true)
	public PortalSettingManage getPortalSettingManage(){
		PortalSettingManage model=new PortalSettingManage();
		model.setId(this.getId());
		model.setTheme(this.getTheme());
		model.setCopyright(this.getCopyright());
		model.setSlideEnable(this.getSlideEnable());
		model.setUserEnable(this.getUserEnable());
		model.setTitle(this.getTitle());
		model.setThemeConfig(this.getThemeConfig());
		return model;
	}

}
