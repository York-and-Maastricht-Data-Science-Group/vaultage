package org.vaultage.demo.fairnet.gen;

import java.util.List;
import java.util.UUID;

public class Post  {
	private String id = UUID.randomUUID().toString();
	private String content;
	private boolean isPublic;

	// getter
	public String getId(){
		return this.id;
	}
	public String getContent() {
		return this.content;
	}
	public boolean getIsPublic() {
		return this.isPublic;
	}

	// setter
	public void setId(String id){
		this.id = id;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	// operations
	
	
			
}