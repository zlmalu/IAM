package com.sense.sdk.saml;

import org.joda.time.DateTime;

import com.sense.core.util.StringUtils;

public class TimeUtils {

	public static DateTime transformTimeZone(DateTime dateTime, String timeZone) {
		if (!StringUtils.isEmpty(timeZone)) {
			if (timeZone.indexOf("-") != -1) {
				timeZone = timeZone.replace("-", "").trim();
				double timeZoneDouble = Double.valueOf(timeZone);
				Double times = timeZoneDouble * 60 * 60 * 1000;
				dateTime = dateTime.minus(times.longValue());
			} else {
				timeZone = timeZone.replace("+", "").trim();
				double timeZoneDouble = Double.valueOf(timeZone);
				Double times = timeZoneDouble * 60 * 60 * 1000;
				dateTime = dateTime.plus(times.longValue());
			}
		}
		return dateTime;
	}

	public static void main(String[] args) {
		DateTime dateTime = transformTimeZone(new DateTime(), "-12");
		System.out.println(new DateTime().toDate());
		System.out.println(dateTime.toDate());
	}
}
