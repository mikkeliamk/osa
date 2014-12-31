package fi.mamk.osa.utils;

import java.util.Comparator;
import fi.mamk.osa.auth.Role;

public class RoleComparator extends BaseComparatorUtils implements Comparator<Role> {
	public static String NAME 		= "name";
	
	private String param 	= "name";
	private int order 		= 1;
	
	/** Sorts roles by name property with ascending order.
	 * 
	 */
	public RoleComparator() {
	}
	
	/**	Sorts roles by given property with ascending order.
	 * 
	 * @param parameter		Role property to sort by
	 */
	public RoleComparator(String parameter) {
		this.param = parameter;
	}
	
	/**
	 * 
	 * @param parameter		Role property to sort by
	 * @param reverse		Order of the sort. <b>True = descending, false = ascending</b>
	 */
	public RoleComparator(String parameter, boolean reverse) {
		this.param = parameter;
		this.order = (reverse) ? -1 : 1;
		
	}
	
	public int compare(Role role1, Role role2) {
		Integer isNull = checkNull(role1, role2); 
		if (isNull == null) {
			switch (param) {
				case "name":
					isNull = checkNull(role1.getName(), role2.getName());
					if (isNull == null) {
						return role1.getName().toLowerCase().compareTo(role2.getName().toLowerCase()) * order;
					}
					return isNull * order;
				default: 
					return role1.getName().toLowerCase().compareTo(role2.getName().toLowerCase()) * order;
			}
		} else {
			return isNull.intValue() * order;
		}
	}
}
