package com.sense.iam.api.model.im;

public class OrgTree {
	
	private long hrid;
	private long id;
    private long parent_id;
    private String name;
    private String sn;
    private String name_path;
    private Integer usercount;
    
	public long getHrid() {
		return hrid;
	}
	public void setHrid(long hrid) {
		this.hrid = hrid;
	}
	public String getName_path() {
		return name_path;
	}
	public void setName_path(String name_path) {
		this.name_path = name_path;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getParent_id() {
		return parent_id;
	}
	public void setParent_id(long parent_id) {
		this.parent_id = parent_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public Integer getUsercount() {
		return usercount;
	}
	public void setUsercount(Integer usercount) {
		this.usercount = usercount;
	}
    
    

    

}
