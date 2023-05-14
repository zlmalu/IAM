package com.sense.iam.api.model.sys;

import java.util.ArrayList;
import java.util.List;

import com.sense.core.db.DBField;
import com.sense.iam.model.am.Group;
import com.sense.iam.model.am.GroupDynamic;
import com.sense.iam.model.im.User;
import com.sense.iam.model.sys.Acct;
import com.sense.iam.model.sys.Role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class GroupReq {
	
	private Long id;
	
	@ApiModelProperty(value="唯一编码",example="0",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**用户组编码*/
	private String sn;
	/**用户组名称*/
	private String name;
	/**用户组描述*/
	private String remark;
	/**状态*/
	private Integer type;
	
	private List<GroupDynamic> dynamicList;
	

	@ApiModelProperty(value="用户组编码")
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	
	@ApiModelProperty(value="用户组名称")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ApiModelProperty(value="用户组描述")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@ApiModelProperty(value="状态")
	public Integer getType() {
		return type;
	}
	
	@ApiModelProperty(value="扩展属性 ，关联GroupDynamic")
	public List<GroupDynamic> getDynamicList() {
		return dynamicList;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	public void setDynamicList(List<GroupDynamic> dynamicList) {
		this.dynamicList = dynamicList;
	}
	
	@ApiModelProperty(hidden=true)
	public Group getGroup(){
		Group model=new Group();
		model.setId(this.getId());
		model.setSn(this.getSn());
		model.setName(this.getName());
		model.setRemark(this.getRemark());
		model.setType(this.getType());
		return model;
	}
	
}
