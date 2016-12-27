
/* 
 * @(#)       LoadKVec.java
 * 
 * Created    Sat Oct 18 23:34:28 2003
 * 
 * Copyright  2003 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;

// import 
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;
import fr.loria.nguyen.mytools.*;

/**
 * Load and prepared 2 texts.
 * 
 * 
 * @author    Nguyen Thi Minh Huyen
 * @version   
 */

public class LoadKVec {
	// Element Properties : 
	static final short EMPTY  = 0;
	static final short IGNORE = 1;
	static final short PHRASE = 2;
	static final short PARAG  = 3;
	static final short DIV    = 4;
	static final short SEQ    = 5;
	static final short TRANSP = 6;
	static final short LEXUNIT = 7;
	static final short BODY = 8;

	static final String S_IGNORE = "IGNORE";
	static final String S_PHRASE = "PHRASE";
	static final String S_PARAG	= "PARAG";
	static final String S_DIV	= "DIV";
	static final String S_SEQ	= "SEQ";
	static final String S_TRANSP = "TRANSP";
	static final String S_LEXUNIT = "LEXUNIT";
	static final String S_BODY = "BODY";

	// public HashDVec to return word table : word index + position vector
	public HashDVec srcDVecs = new HashDVec(), tarDVecs = new HashDVec();

	// public Vector to return indexed word table: word vector
	public Vector srcWords = new Vector(), tarWords = new Vector();

	// source and target language
	String lsource ="", ltarget = "";

	// source and target length (in words)
	public int srcN, tarN;

	// source and target properties that maps tags -> types 
	String psource, ptarget;
	Properties prop;
	private Hashtable srcStopList, tarStopList; // list of non-considered words

	public LoadKVec(boolean smarkup, boolean tmarkup,  // true values if texts are segmented in lexical units
			String sfile, String tfile, // source text, target text
			String ps, String pt, // source XML properties, target XML properties
			String sStopList, String tStopList // source stop list, target stop list
	)  {
		psource = ps;
		ptarget = pt;
		//System.out.println("Run the LoadKVec");
		try {
			srcStopList = getStopList(sStopList);
			tarStopList = getStopList(tStopList);
		} catch (Exception e) {
			System.err.println("errors in getting stop lists :");
			e.printStackTrace();
			return;
		}
		loadAndPrepareTexts(smarkup, tmarkup, sfile, tfile);
	}

