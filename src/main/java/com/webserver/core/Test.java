package com.webserver.core;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Test {
	public static void main(String[] args) throws UnsupportedEncodingException {
		String str = "·¶";
		byte[] data = str.getBytes("UTF-8");
		System.out.println(data.length);
		System.out.println(Arrays.toString(data));
	}
}
