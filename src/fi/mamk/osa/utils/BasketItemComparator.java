package fi.mamk.osa.utils;

import java.util.Comparator;
import fi.mamk.osa.ui.BasketItem;

public class BasketItemComparator extends BaseComparatorUtils implements Comparator<BasketItem>{
	public static String NAME 		= "name";
	public static String TYPE 		= "type";
	
	private String param 	= "name";
	private int order 		= 1;
	
	public BasketItemComparator() {
	}
	
	public BasketItemComparator(String parameter) {
		this.param = parameter;
	}
	
	/**
	 * 
	 * @param parameter		Role property to sort by
	 * @param reverse		Order of the sort. <b>True = descending, false = ascending</b>
	 */
	public BasketItemComparator(String parameter, boolean reverse) {
		this.param = parameter;
		this.order = (reverse) ? -1 : 1;
	}

	@Override
	public int compare(BasketItem basketItem1, BasketItem basketItem2) {
		Integer isNull = checkNull(basketItem1, basketItem2); 
		if (isNull == null) {
			switch (param) {
				case "name":
					isNull = checkNull(basketItem1.getName(), basketItem2.getName());
					if (isNull == null) {
						return basketItem1.getName().toLowerCase().compareTo(basketItem2.getName().toLowerCase()) * order;
					}
					return isNull * order;
				case "type":
					isNull = checkNull(basketItem1.getType(), basketItem2.getType());
					if (isNull == null) {
						return basketItem1.getType().toLowerCase().compareTo(basketItem2.getType().toLowerCase()) * order;
					}
					return isNull * order;
				default: 
					return basketItem1.getName().toLowerCase().compareTo(basketItem2.getName().toLowerCase()) * order;
			}
		} else {
			return isNull.intValue() * order;
		}
	}

}
