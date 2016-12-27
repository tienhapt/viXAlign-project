/**
 * Skins.java : get interface element name
 * 
 * 
 * @author    Nguyen Thanh Bon (IFI - LORIA, 2003)
 * @version   
 */
package fr.loria.xsilfide.DblAlign;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.*;

public class Skins {
	Hashtable data = null;

	public Skins(String objectName, String fileName)     {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileName);

			data = new Hashtable();

			loadObjectSkin("common", document);
			loadObjectSkin(objectName, document);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadObjectSkin(String tagName, Document document)  {
		NodeList list = document.getElementsByTagName(tagName);
		if (list.getLength()==0) return;
		Element node = (Element)list.item(0);
		list = node.getElementsByTagName("object");
		int size = list.getLength();
		for (int i = 0; i<size; i++) {
			Element ele = (Element)list.item(i);
			data.put(ele.getAttribute("name"), ele.getAttribute("label"));
		}
	}

	public String getLabelOf(String objectName) {
		return (String)data.get(objectName);
	}

	public int getValueOf(String objectName) {
		return (new Integer((String)data.get(objectName))).intValue();
	}
}
