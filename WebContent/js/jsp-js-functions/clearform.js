function clearForm(myFormElement) {
  var elements = myFormElement.elements;
  myFormElement.reset();
  for(var i=0; i<elements.length; i++) {
	  field_type = elements[i].type.toLowerCase();
	  switch(field_type) {
	    case "text":
	    case "password":
	    case "textarea":
	          case "hidden":
	
	      elements[i].value = "";
	      break;
	    case "radio":
	    case "checkbox":
	        if (elements[i].checked) {
	          elements[i].checked = false;
	      }
	      break;
	    case "select-one":
	    case "select-multi":
	                elements[i].selectedIndex = -1;
	      break;
	    default:
	      break;
	    }
  	}
}