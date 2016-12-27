
/* 
 * @(#)       Parameters.java
 * 
 * Created    Tue Jan 14 14:55:56 2003
 * 
 * Copyright  2003 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;


//import java.util.Properties;
//import java.util.Hashtable;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Set parameters for KvecAlign
 * 
 * 
 * @author    Nguyen Thi Minh Huyen
 * @version   
 */

public class Parameters {
	private static String pfileName;
	private static Hashtable<String, String> data;

	public Parameters() {
	}

	public static void init(String param){
		pfileName = param;
		reset();
	}

	public static void reset(){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(pfileName);
			data = new Hashtable();
			NodeList list=document.getElementsByTagName("param");
			for(int i=0; i<list.getLength(); i++){
				Element elt = (Element)list.item(i);
				data.put(elt.getAttribute("name"), elt.getAttribute("value"));
				//System.out.println(elt.getAttribute("name") + "*****" + elt.getAttribute("value"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static double getDouble(String name){
		return (new Double((String)data.get(name))).doubleValue();
	}

	public static float getFloat(String name){
		return (new Float((String)data.get(name))).floatValue();
	}

	public static int getInt(String name){
		return (new Integer((String)data.get(name))).intValue();
	}

	public static String getString(String name){
		return (String)data.get(name);
	}
}
