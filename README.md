HBase
=======================================

This project uses HBase to store inverted index. 

API:
---------------------------------------

First, create inverted index to HBase. 

You need to specify two variables, one is the folder which you want to index, the other is 

table name you want to store data in HBase.

```
IndexToHbase th = new IndexToHbase();
String folder = "E:\\lucene\\txt";
File path = new File(folder);
String tableName = "inverted";
th.createHbaseTable(tableName);
th.dataToHbase(path, tableName);	
```

Next, Search index data from HBase.

The IndexSearcher will first parse your query into list, and then search the result, 

at last, return result which is sorted by score.

```
IndexSearcher search = new IndexSearcher();
String query = "喜鹊报喜，乌鸦报的却是忧难和灾祸";
ArrayList<String> list = search.queryParser(query);
ArrayList<Result> resultList = search.mulSortResult(list, tableName);
```
		
Next Work:
---------------------------------------

1.This project i use analyzer which is write by myself to analyze document and IKAnalyzer to Word Segmentation, but i am worried the efficiency of analyzer, so next i will conside to integrate Lucene's Analyzer.
2.Rewrite the programe using Design pattern.
3.Research Partion By Document or Partion By Term in Hbase when the table is too big.