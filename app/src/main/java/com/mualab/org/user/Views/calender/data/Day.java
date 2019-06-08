package com.mualab.org.user.Views.calender.data;

public class Day {
	
	public int dayId;
	public String dayName;
	public boolean isAvailable;

	public Day() {
	}

	private int mYear;
	private int mMonth;
	private int mDay;
	
	public Day(int year, int month, int day){
		this.mYear = year;
		this.mMonth = month;
		this.mDay = day;
	}
	
	public int getMonth(){
		return mMonth;
	}
	
	public int getYear(){
		return mYear;
	}
	
	public int getDay(){
		return mDay;
	}

}
