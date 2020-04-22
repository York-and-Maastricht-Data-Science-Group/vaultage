package org.rdbd.demo.fairnet;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Post {

	private String id;
	private String body;
	private boolean isPublic;

	public Post(String id, String body) {
		this.id = id;
		this.body = body;
		this.isPublic = (id.charAt(17) == 'T'? true : false);
	}
	
	public Post(String body, boolean isPublic) {
		this.isPublic = isPublic;
		this.id = (new SimpleDateFormat("yyyyMMddhhmmssSSS")).format(new Date()) + ((isPublic) ? "T" : "F");
		this.body = body; 

	}

	public String getId() {
		return id;
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
