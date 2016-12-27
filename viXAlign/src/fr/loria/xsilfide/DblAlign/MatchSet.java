/* 
 * @(#)       MatchSet.java
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


public class MatchSet {

	private HashDVec srcDVecs, tarDVecs;
	private Vector srcWords, tarWords;

	public float [] srcBestMatch;
	public float [] tarBestMatch;

	private Vector<Match> matchSet = null; //The element is ordered corresponding with their matchCost 
											//Reffered to this.add(Match) method

	public MatchSet(HashDVec srcDVecs, HashDVec tarDVecs, // vectors of word positions
			Vector srcWords, Vector tarWords // words vector
	) {
		this.srcDVecs = srcDVecs;
		this.tarDVecs = tarDVecs;
		this.srcWords = srcWords;
		this.tarWords = tarWords;
		srcBestMatch = new float[srcWords.size()];
		Arrays.fill(srcBestMatch, Float.MAX_VALUE);
		tarBestMatch = new float[tarWords.size()];
		Arrays.fill(tarBestMatch, Float.MAX_VALUE);
	}

	//Insert a match
	//The list of matches are ordered in increasing of matchCost
	public void add(Match m){
		if (m.matchCost > srcBestMatch[m.srcIdx] || m.matchCost > tarBestMatch[m.tarIdx]) return;
		if (m.matchCost < srcBestMatch[m.srcIdx]) srcBestMatch[m.srcIdx] = m.matchCost;
		if (m.matchCost < tarBestMatch[m.tarIdx]) tarBestMatch[m.tarIdx] = m.matchCost;
		if (matchSet == null){
			matchSet = new Vector<Match> ();
			matchSet.add(m);
		}
		else {
			int n = matchSet.size();
			int a = -1, b = n;
			int idx = 0;
			while (a+1<b) {
				idx = a + (b-a)/2;
				float c = matchSet.elementAt(idx).matchCost;
				if(c < m.matchCost)
					a = idx;
				else if(c > m.matchCost)
					b = idx;
				else
					break;
			}
			if(a+1==b)
				idx = b;
			matchSet.insertElementAt(m, idx);
		}
		m.freeMem();
	} //end add

	public int size(){
		if (matchSet != null) return matchSet.size();
		return 0;
	}

	// resulting the best matches
	public void process(){
		if (matchSet == null) return;
		int MAX_RESULTS = 1;

		int [] bs1 = new int [srcWords.size()]; //b maybe the abbreviation of "best"??
		for (int i=0; i<srcWords.size(); i++) bs1[i] = 0;
		int [] bs2 = new int [tarWords.size()];
		for (int i=0; i<tarWords.size(); i++) bs2[i] = 0;
		int j = 0;
		float prevCost = -10;

		while(j < matchSet.size()) {
			Match m = matchSet.elementAt(j);
			float cost = m.matchCost;
			int srcIdx = m.srcIdx;
			int tarIdx = m.tarIdx;

			if(cost == prevCost) {
				int presi = matchSet.elementAt(j-1).srcIdx; //@presi: previous source indexed i 
				int preti = matchSet.elementAt(j-1).tarIdx; //@preti: previous target indexed i
				if((presi==srcIdx)||(preti==tarIdx)){
					bs1[srcIdx]++;
					bs2[tarIdx]++;
					j ++;
					continue;
				}
			}

			if (bs1[srcIdx] >= MAX_RESULTS || bs2[tarIdx] >= MAX_RESULTS){
				matchSet.removeElementAt(j);
				//bs1[srcIdx]++;
				//bs2[tarIdx]++;
				continue;
			}
			
			int count_later = 0;
			
			for (int k=j+1; k<matchSet.size() && count_later<Match.NB_LATER; k++) {
				if (matchSet.elementAt(k).srcIdx == srcIdx) {
					m.otherTar[count_later] = matchSet.elementAt(k).tarIdx;
					count_later++;
				}
			}

			bs1[srcIdx]++;
			bs2[tarIdx]++;	    
			j ++;
			prevCost = cost;

		}
		return;
	} // end process


	public void recordLinks() {
		if (matchSet == null) return;
		int n = matchSet.size();
		for (int i=0; i<n/5; i++){ //Why take the portion of one fith??
			Match m = matchSet.elementAt(i);
			int si = m.srcIdx;
			int ti = m.tarIdx;
			Vector <Point> p = m.getPosPath(srcDVecs.getPosVec(si), tarDVecs.getPosVec(ti));
			int cur=0;
			while (cur < p.size()) {
				int srcP = p.get(cur).x;
				int tarP = p.get(cur).y;
				TextStructure.addLexicalLink(srcP, tarP, 1);// /((float)p.size());    
				cur++;
			}
		}
	}

	// convert the matchSet to String
	// matchSet = vector of (i) match (ii) srcIdx (iii) tarIdx 
	public void print (PrintWriter out){
		int n = matchSet.size();
		for (int i=0; i<n; i++){
			String tmp = "";
			Match m = matchSet.elementAt(i);
			int si = m.srcIdx;
			int ti = m.tarIdx;
			out.printf("%s%4d. %s - %s (Cost: %.6f  eps: %.6f  correl: %.6f) ", ((i<n/4) ? "*" : " "), i, (String)srcWords.elementAt(si), (String)tarWords.elementAt(ti), m.matchCost, m.eps, m.correlation);

			// AFFICHAGE COLLOCATIONS
			out.print("  (");
			Vector <Point> p = m.getPosPath(srcDVecs.getPosVec(si), tarDVecs.getPosVec(ti));

			Arrays.fill(Match.countArray,0);
			for (int cur=0; cur < p.size(); cur++) {
				int srcP = p.get(cur).x;
				for (int o=srcP-2; o<srcP+3; o++) {
					int t = TextStructure.source.getWordAt(o);
					if (o != srcP && t >= 0) Match.countArray[t]++;
				}
			}
			for (int k=0; k<Match.countArray.length; k++) {
				if (Match.countArray[k] != 0) {
					Match.countArray[k] = (float)Math.log(Match.countArray[k]) * (float)Math.log((TextStructure.source.nbWords*Match.countArray[k])/(((float)p.size())*srcDVecs.getFreq(k)));
					// Match.countArray[k] /= (float)p.size()*(float)Math.log1p(srcDVecs.getFreq(k));
					Match.countArray[k] /= (float)p.size()*(float)srcDVecs.getFreq(k);
				}
			}
			int [] best = new int [Match.NB_LATER];
			getBest(Match.countArray, best);
			for (int j = 0; j<Match.NB_LATER; j++) {
				if (best[j]>=0) out.printf("%s(%10.6f)", (String)srcWords.elementAt(best[j]), Match.countArray[best[j]]*srcDVecs.getFreq(best[j]));
				// 
				if (j<Match.NB_LATER-1 && best[j+1]>=0) out.print(", ");
			}
			out.print(") (");

			Arrays.fill(Match.countArray,0);
			for (int cur=0; cur < p.size(); cur++) {
				int tarP = p.get(cur).y;
				for (int o=tarP-2; o<tarP+3; o++) {
					int t = TextStructure.target.getWordAt(o);
					if (o != tarP && t >= 0) Match.countArray[t]++;
				}
			}
			for (int k=0; k<Match.countArray.length; k++) {
				if (Match.countArray[k] != 0) {
					Match.countArray[k] = (float)Math.log(Match.countArray[k]) * (float)Math.log((TextStructure.target.nbWords*Match.countArray[k])/(((float)p.size())*srcDVecs.getFreq(k)));
					// Match.countArray[k] /= (float)p.size()*(float)Math.log1p(tarDVecs.getFreq(k));
					Match.countArray[k] /= (float)p.size()*tarDVecs.getFreq(k);
				}
			}
			best = new int [Match.NB_LATER];
			getBest(Match.countArray, best);
			for (int j = 0; j<Match.NB_LATER; j++) {
				if (best[j]>=0) out.printf("%s(%10.6f)", (String)tarWords.elementAt(best[j]), Match.countArray[best[j]]*tarDVecs.getFreq(best[j]));
				//
				if (j<Match.NB_LATER-1 && best[j+1]>=0) out.print(", ");
			}
			out.print(")\n");


			// AFFICHAGE RESULTATS SUIVANTS
			// 	    for (int j = 0; j<Match.NB_LATER; j++) {
			// 		if (m.otherTar[j]>=0) out.print((String)tarWords.elementAt(m.otherTar[j]));
			// 		if (j<Match.NB_LATER-1 && m.otherTar[j+1]>=0) out.print(", ");
			// 	    }
			// 	    out.print(")\n");

			// AFFICHAGE DIVISIONS ALIGNEES
			//   	    Vector <Point> p = m.getPosPath(srcDVecs.getPosVec(si), tarDVecs.getPosVec(ti));
			//   	    int cur=0;
			//   	    while (cur < p.size()) {
			// 		out.print("\t");
			//   		do {
			//   		    int srcP = p.get(cur).x;
			//   		    int tarP = p.get(cur).y;
			//   		    out.printf("<%s> - <%s>   /   ", TextStructure.source.getSegmentIdAt(srcP), TextStructure.target.getSegmentIdAt(tarP));
			//   		    cur++;
			//   		} while ((cur < p.size()) && (cur%5 != 0));
			//   		out.print("\n");
			//   	    }
			//   	    out.print("\n");
		}
		//return tmp;
	} //end print

	private void getBest (float [] cArray, int [] best) {
		Arrays.fill(best,0);
		for (int k=0; k<cArray.length; k++) {
			int l = best.length;
			while (l>0 && cArray[k] > cArray[best[l-1]]) l--;
			if (l<best.length) {
				for (int o=best.length-1; o>l; o--) best[o] = best[o-1];
				best[l] = k;
			}
		}

	}

}
