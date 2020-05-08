package org.rdbd.demo.fairnet;


import org.rdbd.demo.fairnet.util.FairnetUtil;

public class Post {

	private String id;
	private String title;
	private String body;
	private boolean isPublic;

	public Post(String id, String title, String body) {
		this.id = id;
		this.body = body;
		this.isPublic = true;
	}
	
	public Post(String title, String body) {
		this.id = FairnetUtil.getTimestamp();
		this.body = body;
		this.isPublic = true;
	}
	
	public Post(String title, String body, boolean isPublic) {
		this.isPublic = isPublic;
		this.id = FairnetUtil.getTimestamp();
		this.body = body;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

}
