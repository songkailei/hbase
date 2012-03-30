package com.song.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

public class DocumentAnalyzer {
	
	
	
	
	/*
	 * Just test.
	 */
	public static void test() throws IOException
	{
		String str = "IKAnalyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
		//Reader reader = new BufferedReader(new InputStreamReader());
		Reader reader = new StringReader(str);
		IKSegmentation ks = new IKSegmentation(reader,true);
		Lexeme lex = null;
		while((lex = ks.next())!=null)
		{
			System.out.println(lex.getLexemeText());
		}
	}
	
	/*
	 * Get the word frequent of file.
	 * @param fileName
	 */
	public Map<String, Integer> frequent(String fileName)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			Reader reader = new BufferedReader(new FileReader(new File(fileName)));
			IKSegmentation ks = new IKSegmentation(reader,true);
			Lexeme lex = null;
			while((lex = ks.next())!=null)
			{
				//System.out.println(lex.getLexemeText());
				String fen = lex.getLexemeText();
				if(map.get(fen)==null)
				{
					map.put(fen, 1);
				}
				else
					map.put(fen, map.get(fen)+1);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	/*
	 * Get the inverted index of files and sort it into the old inverted index, the form of inverted index
	 * is Map<KeyWord, Set<Map<DocumentId, WordFrequent>>>.
	 * Why i did not use this function? Because the program always said 'out of memory', i guessed get all of the 
	 * inverted index one time into Map cost too much memory. So that i modified this function that when got 
	 * a file's inverted index, it insert the data into HBase table. But, at last, i found what i thought before was 
	 * not the reason of 'out of memory', it is that i do not close the pool!:-(
	 * DocumentId is the name of document.
	 * @param String fileName
	 * @param Map<String,Set<Map<String,Integer>>> the old inverted index.
	 * @return Map<String,Set<Map<String,Integer>>>
	 */
	public Map<String,Set<Map<String,Integer>>> invertedIndex(String fileName,Map<String,Set<Map<String,Integer>>> map)
	{
		//Map<String,Set<Map<String,Integer>>> map = new HashMap<String,Set<Map<String,Integer>>>();
		Map<String,Integer> frequent = frequent(fileName);
		fileName = fileName.substring(fileName.lastIndexOf("\\")+1 , fileName.length());
		for(Entry<String,Integer> entry : frequent.entrySet())
		{
			if(map.containsKey(entry.getKey()))
			{
					Map<String,Integer> fileMap = new HashMap<String,Integer>();
					fileMap.put(fileName, entry.getValue());
					Set<Map<String,Integer>> set = map.get(entry.getKey());
					set.add(fileMap);
					map.put(entry.getKey(), set);
			}
			else
			{
				if(map.get(entry.getKey())==null)
				{
					Map<String,Integer> fileMap = new HashMap<String,Integer>();
					fileMap.put(fileName, entry.getValue());
					Set<Map<String,Integer>> set = new HashSet<Map<String,Integer>>();
					set.add(fileMap);
					map.put(entry.getKey(), set);
				}
			}
		}
		return map;
	}
	
	/*
	 * Get the inverted index of files, the form of inverted index
	 * is Map<KeyWord, Set<Map<DocumentId, WordFrequent>>>.
	 * DocumentId is the name of document.
	 * @param String fileName
	 * @return Map<String,Set<Map<String,Integer>>>
	 */
	public Map<String,Set<Map<String,Integer>>> invertedIndex(String fileName)
	{
		Map<String,Set<Map<String,Integer>>> map = new HashMap<String,Set<Map<String,Integer>>>();
		Map<String,Integer> frequent = frequent(fileName);
		fileName = fileName.substring(fileName.lastIndexOf("\\")+1 , fileName.length());
		System.out.println(fileName);
		for(Entry<String,Integer> entry : frequent.entrySet())
		{
			if(map.containsKey(entry.getKey()))
			{
					Map<String,Integer> fileMap = new HashMap<String,Integer>();
					fileMap.put(fileName, entry.getValue());
					Set<Map<String,Integer>> set = map.get(entry.getKey());
					set.add(fileMap);
					map.put(entry.getKey(), set);
			}
			else
			{
				if(map.get(entry.getKey())==null)
				{
					Map<String,Integer> fileMap = new HashMap<String,Integer>();
					fileMap.put(fileName, entry.getValue());
					Set<Map<String,Integer>> set = new HashSet<Map<String,Integer>>();
					set.add(fileMap);
					map.put(entry.getKey(), set);
				}
			}
		}
		return map;
	}
	
	/*
	 * Get the inverted index of all files in the folder recursively.
	 * @param File path 
	 * @param Map<String,Set<Map<String,Integer>>> map
	 * @return Map<String,Set<Map<String,Integer>>>
	 */
	public Map<String,Set<Map<String,Integer>>> getAllInvertedIndex(File path,Map<String,Set<Map<String,Integer>>> map)
	{
		
		if(path.isDirectory())
		{
			File files[] = path.listFiles();
			for(int i=0;i<files.length;i++)
			{
				getAllInvertedIndex(files[i],map);
			}
		}
		else
		{
			System.out.println(path.getAbsolutePath());
			map = invertedIndex(path.getAbsolutePath(),map);
		}
		
		return map;
	}
	
	public static void main(String args[])
	{
//		try {
//			AnalyzerTest.test();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String fileName = "E:\\hbase\\test.txt";
//		DocumentAnalyzer analyzer = new DocumentAnalyzer();
//		Map<String, Integer> map = analyzer.frequent(fileName);
//		for(Entry<String, Integer> entry : map.entrySet())
//		{
//			System.out.println("Word: "+entry.getKey()+" Frequent:"+entry.getValue());
//			
//		}
		DocumentAnalyzer analyzer = new DocumentAnalyzer();
		File path = new File("E:\\hbase");
		Map<String,Set<Map<String,Integer>>> map = new HashMap<String,Set<Map<String,Integer>>>();
		map = analyzer.getAllInvertedIndex(path,map);
		for(Entry<String,Set<Map<String,Integer>>> entry : map.entrySet())
		{
			System.out.print("<"+entry.getKey()+",");
			Set<Map<String,Integer>> set = entry.getValue();
			//set.iterator()
			for(Iterator<Map<String, Integer>> it = set.iterator();it.hasNext();)
			{
				Map<String,Integer> map2 = it.next();
				for(Entry<String,Integer> entry2 : map2.entrySet())
				{
					System.out.print("<"+entry2.getKey()+","+entry2.getValue()+">");
				}
			}
			System.out.println(">");
		}
	}

}
