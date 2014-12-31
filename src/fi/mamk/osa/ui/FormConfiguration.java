package fi.mamk.osa.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

public class FormConfiguration implements Serializable {
    
    private static final long serialVersionUID = -4463644584001558172L;

    /**
     * Key              Form name
     * LinkedHashMap    FormElements
     */
    private LinkedHashMap<String, LinkedHashMap<String, FormElement>> forms = new LinkedHashMap<String, LinkedHashMap<String, FormElement>>();
    private LinkedHashMap<String, Vector<String>> tabs = new LinkedHashMap<String, Vector<String>>();
    
    public FormConfiguration () {
        
    }
    
    /**
     * @return all form names
     */
    public List<String> getAllForms() {
        List<String> list = new ArrayList<String>();
        Iterator<String> iter = forms.keySet().iterator();
        
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return list;
    }

    /**
     * @return the form by name
     */
    public LinkedHashMap<String, FormElement> getForm(String formName) {
        if (this.forms.containsKey(formName)) {
            return this.forms.get(formName);
        } else {
            return null;
        }
    }
    
    /**
     * @param formName  the form to set
     * @param lhm       form elements
     */
    public void setForm(String formName, LinkedHashMap<String, FormElement> lhm) {
        this.forms.put(formName, lhm);
    }
    
    public void setFormElement(String formName, FormElement el) {
        if (this.forms.get(formName) == null) {
            LinkedHashMap<String, FormElement> lhm = new LinkedHashMap<String, FormElement>();
            this.forms.put(formName, lhm);
        } 
        this.forms.get(formName).put(el.getName(), el);
    }
    
    public Vector<String> getTabs(String formName) {
        return this.tabs.get(formName);
    }
    
    public void setTab(String formName, String tabName) {
        if (this.tabs.get(formName) == null) {
            Vector<String> formTabs = new Vector<String>();
            this.tabs.put(formName, formTabs);
        }
        if (!tabs.get(formName).contains(tabName)) {
            this.tabs.get(formName).add(tabName);
        }
    }
}
