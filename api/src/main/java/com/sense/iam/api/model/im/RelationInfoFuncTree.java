package com.sense.iam.api.model.im;


import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import com.sense.iam.model.im.AppFunc;

public class RelationInfoFuncTree {
	
	
	List<Long> checkedKeys;
    
    private List<AppFunc> element;

    @ApiModelProperty(value="已授权的权限ID集合")
	public List<Long> getCheckedKeys() {
		if(checkedKeys==null)checkedKeys=new ArrayList<Long>();
		return checkedKeys;
	}

	public void setCheckedKeys(List<Long> checkedKeys) {
		this.checkedKeys = checkedKeys;
	}
	@ApiModelProperty(value="树形对象")
	public List<AppFunc> getElement() {
		if(element==null)element=new ArrayList<AppFunc>();
		return element;
	}

	public void setElement(List<AppFunc> element) {
		this.element = element;
	}
    
    
   
   

    

}
