package com.song.hbase;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseOperation {
	
	public Configuration config;
	
	public HbaseOperation()
	{
		config = HBaseConfiguration.create();
		config.set("hbase.master", "xjtudlClient:60000");
		config.set("hbase.zookeeper.quorum", "xjtudlClient");
	}
	
	/*
	 * Create table, create column family "document".
	 * @param tableName
	 */
	public void createTable(String tableName)
	{
		try {
			HBaseAdmin admin = new HBaseAdmin(config);
			if(admin.tableExists(tableName))
			{
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
				System.out.println(tableName+" is exist, delete...");
			}
			HTableDescriptor descriptor = new HTableDescriptor(tableName);
			descriptor.addFamily(new HColumnDescriptor("document"));
			//descriptor.addFamily(new HColumnDescriptor("score"));
			admin.createTable(descriptor);
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Insert data into table. The keyword is as rowKey, and the column is document contains this keyword,
	 * the cell is keyword's frequent in this document.
	 * When the rowKey is not exist in HBase table, it insert the rowKey and column to table, or insert the column to the row.
	 * @param tableName
	 * @param column The document which contains key word as row key. 
	 * @param rowKey The key word
	 * @param frequent of keyword
	 */
	public void insertTable(String tableName,String rowKey,String column,int frequent)
	{
		System.out.println("insert....");
		HTablePool pool = new HTablePool(config,2000);
		HTable table = (HTable) pool.getTable(tableName);
		Put put = new Put(rowKey.getBytes());
		put.add(Bytes.toBytes("document"), column.getBytes(), Bytes.toBytes(frequent+""));
			//put.add(Bytes.toBytes("score"), null, Bytes.toBytes(i+""));
		try {
			table.put(put);
			table.close();
			pool.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * First, i want to say i made a mistake in class DocumentAnalyzer, the inverted index of files
	 * can be described with Map<String,Map<String,Integer>> easily, not Map<String,Set<Map<String,Integer>>>.
	 * OK, this function is to select row from HBase table by rowKey.
	 * @param tableName
	 * @param rowKey
	 * @return Map<String,Map<String,Integer>> inverted index of files
	 */
	public Map<String,Map<String,Integer>> selectByRowKey(String tableName,String rowKey)
	{
		Map<String,Map<String,Integer>> map = new HashMap<String,Map<String,Integer>>();
		try {
			HTable table = new HTable(config,tableName);
			Get get = new Get(rowKey.getBytes());
			Result r = table.get(get);
			Map<String,Integer> data = new HashMap<String,Integer>();
			for(KeyValue kv : r.raw())
			{
				data.put(new String(kv.getQualifier()), Integer.parseInt(new String(kv.getValue())));
			}
			map.put(rowKey, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	/*
	 * Get all result of table
	 * @param tableName
	 */
	public void QueryAll(String tableName)
	{
		HTablePool pool = new HTablePool(config,1000);
		HTable table = (HTable)pool.getTable(tableName);
		try {
			ResultScanner rs = table.getScanner(new Scan());
			for(Result r : rs)
			{
				System.out.println("Row key: " + new String(r.getRow()));
				for(KeyValue kv : r.raw())
				{
					System.out.println("Family:" + new String(kv.getFamily())+" Column:"+new String(kv.getQualifier())+" Value:"+new String(kv.getValue())+" Timestamp:"+ kv.getTimestamp());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		HbaseOperation hbase = new HbaseOperation();
		String tableName = "inverted";
		//hbase.createTable(tableName);
		//hbase.insertTable(tableName,"你好","世界",1);
		//hbase.insertTable(tableName,"你好","世界1",1);
		//hbase.insertTable(tableName,"你好1","世界",1);
		//hbase.QueryAll(tableName);
		Map<String,Map<String,Integer>> map = hbase.selectByRowKey(tableName, "我是谁");
		for(Entry<String, Map<String, Integer>> entry:map.entrySet())
		{
			System.out.println("----------"+entry.getKey()+"------------------");
			Map<String,Integer> map2 = entry.getValue();
			for(Entry<String,Integer> entry2:map2.entrySet())
			{
				System.out.println(entry2.getKey()+"  "+entry2.getValue());
			}
		}
	}

}
