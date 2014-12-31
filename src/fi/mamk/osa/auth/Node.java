package fi.mamk.osa.auth;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private String id;
	private List<Node> children;
	private String name;
	

	public Node(){
		this(null,null);
	}
	public Node(String id, String name){
		this.setId(id);
		this.setName(name);
		children = new ArrayList<Node>();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void addChild(Node child){
		for(Node n : children){
			if(n.id.equals(child.id)){
				return;
			}
		}
		this.children.add(child);
	}
	public List<Node> getChildren() {
		return children;
	}
	public void setChildren(List<Node> children) {
		this.children = children;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
