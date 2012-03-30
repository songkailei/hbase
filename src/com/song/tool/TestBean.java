package com.song.tool;

public class TestBean {
	public int a;
	public int b;
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	public int getB() {
		return b;
	}
	public void setB(int b) {
		this.b = b;
	}
	public boolean equals(Object o)
	{
		if(this == o)return true;
		if(this.getClass() == o.getClass()){
			TestBean b = (TestBean)o;
			if(this.a == b.a&&this.b==b.b)
				return true;
			else return false;
		}
		return false;
	}
	
	public int hashCode()
	{
		return this.a+"".hashCode();
	}
	

}
