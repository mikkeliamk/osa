package fi.mamk.osa.utils;

import java.util.Comparator;
import fi.mamk.osa.auth.User;

public class UserComparator extends BaseComparatorUtils implements Comparator<User> {
	public static String NAME 		= "name";
	public static String ROLE 		= "role";
	
	private String param = "name";
	private int order 	 = 1;
	
	public UserComparator(String parameter) {
		this.param = parameter;
	}
	
	/** 
	 * 
	 * @param parameter		User property to sort by
	 * @param reverse		Order of the sort. <b>True = descending, false = ascending</b>
	 */
	public UserComparator(String parameter, boolean reverse) {
		this.param = parameter;
		this.order = (reverse) ? -1 : 1;
		
	}
	
	public int compare(User u1, User u2) {
		Integer isNull = checkNull(u1, u2);
		if (isNull == null) {
			switch (param) {
				case "name":
					isNull = checkNull(u1.getCn(), u2.getCn());
					if (isNull == null) {
						return u1.getCn().toLowerCase().compareTo(u2.getCn().toLowerCase()) * order;
					}
					return isNull * order;
				case "role":
					isNull = checkNull(u1.getRole(), u2.getRole());
					if (isNull == null) {
						return u1.getRole().toLowerCase().compareTo(u2.getRole().toLowerCase()) * order;
					}
					return isNull * order;
				default: 
					return u1.getCn().toLowerCase().compareTo(u2.getCn().toLowerCase()) * 1;
			}
		} else {
			return isNull.intValue() * order;
		}
	}
}
