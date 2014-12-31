package fi.mamk.osa.ui;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	
	private String id;
	private List<TreeNode> children;
	private String name;
	private String type;
	private boolean load_on_demand;
	

	public TreeNode(){
		this(null,null);
		setLoadOnDemand(true);
	}
	public TreeNode(String id, String name){
		this.setId(id);
		this.setName(name);
		setLoadOnDemand(true);
		children = new ArrayList<TreeNode>();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void addChild(TreeNode child){
		for(TreeNode n : children){
			if(n.id.equals(child.id)){
				return;
			}
		}
		this.children.add(child);
	}
	public List<TreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isLoadOnDemand() {
		return load_on_demand;
	}
	public void setLoadOnDemand(boolean loadOnDemand) {
		load_on_demand = loadOnDemand;
	}

}
