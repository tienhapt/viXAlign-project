
package fr.loria.xsilfide.DblAlign;

import java.io.*;
import java.util.*;

class TextStructure {

	public static final TextStructure source;
	public static final TextStructure target;

	public static float [][] lexSimilArray;
	public static float maxLexSimil; //Xem cach no duoc cap nhat trong ham addLexicalLink (TextDiv other, float val)

	static {
		source = new TextStructure();
		target = new TextStructure();
		lexSimilArray = null;
		maxLexSimil = 0;
	}

	public static float [] srcAverage;
	public static float [] tarAverage;
	public static float [] srcSigma;
	public static float [] tarSigma;

	private TextDiv docRoot;

	protected TextDiv [] currentlyAligned;

	public int[] words;
	public int nbWords;

	float [] lengthFactor;

	int maxDepth;

	public TextStructure () {
		lengthFactor = new float[128];
		maxDepth = 0;
		docRoot = new TextDiv ("root", "d", null, 0, 0);
		words = null;
		nbWords = 0;
	}

	public static void alignSegments () {
		System.err.printf("Max lex simil : %f (%f)\n", maxLexSimil, ((source.docRoot.lexicalLinks.get(target.docRoot.id)==null) ? 0 : source.docRoot.lexicalLinks.get(target.docRoot.id).floatValue()));
		source.alignSegments(target);
	}

	public static void setWorkingLevel (int depth) {
		source.currentlyAligned = new TextDiv[1];
		source.currentlyAligned[0] = source.docRoot;
		target.currentlyAligned = new TextDiv[1];
		target.currentlyAligned[0] = target.docRoot;
		TextDiv.getNormalizedSegments(depth);
	}

	public TextDiv getTextDiv (String id) {
		TextDiv cour = docRoot;
		int[] path = new int[10];
		int pathPos = 0;
		int len = id.length();
		int pos = 0;
		while (pos < len) {
			while(Character.getType(id.charAt(pos)) != Character.DECIMAL_DIGIT_NUMBER) pos++;
			int pos2 = pos;
			while(pos2 < len && Character.getType(id.charAt(pos2)) == Character.DECIMAL_DIGIT_NUMBER) pos2++;
			String num = id.substring(pos,pos2);
			try {
				path[pathPos] = Integer.valueOf(num).intValue();}
			catch(Exception e) {
				System.err.println ("Problem while parsing id : " + id);
				e.printStackTrace();
				System.exit(-1);
			}
			pos = pos2;
		}
		pos=0;
		while (pos<pathPos) {
			if (cour.daughters==null || cour.daughters.length<path[pos]) {
				System.err.println ("Warning : trying to extract unregistered id : " + id + ".\n");
				return null;
			}
			cour = cour.daughters[path[pos]-1];
			pos++;
		}
		return cour;
	}

	public void addTextDiv (String id, int cPos, int wPos) {
		TextDiv cour = docRoot;
		int[] path = new int[10];
		int pathPos = 0;
		int len = id.length();
		int pos = 0;
		String typeId = "";
		//phan tich id, vi du 
		//id="d4p15s36" se luu vao cac bien nhu sau, path[]=[4,15,36] con typeId="s"
		while (pos < len) {
			typeId="";
			while(Character.getType(id.charAt(pos)) != Character.DECIMAL_DIGIT_NUMBER) {
				typeId += id.charAt(pos);
				pos++;
			}
			int pos2 = pos;
			while(pos2 < len && Character.getType(id.charAt(pos2)) == Character.DECIMAL_DIGIT_NUMBER) pos2++;
			String num = id.substring(pos,pos2);
			try {
				path[pathPos] = Integer.valueOf(num).intValue();}
			catch(Exception e) {
				System.err.println ("Problem while parsing id : " + id);
				e.printStackTrace();
				System.exit(-1);
			}
			pathPos++;
			pos = pos2;
		}
		pos=0;
		//Kiem tra xem co chi so nao trong path[] vuot qua so luong div tuong ung cua khoi chua no hay khong
		while (pos<pathPos-1) {
			if (cour.daughters==null || (pos<pathPos-2 && cour.daughters.length<path[pos])) {
				System.err.println ("Probleme de structure du document a l'insertion de " + id + ".\n");
				System.exit(-1);
			}
			cour = cour.daughters[path[pos]-1];
			pos++;
		}
		
		if ((path[pos]==1 && cour.daughters!=null) || (path[pos]>1 && (cour.daughters==null || cour.daughters.length!=path[pos]-1))) {
			System.err.println ("Probleme de structure du document a la creation de " + id + ".\n");
			System.exit(-1);
		}
		TextDiv daughter = new TextDiv(id, typeId, cour, cPos, wPos);
		cour.addDaughter(daughter);
	}

	public void addWord (int wordIdx, String sentenceID) {
		if (words==null)
			words = new int [10];
		if (words.length==nbWords) {
			int [] tmp = words;
			words = new int [2*nbWords];
			for (int i=0; i<nbWords; i++) {
				words[i] = tmp[i];
			}
		}
		words[nbWords] = wordIdx;
		nbWords++;
	}

