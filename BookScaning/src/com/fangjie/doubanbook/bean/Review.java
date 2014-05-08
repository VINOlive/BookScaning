package com.fangjie.doubanbook.bean;

/*
 * Create by Jim 13-10-7
 */

public class Review {
	//作者
	private String Author;
	//摘要
	private String Abstract;
	//内容
	private String Content;
	
	public String getAuthor() {
		return Author;
	}
	public void setAuthor(String author) {
		Author = author;
	}
	public String getAbstract() {
		return Abstract;
	}
	public void setAbstract(String abstract1) {
		Abstract = abstract1;
	}
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	
}
