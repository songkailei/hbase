package com.song.index;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

import com.song.hbase.HbaseOperation;
import com.song.tool.Util;

public class IndexSearcher {
	
	/*
	 * Use IKAnalyzer to parse user's input. Users's input maybe a sentence or some phrase not only words, 
	 * so if we just use users's input as rowKey to search HBase table sometimes we get nothing, this is not
	 * we want to see. Like Lucene we first parse users's input to some keywords, then use them to search.
	 * @param query User's search input.
	 * @return ArrayList<String> Result of keyword parsed.
	 */
	public ArrayList<String> queryParser(String query) throws IOException
	{
		Reader reader = new StringReader(query);
		IKSegmentation ks = new IKSegmentation(reader,true);
		Lexeme lex = null;
		ArrayList<String> list = new ArrayList<String>();
		while((lex = ks.next())!=null)
		{
			list.add(lex.getLexemeText());
		}
		return list;
	}
	
	/*
	 * According to the result of parsed query this function get all of the rows in HBase table, and then
	 * merge these rows together. Here i just accumulate the same documents' word frequent(Integer).
	 * @param ArrayList<String> Parsed query
	 * @param tableName 
	 * @param query Why this function still use this parameter? Because combine the list may be not equal query. 
	 * @return Map<String,Map<String,Integer>>
	 */
	public Map<String,Map<String,Integer>> result(ArrayList<String> list,String tableName,String query)
	{
		HbaseOperation op = new HbaseOperation();
		Map<String,Map<String,Integer>> r = new HashMap<String,Map<String,Integer>>();
		Map<String,Integer> result = new HashMap<String,Integer>();
		for(String rowKey:list)
		{
			Map<String,Map<String,Integer>> map = op.selectByRowKey(tableName, rowKey);
			for(Entry<String,Map<String,Integer>> entry:map.entrySet())
			{
				Map<String,Integer> map2 = entry.getValue();
				for(Entry<String,Integer> entry2:map2.entrySet())
				{
					if(result.containsKey(entry2.getKey()))
					{
						result.put(entry2.getKey(), result.get(entry2.getKey())+entry2.getValue());
					}
					else
					{
						result.put(entry2.getKey(), entry2.getValue());
					}
				}
			}
		}
		r.put(query, result);
		return r;
	}
	
	/*
	 * Sort the result by frequent of keyword. Because HashMap is disorder it 
	 * uses ArrayList<Result> to store the sorted inverted index.
	 * @param Map<String,Map<String,Integer>>
	 * @return ArrayList<Result>
	 */
	public ArrayList<Result> sortResult(Map<String,Map<String,Integer>> map)
	{
		Util u = new Util();
		ArrayList<Result> list = new ArrayList<Result>();
		for(Map.Entry<String, Map<String,Integer>> entry : map.entrySet())
		{
			Map<String,Integer> map2 = entry.getValue();
			list = u.sortMap(map2);
		}
		return list;
	}
	
	/*
	 * Only use the frequent which is accumulated simply is not enough. In order to sort the result
	 * this function use the Result object to express inverted index and add a score system. The 
	 * theory of the score system is that if a document contains more keywords user input it's score
	 * is higher. In this function i just +1 to the 'score' in object. At last, the result is sorted 
	 * by the score first, if two object have the same score then it will compare the frequent of keyword.
	 * @param ArrayList<String> 
	 * @param String tableName
	 * @return ArrayList<Result> Sorted result list.
	 */
	public ArrayList<Result> mulSortResult(ArrayList<String> list,String tableName)
	{
		HbaseOperation op = new HbaseOperation();
		ArrayList<Result> resultList = new ArrayList<Result>();
		for(String rowKey : list)
		{
			Map<String,Map<String,Integer>> map = op.selectByRowKey(tableName, rowKey);
			for(Entry<String,Map<String,Integer>> entry : map.entrySet())
			{
				Map<String,Integer> map2 = entry.getValue();
				for(Entry<String,Integer> entry2 : map2.entrySet())
				{
					Result result = new Result();
					result.setDocument(entry2.getKey());
					if(resultList.contains(result)){
						for(int i=0;i<resultList.size();i++){
							if(resultList.get(i).equals(result)){
								resultList.get(i).setFrequent(resultList.get(i).getFrequent()+entry2.getValue());
								resultList.get(i).setScore(resultList.get(i).getScore()+1);
							}
						}
					}
					else{
						result.setFrequent(entry2.getValue());
						result.setScore(1);
						resultList.add(result);
					}
				}
			}
		}
		Collections.sort(resultList, comparator);
		return resultList;
	}
	
	
	/*
	 * Multiple comparators
	 */
	 private final static Comparator<Result> comparator = new Comparator<Result>(){
			@Override
			public int compare(Result r1, Result r2) {
				// TODO Auto-generated method stub
				int t = 0;
				int a = r2.score - r1.score;
				if(a!=0){
					t = (a>0)?1:-1;
				}
				else{
					a = r2.frequent - r1.frequent;
					if(a!=0){
						t = (a>0)?1:-1;
					}
				}
				return t;
			}
			
		};
	
	public static void main(String args[])
	{
		IndexSearcher search = new IndexSearcher();
		String query = "喜鹊报喜，乌鸦报的却是忧难和灾祸";
		String tableName = "inverted";
		try {
			ArrayList<String> list = search.queryParser(query);
			for(String str:list)
			{
				System.out.print(str+"  ");
			}
			Map<String,Map<String,Integer>> map = search.result(list, tableName, query);
			ArrayList<Result> slist = search.sortResult(map);
			for(Result r : slist){
				System.out.println(r.getDocument()+"  "+r.getFrequent());
			}
			System.out.println("----------------------------");
			ArrayList<Result> resultList = search.mulSortResult(list, tableName);
			for(Result r : resultList){
				System.out.println(r.getDocument()+"  "+r.getFrequent()+"  "+r.getScore());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
