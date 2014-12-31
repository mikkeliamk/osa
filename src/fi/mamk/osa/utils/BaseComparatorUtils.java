package fi.mamk.osa.utils;

public class BaseComparatorUtils {
	
	/** Method to use within different kind of comparators to check whether object's property is null.
	 * 
	 * @param obj1	Object to compare
	 * @param obj2	Object to compare
	 * @return		Sort order or null
	 */
	public Integer checkNull(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return 0;
		} 
		else if(obj1 == null ) {
			return 1;
		}
		else if (obj2 == null) {
			return -1;
		}
		
		return null;
	}
}