	public void setFinalPos (int cPos, int wPos) {
		DblAlign.debug (wPos + " - " + cPos);
		TextDiv cour = docRoot;
		while (cour != null) {
			cour.posSave[2] = cour.endPosC = cPos;
			cour.posSave[3] = cour.endPosW = wPos;
			if (cour.daughters != null)
				cour = cour.daughters[cour.daughters.length-1];
			else
				cour = null;
		}
		int [] count = new int[128];
		for (int i=0; i<128; i++) {
			count[i] = 0;
			lengthFactor[i] = 0;
		}
		maxDepth = docRoot.computeStatistics(count, lengthFactor, null, 0); //Ham nay co chuc nang sau
		//count[i] chinh la so con chau o muc i cua docRoot, vi du count[0] la so con gai cua docRoot
		//lengthFactor[i] la tong do lech (endPosC-startPosC+1) cua cac con chau o muc i cua docRoot
		//maxDepth se tra ve la do sau lon nhat cua docRoot
		for (int i=0; i<=maxDepth; i++)
			lengthFactor[i] /= (float)(count[i]); //Luc nay lengthFactor[i] se la do dai trung binh cua moi con chau o cap do i
		for (int i=0; i<=maxDepth; i++)
			lengthFactor[i] = lengthFactor[maxDepth]*lengthFactor[maxDepth]/lengthFactor[i];
	}

	public float getProportionalPos (int pos) {
		//DblAlign.debug("ca = [" + currentlyAligned[currentlyAligned.length-1] + "], root = " + docRoot +  ", max = " + currentlyAligned[currentlyAligned.length-1].endPosW);
		if (pos == docRoot.endPosW) return (float)(currentlyAligned.length);
		int count = 0;
		for (count=0; count<currentlyAligned.length; count++) {
			if (currentlyAligned[count] != null && currentlyAligned[count].startPosW <= pos && currentlyAligned[count].endPosW >= pos)
				return ((float)count) + ((float)(pos-currentlyAligned[count].startPosW))
				/((float)(currentlyAligned[count].endPosW-currentlyAligned[count].startPosW+1));
		}
		System.err.printf("Warning : position %d is out of range\n\n", pos);
		//  	for (int i=0; i<currentlyAligned.length; i++) {
		//  	    System.err.printf("<%s> : %d/%d - ", currentlyAligned[i].id, currentlyAligned[i].startPosW, currentlyAligned[i].endPosW);
		//  	}
		// 	System.err.println("\n\n");
		return -1;
	}

	public String getSegmentIdAt (int pos) {
		return docRoot.getSegmentAt(pos).id;
	}

	
	public TextDiv getSegmentAt (int pos) {
		return docRoot.getSegmentAt(pos);
	}

	//Lay tu o vi tri thu pos trong mang words
	public int getWordAt (int pos) {
		if (pos >= 0 && pos<nbWords)
			return words[pos];
		return -1;
	}
	
	private void alignSegments (TextStructure other) {
		computeStatistics();
		docRoot.clearLinks();
		other.docRoot.clearLinks();
		docRoot.links = new TextDiv[1];
		docRoot.links[0] = other.docRoot;
		other.docRoot.links = new TextDiv[1];
		other.docRoot.links[0] = docRoot;
		TextDiv.align(other.docRoot.links, docRoot.links, lengthFactor, 0, 1);
		docRoot.clearLexicalLinks();
		other.docRoot.clearLexicalLinks();
		maxLexSimil = 0;
	}

	public static void computeStatistics () {
		boolean printInfo = true;
		srcAverage = new float [5];
		tarAverage = new float [srcAverage.length];
		srcSigma = new float [srcAverage.length];
		tarSigma = new float [srcAverage.length];
		int [] srcCount = new int [srcAverage.length];
		int [] tarCount = new int [srcAverage.length];
		for (int i=0; i<srcAverage.length; i++) {
			srcAverage[i] = tarAverage[i] = srcSigma[i] = tarSigma[i] = 0;
			srcCount[i] = tarCount[i] = 0;
		}
		source.docRoot.computeStatistics(srcCount, srcAverage, srcSigma, 0);
		target.docRoot.computeStatistics(tarCount, tarAverage, tarSigma, 0);
		if (printInfo) {
			System.err.print("\n |    level    |    count    |     ave     |     sig     |");
			System.err.print("\n |-------------+-------------+-------------+-------------|");
		}
		int maxSegSrc = 0, maxSegTar = 0; //So luong phan manh lon nhat trong so cac cap do
		for (int i=0; i<srcAverage.length && srcCount[i]>0; i++) {
			srcAverage[i] /= (float)srcCount[i];
			if (srcCount[i] > maxSegSrc) maxSegSrc = srcCount[i];
			srcSigma[i] = (float) Math.sqrt(srcSigma[i]/(float)srcCount[i] - srcAverage[i]*srcAverage[i]);
			tarAverage[i] /= (float)tarCount[i];
			if (tarCount[i] > maxSegTar) maxSegTar = tarCount[i];
			tarSigma[i] = (float) Math.sqrt(tarSigma[i]/(float)tarCount[i] - tarAverage[i]*tarAverage[i]);
			if (printInfo) {
				System.err.printf("\n |     s %1d     |  %9d  | %11.4f | %11.4f |", i, srcCount[i], srcAverage[i], srcSigma[i]);
				System.err.printf("\n |     t %1d     |  %9d  | %11.4f | %11.4f |", i, tarCount[i], tarAverage[i], tarSigma[i]);
			}
		}
		if (printInfo) {
			System.err.print("\n +-------------+-------------+-------------+-------------+\n");
		}
		if (lexSimilArray == null) {
			lexSimilArray = new float[maxSegSrc+1][maxSegTar+1];
		}
	}

	public static void addLexicalLink (int pos1, int pos2, float val) {
		TextDiv src = source.getSegmentAt(pos1);
		TextDiv tar = target.getSegmentAt(pos2);
		while (src != null && tar != null) {
			src.addLexicalLink(tar, val);
			tar.addLexicalLink(src, val);
			src = src.father;
			tar = tar.father;
		}
	}

	public static void cleanEmptyParagraphs () {
		//source.docRoot.cleanEmptyParagraphs();
		//target.docRoot.cleanEmptyParagraphs();
	}

};

