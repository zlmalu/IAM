package com.sense.iam.api.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * 人脸识别
 * @author ygd
 *
 */
public class FaceRep {

	String sn;
	String data;
	
	@ApiModelProperty(value="账号")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="人脸图片")
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
}
