/* 
 * @(#)       HashDVec.java
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

public class HashDVec {

	private int[][] wordDVec; // [word index] -> word position vector. Why this has two dimensions ??
	private String[][] wordIdVec; // [word index] -> occurrence ids
	private int[] nbOcc; //nbOcc[i] indicates the number of occurrences of word[i] 
	private int maxIndex; //The max size of the vector, this can be changed in time
	private int nbWords; //The number of word in the first two 2-dis array
	public int maxFreq = 0; //The max appearance times of a word in all the texts
	public int maxFreqWord = 0;

	//Constructor initiates object with 10 elements
	public HashDVec() {
		this(10);
	}
	
	//Constructor initiate object with i elements
	public HashDVec(int i) {
		wordDVec = new int[i+1][];
		wordIdVec = new String[i+1][];
		nbOcc = new int[i+1];
		maxIndex = nbWords = 0;
		Arrays.fill(nbOcc, 0);
		Arrays.fill(wordDVec, null);
		Arrays.fill(wordIdVec, null);
	}

	//Add a new word to vector
	//word: the position to input an array
	//nb: the position of word
	//id: 
	public void put(int word, int nb, String id) {

		if (word > maxIndex) maxIndex = word;

		if (word >= wordDVec.length) { //The length of current vector is not enough, we double it's size
			//double size of wordDVec
			int[][] tmp = wordDVec;
			wordDVec = new int[2*tmp.length][];
			for (int i=0; i<tmp.length; i++) wordDVec[i] = tmp[i];
			for (int j=tmp.length; j<wordDVec.length; j++) wordDVec[j] = null;
			//double size of nbOcc
			int[] tmp2 = nbOcc;
			nbOcc = new int[2*tmp.length];
			for (int i=0; i<tmp2.length; i++) nbOcc[i] = tmp2[i];
			for (int i=tmp2.length; i<nbOcc.length; i++) nbOcc[i] = 0;
			//double size of wordIdVec
			String[][] tmp3 = wordIdVec;
			wordIdVec = new String[2*tmp.length][];
			for (int i=0; i<tmp3.length; i++) wordIdVec[i] = tmp3[i];
			for (int j=tmp3.length; j<wordIdVec.length; j++) wordIdVec[j] = null;
			//delete the temporary variable
			tmp = null;
			tmp2 = null;
			tmp3 = null;
			System.gc();
		}

		//initialize the array wordDVec if null
		if (wordDVec[word] == null) {
			wordDVec[word] = new int[8]; //Why is the number of elements of an 1-dim array is initiated at 8??
			wordIdVec[word] = new String[8];
			nbWords++;
		}
		
		if (nbOcc[word] == wordDVec[word].length) {
			//Double the size of wordDVec[word]
			int[] tmp = wordDVec[word];
			//System.out.println("print out the wordDVec[word]: " + tmp.length);
			wordDVec[word] = new int[2*tmp.length];
			for (int i=0; i<tmp.length; i++) wordDVec[word][i] = tmp[i];
			//double the size of wordIdVec[word]
			String[] tmp2 = wordIdVec[word];
			wordIdVec[word] = new String[2*tmp.length];
			for (int i=0; i<tmp2.length; i++) wordIdVec[word][i] = tmp2[i];
		}
		
		//input the word with position is nb and id is id 
		wordDVec[word][nbOcc[word]] = nb;
		wordIdVec[word][nbOcc[word]] = id;
		nbOcc[word]++; //increase the number occurrence by 1

		if (maxFreq < nbOcc[word]) {
			maxFreq = nbOcc[word];
			maxFreqWord = word;
		}
	}

	//remove a word in word position
	public void remove(int word) {
		if (wordDVec[word] != null) {
			nbWords--;
			wordDVec[word] = null;
			wordIdVec[word] = null;
			//nbOcc[word] = 0;
		}
	}

	//Get the word in it idx-th position
	//for example word "cat" occurs in 2-nd, 7-th and 8-th position
	//in the text, so its 3-th position is 8th
	public int getPosAt(int word, int idx){
		return wordDVec[word][idx];
	}

	public String getIdAt(int word, int idx){
		return wordIdVec[word][idx];
	}

	//Get the position vector
	public int[] getPosVec(int word){
		return wordDVec[word];
	}

	//divide the text into blocks of segSize size
	//the vector kvec is a vector which has kVal elements
	//element i equals 1 if the word occurs in i-th block
	public int[] getKVec(int word, int segSize, int kVal){
		int[] dw = wordDVec[word];
		int[] kvec = new int[kVal];
		for(int i=0; i<kVal; i++)
			kvec[i] = 0;
		for (int i=0; i<nbOcc[word]; i++){
			int cur = dw[i]/segSize;
			kvec[cur] = 1;
		}
		return kvec;
	}

	//
	public DVec getDVec(int word, TextStructure struct) {
		if (word < 0 || word > maxIndex || nbOcc[word] == 0) return null;
		int[] dw = wordDVec[word];
		DVec dvec = new DVec(nbOcc[word]); //+1);
		//dvec.addDistance(0);
		for (int i=0; i<nbOcc[word]; i++) {
			float d = struct.getProportionalPos(dw[i]);
			if (d>=0) dvec.addDistance(d);
		}
		dvec.finalized();
		return dvec;
	}

	public Enumeration keys() {
		return new HashDVecEnumeration();
	}

	public int size() {
		return nbWords;
	}

	public int getFreq(int word){
		return nbOcc[word]; 
	}

	public class HashDVecEnumeration implements Enumeration {
		private int cur;
		public HashDVecEnumeration () {
			cur = 0;
			toNextValid();
		}

		public boolean hasMoreElements () {
			return cur < maxIndex;
		}

		public Integer nextElement() {
			Integer res = new Integer(cur);
			cur++;
			toNextValid();
			return res;
		}

		private void toNextValid() {
			while ((cur < maxIndex) && (wordDVec[cur] == null)) cur++;
		}
	}

}
