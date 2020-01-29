package com.eg.realtimebus.activity.timetable;

import java.util.List;

/**
 * 响应给客户端的时刻表类
 * 
 * @author Administrator
 *
 */
public class ClientTimeTable {
	private String msg;
	private String topic;
	private List<String> timeList;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public List<String> getTimeList() {
		return timeList;
	}

	public void setTimeList(List<String> timeList) {
		this.timeList = timeList;
	}

	@Override
	public String toString() {
		return "TimeTable [msg=" + msg + ", topic=" + topic + ", timeList=" + timeList + "]";
	}

}
