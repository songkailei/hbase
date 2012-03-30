package com.song.index;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.song.document.DocumentAnalyzer;
import com.song.hbase.HbaseOperation;

public class IndexToHbase {
	
	HbaseOperation operation = new HbaseOperation();
	DocumentAnalyzer analyzer = new DocumentAnalyzer();
	
	/*
	 * Create HBase table. To see HbaseOperation.
	 * @param tableName
	 */
	public void createHbaseTable(String tableName)
	{
		operation.createTable(tableName);
	}
	
	/*
	 * Store inverted index to HBase, you can see the function insertTable of class HbaseOperation
	 * to know how to insert the Map<String,Set<Map<String,Integer>>> got from documents in folder to HBase.
	 * @param path The path of documents which need to be analyzed.
	 * @param tableName The HBase table name.
	 */
	public void dataToHbase(File path, String tableName)
	{
		Map<String,Set<Map<String,Integer>>> map = new HashMap<String,Set<Map<String,Integer>>>();
		if(path.isDirectory())
		{
			File files[] = path.listFiles();
			for(File file : files)
			{
				dataToHbase(file,tableName);
			}
		}
		else
		{
			map = analyzer.invertedIndex(path.getAbsolutePath());
			System.out.println("start to insert into hbase...");
			for(Entry<String,Set<Map<String,Integer>>> entry : map.entrySet())
			{
				//System.out.print("<"+entry.getKey()+",");
				Set<Map<String,Integer>> set = entry.getValue();
				//set.iterator()
				for(Iterator<Map<String, Integer>> it = set.iterator();it.hasNext();)
				{
					Map<String,Integer> map2 = it.next();
					for(Entry<String,Integer> entry2 : map2.entrySet())
					{
						//System.out.print("<"+entry2.getKey()+","+entry2.getValue()+">");
						operation.insertTable(tableName, entry.getKey(), entry2.getKey(), entry2.getValue());
					}
				}
				//System.out.println(">");
			}
		}
		
	}
	
	public static void main(String args[])
	{
		IndexToHbase th = new IndexToHbase();
		String folder = "E:\\lucene\\txt";
		File path = new File(folder);
		String tableName = "inverted";
		th.createHbaseTable(tableName);
		System.out.println("Create hbase table successfully, begin to analyzer the documents...");
		long start = System.currentTimeMillis();
		th.dataToHbase(path, tableName);
		long end = System.currentTimeMillis();
		System.out.println("Data are stored to hbase successfully, cost time: "+(end-start));
		
	}

}
