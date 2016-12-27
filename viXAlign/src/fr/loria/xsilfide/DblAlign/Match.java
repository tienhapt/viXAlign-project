/* 
 * @(#)       Match.java
 * 
 * Created    Mon Jun 14 09:34:28 2004
 * 
 * Copyright  2004 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;

import java.io.*;
import java.util.*;


public class Match {

	public static final int NB_LATER = 2;

	public float matchCost;
	public Vector<BitSet> movePath; //Path of optimal matched items (maybe divs)
	public final int ls, lt; //length of source and target vectors, are they vectors of positions of any word??
	private static float[][] mc = null;

	public static float [] countArray = null;

	//Two vector to check as if they are matched together?? the answer is yes
	private DVec sdvec;
	private DVec tdvec;

	public float eps;
	public float correlation;
	public int srcIdx;
	public int tarIdx;
	public int [] otherTar; //the target element of other Match objects which have the same srcIdx. 
	                        //But the number of this array does not exceeds NB_LATER

	public Match(DVec sdvec, DVec tdvec, int swordIdx, int twordIdx, float eps, float maxScore) {
		this.sdvec = sdvec;
		this.tdvec = tdvec;
		this.eps = eps;
		this.srcIdx = swordIdx;
		this.tarIdx = twordIdx;
		ls = sdvec.size()-1;
		lt = tdvec.size()-1; // nb of considered distances 
		otherTar = new int[NB_LATER];
		for (int i=0; i<NB_LATER; i++) otherTar[i] = -1;
		match(maxScore);
	}

	//Free the memory
	public void freeMem () {
		sdvec = tdvec = null;
	}

	public static void initCostMatrix(int maxx, int maxy){
		System.gc();
		mc = new float[maxx+1][maxy+1];
		int max = TextStructure.source.nbWords;
		// 	if (TextStructure.target.nbWords >= max) {
		// 	    max = TextStructure.target.nbWords;
		// 	}
		countArray = new float [max];
	}

	public static void removeCostMatrix(){
		mc = null;
		System.gc();
	}

	// sdvec, tdvec : distance vectors with mean & standard deviation values
	// the cost function takes into account only the distances 
	// from the second element of each vector
	// Match two vector (s0, s1, ..., s_n) (source) and (t0, t1, ..., tm) (target)
	// n = ls, m = lt
	// As following (s_i0, t_j0),(s_i1, t_j1), .... (s_ik, t_jk) where i0=j0=0
	// i_(p+1)-1 <= i_p <= i_(p+1) and j_(p+1) - 1 <= j_p <= j_(p+1) and ik = n, j_k = m
	// and the matchCost = Sum(p=0, p<=k, p) |sdvec(s_p) - tdvec(t_p)| = min among all possible combinations
	private void match(float maxScore){
		float ds = 0, dt = 0;
		maxScore *= (ls+lt);
		if(ls+lt>10000)
			System.out.println("Matrix :\t"+ls+"\tx\t"+lt);
		mc[0][0] = 0;
		for(int i=0; i < ls+1; i++){
			float minCost = Float.MAX_VALUE;
			for(int j=0; j < lt+1; j++){
				float min_cost = Float.MAX_VALUE;
				float tmp;
				int type = 0;
				if(i+j==0) 
					continue;
				if((i>0)&&(j>0)){
					min_cost = mc[i-1][j-1];
					type = 1;
				}
				if(i>0){
					ds = sdvec.elementAt(i);
					if((tmp=mc[i-1][j])<min_cost){
						min_cost = tmp;
						type = 2;
					}
				}
				if(j>0){
					dt = tdvec.elementAt(j);
					if((tmp=mc[i][j-1])<min_cost){
						min_cost = tmp;
						type = 3;
					}
				}
				mc[i][j] = min_cost + (float)Math.abs(ds-dt);
				if (mc[i][j] < minCost) minCost = mc[i][j];
				// 		if(type==1 && i>0 && j>0 && i<ls && j<lt && (Math.ceil(ds) != Math.ceil(dt))) {
				// 		    mc[i][j] = 10000;
				// 		}
			}
			if (minCost > maxScore) {
				matchCost = Float.MAX_VALUE;
				movePath = null;
				return;
			}
		}
		matchCost = (float)mc[ls][lt]/(ls+lt); // normalised cost
		if(matchCost > Parameters.getFloat("MAX_COST"))
			movePath = null;
		else {
			movePath = getMovePath();
		}
		if (movePath != null) correlation = correlation();
		else correlation = 0;
	}

	//Get the path to the path of best match
	//this path contained pairs (true, false), (false, true) and (true, true) 
	//indicate the match in matrix build on sdvec and tdvec
	//in which the elements label by 0, 1, ..., ls and 0, 1, ..., lt 
	private Vector<BitSet> getMovePath(){
		int i = ls, j = lt;
		Vector<BitSet> temp = new Vector<BitSet> ();
		while((i!=0)&&(j!=0)){
			BitSet bs = new BitSet(2);
			float prev0 = mc[i-1][j-1];
			float prev1 = mc[i-1][j];
			float prev2 = mc[i][j-1];
			if ((prev0<prev1) && (prev0<prev2)){
				bs.set(0, true);
				bs.set(1, true);
				i--; j--;
			}
			else if ((prev1<prev0)&&(prev1<prev2)){
				bs.set(0, true);
				bs.set(1, false);
				i--;
			}
			else {
				bs.set(0, false);
				bs.set(1, true);
				j--;
			}
			temp.insertElementAt(bs, 0);
		}

		return temp;
	}

	//Get the path to the path of best match
	//this path contained pairs (i, j)
	//indicate the match in matrix build on sdvec and tdvec
	//in which the elements label by positions of element is context
	public Vector<Point> getPath(){
		int n = movePath.size();
		Vector<Point> p = new Vector<Point>();
		int i = 0, j = 0, cpt = 0;
		for (int idx=0; idx < n; idx++){
			BitSet bs = movePath.elementAt(idx);
			if (bs.get(0)) {
				i++;
			}
			if (bs.get(1)) {
				j++;
			}
			if(bs.get(0) && bs.get(1)) {
				p.addElement(new Point(i, j));
			} //What if one among two bs.get(0) or bs.get(1) is false
		}
		return p;
	}

	public Vector<Point> getPosPath(int [] srcPosVec, int [] tarPosVec){
		Vector<Point> path = getPath();
		for (int i=0; i<path.size(); i++) {
			Point p = path.get(i);
			path.set(i, new Point(srcPosVec[p.x-1], tarPosVec[p.y-1]));
		}
		return path;
	}

	public float correlation () {
		Vector<Point> p = getPath();
		float covar = 0;
		float sigmax = 0;
		float sigmay = 0;
		float meanx = 0;
		float meany = 0;
		float count = 0;

		int cur=0;
		float prevPosX = 0;
		float prevPosY = 0;
		while (cur < p.size()) {
			float x = sdvec.elementAt(p.get(cur).x)-prevPosX;
			float y = tdvec.elementAt(p.get(cur).y)-prevPosY;
			prevPosX = sdvec.elementAt(p.get(cur).x);
			prevPosY = tdvec.elementAt(p.get(cur).y);
			sigmax += x*x;
			sigmay += y*y;
			meanx += x;
			meany += y;
			covar += x*y;
			count++;
			cur++;
		}
		meanx /= count;
		meany /= count;
		sigmax = (float)Math.sqrt(sigmax/count - meanx*meanx);
		sigmay = (float)Math.sqrt(sigmay/count - meany*meany);
		covar = covar/count - meanx*meany;
		return covar/(sigmax*sigmay);
	}
}
