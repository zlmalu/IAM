package com.sense.am.model;

import java.util.Calendar;

public class TimePolicy {

	private Long month;
	private Long dayMonth;
	private Long dayWeek;
	private Long startTime;
	private Long endTime;
	private boolean isAllow;
	
	public Long getMonth() {
		return month;
	}
	public void setMonth(Long month) {
		this.month = month;
	}
	public Long getDayMonth() {
		return dayMonth;
	}
	public void setDayMonth(Long dayMonth) {
		this.dayMonth = dayMonth;
	}
	public Long getDayWeek() {
		return dayWeek;
	}
	public void setDayWeek(Long dayWeek) {
		this.dayWeek = dayWeek;
	}
	
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public boolean isAllow() {
		return isAllow;
	}
	public void setAllow(boolean isAllow) {
		this.isAllow = isAllow;
	}
	
	
	public boolean isMatch(){
		Calendar cal=Calendar.getInstance();
		if(month.intValue()!=0 && month.intValue()!=(cal.get(Calendar.MONTH)+1)){
			return false;
		}
		if(dayWeek.intValue()!=0){
			int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK)-1;
			if(dayOfWeek<=0)dayOfWeek=7;
			if(dayWeek.intValue()!=dayOfWeek){
				return false;
			}
		}
		if(dayMonth.intValue()!=0 && dayMonth.intValue()!=cal.get(Calendar.DAY_OF_MONTH)){
			return false;
		}
		long curHms=Long.parseLong(hmsFormat.format(cal.getTime()));
		if(curHms<startTime || curHms>endTime){
			return false;
		}
		return true;
	}
	
	java.text.SimpleDateFormat hmsFormat=new java.text.SimpleDateFormat("HHmmss");
	
	
}
