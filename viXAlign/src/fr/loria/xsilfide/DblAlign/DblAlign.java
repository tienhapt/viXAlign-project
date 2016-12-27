/* 
 * @(#)       KVecAlign.java
 * 
 * Created    Tue Jan 14 14:55:56 2003
 * 
 * Copyright  2003 (C) Nguyen Thi Minh Huyen
 *            UMR LORIA (Universities of Nancy, CNRS & INRIA)
 *            
 */

package fr.loria.xsilfide.DblAlign;

import fr.loria.nguyen.mytools.*;

//import java.util.Properties;
//import java.util.Hashtable;
import java.util.*;
import java.io.*;

/**
 * This is the main class for the alignment tool using KVec algorithm.
 * 
 * 
 * @author    Nguyen Thi Minh Huyen
 * @version   
 */

public class DblAlign {

	//public static RoughAlign ra;
	public static MatchSet mset;

	public static PrintWriter log;

	private static String srcFileName;
	private static String tarFileName;

	public DblAlign(HashDVec srcDVecs, HashDVec tarDVecs, // vectors of word positions
			Vector srcWords, Vector tarWords, // words vector
			int srcN, int tarN,  // numbers of word occurences in two texts
			// String srcStopList, String tarStopList, // stoplist: non-considered words
			String outfile // result file name
	) {
//		if(srcWords == null || srcWords.size() == 0)
//			System.out.println("Err here");
		//System.err.printf ("%d (%s) - %d (%s)\n", srcDVecs.maxFreq, ((String)srcWords.get(srcDVecs.maxFreqWord)), tarDVecs.maxFreq, ((String)tarWords.get(tarDVecs.maxFreqWord)));
		
		//System.out.println("*****" + srcDVecs.maxFreq + "**" + tarDVecs.maxFreq);
		Match.initCostMatrix(srcDVecs.maxFreq, tarDVecs.maxFreq);

		PrintWriter out = null;

		log = null;

		//out.println("src-tokens = " + srcN + "\ttar-tokens = " + tarN);
		//out.println("language proportion corfficient = " + lpc);

		System.out.println("writing results ...");

		System.out.println("Numbers of words:\t" + srcDVecs.size() + "\t" + tarDVecs.size());
		//out.println("Numbers of words:\t" + srcDVecs.size() + "\t" + tarDVecs.size());

		//int count = 0; // number of saved word pairs

		// int [] levels = {0};

		int [] levels = {0, 1, 1, 2, 2, 3, 3};
		//int [] levels = {0, 2, 2, 3, 3, 3};

		//int [] levels = {0, 1, 1, 3, 3};

		for (int v=0; v <= levels.length; v++) {
			try {
				File tmp = new File("lex_align_" + v + ".txt");
				if (tmp != null) tmp.delete();
				tmp = new File("div_align_" + v + ".txt");
				if (tmp != null) tmp.delete();
				//System.out.println(tmp.getAbsolutePath());
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		for (int v=0; v < levels.length; v++) {
			int w = levels[v];

			out = FileIO.openOUT("D:/workspace/Arcade2/out/lex_align_" + v + ".txt", "UTF-8");
			if (out==null) {
				System.err.println("Could not open file " + "lex_align_" + v + ".txt" + " to print results.");
				return;
			}

			log = FileIO.openOUT("D:/workspace/Arcade2/out/div_align_" + v +".txt", "UTF-8");
			if (out==null) {
				System.err.println("Could not open file " + "div_align_" + v +".txt" + " to print results.");
				return;
			}

			out.printf("Source : %s\n", srcFileName);
			log.printf("Source : %s\n", srcFileName);
			out.printf("Target : %s\n", tarFileName);
			log.printf("Target : %s\n", tarFileName);


			TextStructure.alignSegments();

			if (log != null) {
				log.close();
				log = null;
			}

			TextStructure.setWorkingLevel(w);

			mset = new MatchSet(srcDVecs, tarDVecs, srcWords, tarWords);

			System.gc();

			float totalTextSize = TextStructure.source.getProportionalPos(srcN);
			float globalScaleFactor = ((float)srcN)/totalTextSize;

			System.err.println(globalScaleFactor);

			System.err.print("\n          ");

			int nbValidEps = 0;
			int nbEps = 0;
			double aveEps = 0;

			for(Enumeration se = srcDVecs.keys(); se.hasMoreElements(); ){
				int swordIdx = ((Integer)se.nextElement()).intValue();
				System.err.printf("\b\b\b\b\b\b\b\b\b\b%10d", swordIdx);
				DVec sdvec = srcDVecs.getDVec(swordIdx, TextStructure.source);
				int sn = sdvec.size();
				float sfirst = sdvec.elementAt(0);
				// filter: the frequency of the considered (source) word should be greater than MIN_FREQ
				if (sn > Parameters.getInt("MIN_FREQ")) {
					for(Enumeration te = tarDVecs.keys(); te.hasMoreElements(); ){
						int twordIdx = ((Integer)te.nextElement()).intValue();
						DVec tdvec = tarDVecs.getDVec(twordIdx, TextStructure.target);
						int tn = tdvec.size();

						// filter: the frequency of the considered (target) word should be greater than MIN_FREQ
						if (tn > Parameters.getInt("MIN_FREQ")) {

							float tfirst = tdvec.elementAt(0);

							// filter: the first position of two vectors shouldn't be 
							// greater than half of text length
							if(Math.abs(tfirst-sfirst)*2>=totalTextSize)
								continue;

							double eps = vectorLikelihoodScoreTest(sdvec, tdvec);
							eps *= globalScaleFactor;
							if (!(Double.isInfinite(eps) || Double.isNaN(eps))) {
								aveEps += eps;
								nbEps++;
							}
							if (eps <= Parameters.getFloat("MIN_EPS_SCORE")) {
								nbValidEps++;
								float maxScore = mset.srcBestMatch[swordIdx];
								if (mset.tarBestMatch[twordIdx] < maxScore) maxScore = mset.srcBestMatch[swordIdx];
								Match m = new Match(sdvec, tdvec, swordIdx, twordIdx, ((float)eps), maxScore); // for distance vectors
								if(m.movePath!=null
										//&& m.correlation > 0
								){
									mset.add(m); 
								}
							}
						}
						else
							tarDVecs.remove(twordIdx);
					} // end for tarDVecs
					//System.gc();
				}
				else
					srcDVecs.remove(swordIdx);
			} //end for srcDVecs

			//mset.print(out);
			System.out.println("\n" + mset.size() + " saved word pairs");
			out.println(mset.size()+" saved word pairs");
			mset.process();
			mset.recordLinks();
			//mset.print(out);

			out.println(mset.size()+" saved word pairs");
			//out.println(ra.toString(srcN));
			System.out.println(mset.size()+" saved word pairs");

			out.println("\n\n\n------------------------------------------------------------\n\n\n");
		}

		System.out.println("done ");
		out.close();
		Match.removeCostMatrix();
	}

	public DblAlign(boolean smarkup, boolean tmarkup,  // true values if texts are segmented in lexical units
			String sfile, String tfile, // source text, target text
			String ps, String pt, // source XML properties, target XML properties
			String srcStopList, String tarStopList, // source and target XML files of ignored words
			String out  // result
	)  {

		// Load and prepare texts

		LoadKVec lap = new LoadKVec(smarkup, tmarkup, sfile, tfile, ps, pt, srcStopList, tarStopList);

		TextStructure.cleanEmptyParagraphs();

		srcFileName = sfile;
		tarFileName = tfile;

		System.out.println(lap.srcN  + "\t " + lap.tarN);
		// Align two dvecs
		new DblAlign(lap.srcDVecs, lap.tarDVecs,
				lap.srcWords, lap.tarWords,
				lap.srcN, lap.tarN,
				//srcStopList, tarStopList, 
				out);
	}

	boolean freqRatioTest(int sn, int tn){     
		// filter: 0.5 <= sn/tn <= 2
		float r = ((float)sn)/tn;
		float MAX_FREQ_RATIO = Parameters.getFloat("MAX_FREQ_RATIO");
		if ((r <= MAX_FREQ_RATIO) && (r >= 1/MAX_FREQ_RATIO))
			return true;
		return false;
	}

	float vectorLikelihoodScoreTest(DVec sdvec, DVec tdvec) {
		// filter: Fung & McKeown(1997) eps(sword,tword) = sqrt((m_ds - m_dt)^2 - (sig_ds - sig_dt)^2) <= 200
		int sn = sdvec.size(), tn = tdvec.size();
		if(!freqRatioTest(sn,tn))
			return Float.MAX_VALUE;
		float mds = sdvec.mean;
		float mdt = tdvec.mean;
		float sds = sdvec.stdDev;
		float sdt = tdvec.stdDev;
		float eps = (float)Math.sqrt((mds-mdt)*(mds-mdt) + (sds-sdt)*(sds-sdt));
		return eps;
	}

	public static void debug () {
		Throwable tmp = new Throwable();
		System.err.println ("\n***  " + tmp.getStackTrace()[1] + "  ***\n");
	}

	public static void debug (String mess) {
		Throwable tmp = new Throwable();
		System.err.println ("\n***  " + tmp.getStackTrace()[1] + " : " + mess + "  ***\n");
	}

}
