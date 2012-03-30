package com.song.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import com.song.index.Result;

public class Util {
	
	/*
	 * Sort HashMap use Collections' sort function and comparator, 
	 * at last output a sorted List.
	 * @param Map
	 * @return ArrayList
	 */
	public ArrayList<Result> sortMap(Map<String,Integer> map)
	{
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, comparator);
		ArrayList<Result> result = new ArrayList<Result>();
		for(Map.Entry<String, Integer> entry : list)
		{
			Result r = new Result();
			r.setDocument(entry.getKey());
			r.setFrequent(entry.getValue());
			result.add(r);
		}
		return result;
	}
	
	private final static Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>(){
		@Override
		public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
			// TODO Auto-generated method stub
			return o2.getValue()-o1.getValue();
		}
		
	};
	
	/*
	 * 对象作为key，则需要重写对象的equals和hashCode函数
	 */
	public static void main(String args[])
	{
		Hashtable<TestBean, Integer> numbers = new Hashtable<TestBean, Integer>();
		TestBean tb = new TestBean();
		tb.setA(1);
		tb.setB(2);
		numbers.put(tb, 1); 
		Integer n = numbers.get(tb); 
		System.out.println(n); 
		TestBean tb1 = new TestBean();
		tb1.setA(1);
		tb1.setB(2);
		n = numbers.get(tb1); 
		System.out.println(n); 
	}

}
