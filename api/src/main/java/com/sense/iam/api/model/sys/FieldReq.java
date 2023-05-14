package com.sense.iam.api.model.sys;

import com.sense.iam.model.im.OrgType;
import com.sense.iam.model.sys.Field;

import io.swagger.annotations.ApiModelProperty;

public class FieldReq {

	private Long id;
	
	@ApiModelProperty(value="唯一编码",required=true)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public static Integer BASIC_FIELD_NO=2;
	public static Integer BASIC_FIELD_YES=1;

	/**字段名 */
	private String name;
	/**字段描述 */
	private String remark;
	/**控件类型 */
	private String inputType;
	/**字段类型 */
	private String type;
	/**字段长度 */
	private Integer len;
	/**排序号 */
	private Integer sortNum;
	/**是否基本字段 */
	private Integer isBasic;
	/**组件内容*/
	private String compant;
	/**验证格式*/
	private String regex;
	/**验证提示*/
	private String regexText;
	/**是否必填*/
	private Integer isRequired;
	/**默认值*/
	private String defaultValue;
	/**门户是否允许修改字段,默认值1*/
	private Integer isPortalEdit;
	/**是否唯一 1是，2否*/
	private Integer isUnique;
	
	@ApiModelProperty(value="是否唯一 1是，2否")
	public Integer getIsUnique() {
		return isUnique;
	}
	public void setIsUnique(Integer isUnique) {
		this.isUnique = isUnique;
	}
	
	public Integer getIsPortalEdit() {
		return isPortalEdit;
	}
	public void setIsPortalEdit(Integer isPortalEdit) {
		this.isPortalEdit = isPortalEdit;
	}
	public static Integer getBASIC_FIELD_NO() {
		return BASIC_FIELD_NO;
	}
	public static void setBASIC_FIELD_NO(Integer bASIC_FIELD_NO) {
		BASIC_FIELD_NO = bASIC_FIELD_NO;
	}
	public static Integer getBASIC_FIELD_YES() {
		return BASIC_FIELD_YES;
	}
	public static void setBASIC_FIELD_YES(Integer bASIC_FIELD_YES) {
		BASIC_FIELD_YES = bASIC_FIELD_YES;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getInputType() {
		return inputType;
	}
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getLen() {
		return len;
	}
	public void setLen(Integer len) {
		this.len = len;
	}
	public Integer getSortNum() {
		return sortNum;
	}
	public void setSortNum(Integer sortNum) {
		this.sortNum = sortNum;
	}
	public Integer getIsBasic() {
		return isBasic;
	}
	public void setIsBasic(Integer isBasic) {
		this.isBasic = isBasic;
	}
	public String getCompant() {
		return compant;
	}
	public void setCompant(String compant) {
		this.compant = compant;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public String getRegexText() {
		return regexText;
	}
	public void setRegexText(String regexText) {
		this.regexText = regexText;
	}
	public Integer getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(Integer isRequired) {
		this.isRequired = isRequired;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@ApiModelProperty(hidden=true)
	public Field getField(){
		Field field = new Field();
		field.setId(this.getId());
		field.setName(this.name);
		field.setRemark(this.remark);
		field.setInputType(this.inputType);
		field.setType(this.type);
		field.setLen(this.len);
		field.setSortNum(this.sortNum);
		field.setIsBasic(isBasic);
		field.setCompant(this.compant);
		field.setRegex(this.regex);
		field.setRegexText(this.regexText);
		field.setIsRequired(this.isRequired);
		field.setDefaultValue(this.defaultValue);
		
		if(this.getIsUnique()==null||this.getIsUnique()==0){
			field.setIsUnique(null);
		}else{
			field.setIsUnique(this.getIsUnique());
		}
		//设置默认值1
		if(this.getIsPortalEdit()==null||this.getIsPortalEdit()==0){
			field.setIsPortalEdit(1);
		}else{
			field.setIsPortalEdit(this.getIsPortalEdit());
		}
		return field;
	}
}
