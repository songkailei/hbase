package com.song.index;

public class Result {
	
	public String document;
	public int frequent;
	public int score;
	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public int getFrequent() {
		return frequent;
	}
	public void setFrequent(int frequent) {
		this.frequent = frequent;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
    
	public boolean equals(Object o)
	{
		if(this == o)return true;
		if(this.getClass() == o.getClass()){
			Result r = (Result)o;
			return r.document.equals(this.document);
		}
		return false;
	}

}