	private Hashtable getStopList(String fileName) throws Exception {
		Hashtable stopList = new Hashtable();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileName);
			NodeList list=document.getElementsByTagName("word");
			for(int i=0; i<list.getLength(); i++){
				Element elt = (Element)list.item(i);
				String word = elt.getAttribute("name");
				String pos = elt.getAttribute("pos");
				Vector pv = (Vector)(stopList.get(word));
				//System.out.println(word + "*****" + pos);
				if (pv == null){
					pv = new Vector();
				}
				pv.addElement(pos);
				stopList.put(word, pv);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}	
		return stopList;	
	}

	private final void loadAndPrepareTexts(boolean smarkup, boolean tmarkup, // true values if texts are segmented in lexical units
			String sfile, String tfile
	) {

		DblAlign.debug();

		XMLReader parser = null;

		try { 
			parser = XMLReaderFactory.createXMLReader(XMLTools.parserClass);
			//System.out.println("Sucessfull create XML Reader");
		}  catch (SAXException e) {
			//System.out.println("Can not create XML Reader");
			e.printStackTrace();
			System.exit(-1);
		}

		// The source document handler for SAX/XML
		//System.out.println("Run the loadAndPrepareTexts");
		KVMAHandler handler = new KVMAHandler(smarkup, 
				srcDVecs, 
				TextStructure.source,
				srcWords, 
				srcStopList
		);
		parser.setEntityResolver(handler);
		parser.setDTDHandler(handler);
		parser.setErrorHandler(handler);
		parser.setContentHandler(handler);

		// Reading the source XML text
		System.err.print("Parsing '" + sfile + "'...");
		try {
			InputSource is = new InputSource(FileIO.openLargeInput(sfile));
			is.setEncoding("UTF-8");
			initXMLProperties(psource);  // load the tag to type mapping
			is.setSystemId(sfile);      
			parser.parse(is);
			lsource = handler.lang; // ???
			srcN = handler.nwords;	
			//System.out.println("this is done");
		} catch(Exception e) {
			System.err.println("source: " + e);
			e.printStackTrace();
			// TODO
		}
		System.err.println("done.");
		if(handler.nospec.size() > 0) {
			System.err.println("Unspecified tags: " + handler.nospec.toString());
		}

		TextStructure.source.setFinalPos(handler.nchars, handler.nwords);

		// The target document handler for SAX/XML

		handler = new KVMAHandler(tmarkup, 
				tarDVecs, 
				TextStructure.target,
				tarWords, 
				tarStopList
		);

		// Reading the target text
		parser.setEntityResolver(handler);
		parser.setDTDHandler(handler);
		parser.setErrorHandler(handler);
		parser.setContentHandler(handler);

		System.err.print("Parsing '" + tfile + "'...");
		try {
			InputSource is = new InputSource(FileIO.openLargeInput(tfile));
			is.setEncoding("UTF-8");
			initXMLProperties(ptarget);
			is.setSystemId(tfile);
			parser.parse(is);
			ltarget = handler.lang;
			tarN = handler.nwords;
		} catch(Exception e) {
			System.err.println("target: " + e);
			e.printStackTrace();
			// TODO
		}
		System.err.println("done.");
		if(handler.nospec.size() > 0) {
			System.err.println("Unspecified tags: " + handler.nospec.toString());
		}

		TextStructure.target.setFinalPos(handler.nchars, handler.nwords);

	}


	// Methode qui initialise les Properties a partir du fichier entré en parametre   @Pat
	private final void initXMLProperties(String propFile) {
		prop = new Properties();
		try {
			FileInputStream f = new FileInputStream(propFile);
			prop.load(f);
		} catch (Exception e) {
			System.err.println("initXMLProperties: " + e);
			e.printStackTrace();
		}
	}

	/** 
	 * Return the context corresponding to the type of tag.
	 * 
	 * @param     name The name of the tag.
	 */
	public final short getContext(String name) {
		if (name == null) return EMPTY;
		//System.out.println("***" + name + "***");
		String p = prop.getProperty(name);
		//System.out.println("****" + p + "****");
		if (S_PHRASE.equals(p))
			return PHRASE;
		else if (S_LEXUNIT.equals(p))
			return LEXUNIT;
		else if (S_PARAG.equals(p))
			return PARAG;
		else if (S_DIV.equals(p))
			return DIV;
		else if (S_SEQ.equals(p))
			return SEQ;
		else if (S_TRANSP.equals(p))
			return TRANSP;
		else if (S_IGNORE.equals(p))
			return IGNORE;
		else if (S_BODY.equals(p))
			return BODY;
		else
			return EMPTY;
	}

	class KVMAHandler extends DefaultHandler {
		private boolean body = false;  // true if inside BODY element
		private boolean lexunit = false; // true if inside LEXUNIT element
		private boolean getContents = false; // true if inside lexunit AND must build lex
		private String id = "";  // id of the current element

		private short context; // the current context showing the current element

		private TextStructure structure;

		private HashDVec dVec;
		private Vector words;
		private HashWordIdx wordIdx;
		private boolean markup;
		private Hashtable stopList;

		private Stack <String> IDStack;
		private Stack <Integer> divNumStack;

		public String lang;		// language gleaned nom TEIHeader (if any).
		public Vector nospec;	// Tags for which a type wasn't defined.

		private int nignore = 0; // insiding some ignored elements
		public int nwords = 0;  // number of words
		public int nchars = 0;
		private String lex = ""; // current word

		public KVMAHandler(boolean markup,  // markup=true if lexical units are marked up
				HashDVec dVec, 
				TextStructure struct,
				Vector words, 
				Hashtable stopList
		)	{
			this.markup = markup;
			this.dVec = dVec;
			this.words = words;
			this.stopList = stopList;
			structure = struct;
			nospec = new Vector();
			wordIdx = new HashWordIdx();
			IDStack = new Stack <String> ();
			IDStack.push("");
			divNumStack = new Stack <Integer> ();
			divNumStack.push(new Integer(0));
		}


		public void startElement(String uri, String name, String qName, Attributes attrs) {

			if ("".equals (uri))
				name = qName;
			//System.out.println("**" + uri + "**");
			//System.out.println("**" + name + "**");
			//System.out.println("*" + qName + "*");
			context = getContext(name);

			// (id is now auto-generated)	    
			String theoryId = attrs.getValue("id");
			//System.out.println("ii" + theoryId + "ii");
			if(theoryId == null)
				theoryId = "";

			if(body) {
				try {
					switch (context) {
					case LEXUNIT:
						if (nignore > 0) 
							break;
						if (markup) {
							String lm = attrs.getValue("lemma");
							String ct = attrs.getValue("cat");
							lexunit = true;
							if (lm!=null && ct!=null) {
								lex = ct+"_"+lm;
								getContents = false;
							}
							else {
								lex = "";
								getContents = true;
							}
						}
						break;
					case PHRASE:
						if (nignore > 0)
							break;
						if (!markup){
							lex = "";
							lexunit = true;
							getContents = true;
						}
						int sNum = divNumStack.pop().intValue()+1;
						divNumStack.push(new Integer(sNum));
						id = IDStack.peek() + "s" + sNum;
						//System.out.println("**" + sNum + "**");
						IDStack.push(id);
						structure.addTextDiv(id, nchars, nwords);
						break;
					case PARAG:
						if (nignore > 0)
							break;
						int pNum = divNumStack.pop().intValue()+1;
						divNumStack.push(new Integer(pNum));
						divNumStack.push(new Integer(0));
						id = IDStack.peek() + "p" + pNum;
						IDStack.push(id);
						structure.addTextDiv(id, nchars, nwords);
						break;
					case DIV:
						if (nignore > 0)
							break;
						int dNum = divNumStack.pop().intValue()+1;
						divNumStack.push(new Integer(dNum));
						divNumStack.push(new Integer(0));
						id = IDStack.peek() + "d" + dNum;
						//System.err.printf("Theory Id : %s  /  Generated Id : %s\n", theoryId, id);
						IDStack.push(id);
						structure.addTextDiv(id, nchars, nwords);
						break;
					case SEQ:
					case TRANSP:
						break;
					case IGNORE:
						nignore++;
						break;
					default: // not done yet
						if(nospec.indexOf(name) == -1)
							nospec.addElement(name);
						break;
					}
				} catch(Exception e) {
					System.err.println("startElement: (name=" + name
							+ ", id=" + id + "): " + e);
					e.printStackTrace();
					System.exit(0);
				}
			}
			else if(name.equals("language")) // I write this just for TEI document
				lang = id;

			if (context==BODY) {
				body = true;
			}
		}

		public void endElement(String uri, String name, String qName)	{
			if ("".equals (uri))
				name = qName;

			context = getContext(name);
			if(context == BODY)
				body = false;

			if (body) try {
				switch (context) {
				case LEXUNIT:
					if (nignore > 0)
						break;
					if (markup) {
						Vector pv = (Vector)stopList.get(wordOnly(lex));
						int idx = -1;
						if (pv==null){
							wordIdx.put(lex);
							idx = wordIdx.get(lex);
							if(idx == words.size())
								words.add(lex);
							dVec.put(idx, nwords, id);
						}
						structure.addWord(idx, IDStack.peek());
						lexunit = getContents = false;
						nwords++;
					}
					break;
				case PHRASE:
					if (nignore > 0)
						break;
					if (!markup) {
						lexunit = getContents = false;
						tokenize(lex); // tokenizing lex and put in dVec
					}
					IDStack.pop();
					break;
				case PARAG:
				case DIV:
					if (nignore > 0)
						break;
					IDStack.pop();
					divNumStack.pop();
					break;
				case SEQ:
				case TRANSP:
					break;
				case IGNORE:
					nignore--;
					break;
				default: // not done yet
				}
			} catch (Exception e) {
				System.err.println("endElement: " + e);
				e.printStackTrace();
				System.exit(0);
			}
		}

		public void characters(char[] ch, int start, int length) {
			if(body){
				String s = new String(ch, start, length);
				if (lexunit && getContents)
					lex += s;
				if (lexunit && markup) nchars += s.length();
			}
		}

		private void tokenize(String s) {
			String delim = " .,?!:;\"\'()-";
			StringTokenizer st = new StringTokenizer(s, delim, true);
			while (st.hasMoreTokens()) {
				String lu = st.nextToken().trim();
				if (lu.length() > 0) {
					lu = lu.toLowerCase(new Locale("EN"));
					Vector pv = (Vector)stopList.get(lu);
					int idx = -1;
					if(pv == null){
						wordIdx.put(lu);
						idx = wordIdx.get(lu);
						if(idx == words.size())
							words.add(lu);
						dVec.put(idx, nwords, "w"+nwords);
					}
					structure.addWord(idx, IDStack.peek());
					nwords ++;
					nchars += lu.length();
				}
			} // end while
		} // end tokenize()

		private String wordOnly (String s) {
			int idx = s.length()-1;
			while (s.charAt(idx) != '_') {idx--;}
			return s.substring(idx+1, s.length());
		}

	}; 
}

// EOF LoadKVec
