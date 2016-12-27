/* 
 * @(#)       HashWordIdx.java
 * 
 * Created    Sat Oct 18 23:34:28 2003
 * 
 * Copyright  2003 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;

import java.io.*;
import java.util.*;


public class HashWordIdx {

	private Hashtable<String,Integer> wordIdx; // key: word string, value: word index; idx is the order of inputing word into the hashtable

	private int largest; //The number of (key, index) pairs in wordIdx

	public HashWordIdx() {
		this(10);
	}

	public HashWordIdx(int i) {
		wordIdx = new Hashtable<String,Integer> (i+1);
		largest = 0;
	}

	public void put(String word) {
		Integer idx = wordIdx.get(word);
		// if word has not appeared before, then create new distance vector
		if  (idx == null){ 
			idx = new Integer(largest);
			wordIdx.put(word, idx);
			largest++;
		}
	}

	public int get(String word){
		return wordIdx.get(word).intValue(); 
	}


	public void remove(String word) {
		wordIdx.remove(word);
	}

	public Enumeration keys() {
		return wordIdx.keys();
	}

	public int size() {
		return wordIdx.size();
	}

}
