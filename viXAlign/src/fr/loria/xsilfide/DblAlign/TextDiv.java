
package fr.loria.xsilfide.DblAlign;

import java.io.*;
import java.util.*;

public class TextDiv {

	public static final boolean ignorePars = false;

	public static final int DIV = 1;
	public static final int PAR = 2;
	public static final int STC = 3;
	public static final int UNK = -1;

	public final int type;

	public int numericID;

	public TextDiv[] links;

	public String id;

	public TextDiv father;
	public TextDiv[] daughters;
	public TextDiv [] alignedDaughters;
	//public TextDiv next;

	public int [] posSave;

	public int startPosW;
	public int endPosW;

	public int startPosC;
	public int endPosC;

	public Hashtable <String,Float> lexicalLinks;

	public TextDiv (String id, String typeString, TextDiv father, int stPC, int stPW) {
		this.id = id;
		this.father = father;
		startPosW = stPW;
		endPosW = startPosW-1;
		startPosC = stPC;
		endPosC = startPosC-1;
		posSave = new int[4];
		posSave[0] = startPosC;
		posSave[1] = startPosW;
		posSave[2] = endPosC;
		posSave[3] = endPosW;
		daughters = null;
		alignedDaughters = null;
		links = null;
		switch(typeString.charAt(0)) {
		case 'p':
		case 'P':
			type = PAR;
			break;
		case 'd':
		case 'D':
			type = DIV;
			break;
		case 's':
		case 'S':
			type = STC;
			break;
		default :
			System.err.printf("Indicateur de type de segment non reconnu : %s\n\n", typeString);
			type = UNK;
		}
		numericID = -1;
		lexicalLinks = new Hashtable<String,Float> ();
	}

	//add a new daughter to current father
	//the daughter is added to array TextDiv daughters[]
	public void addDaughter (TextDiv d) {
		int nbDaughters = ((daughters!=null) ? daughters.length : 0);
		TextDiv[] tmp = daughters;
		daughters = new TextDiv[nbDaughters+1];
		for (int i=0; i<nbDaughters; i++)
			daughters[i] = tmp[i];
		daughters[nbDaughters] = d;
		if (nbDaughters > 0) daughters[nbDaughters-1].setNext(d); 
		//Why don't increase the nbDaughters. Because this is a local variable
	}

	public void setNext (TextDiv n) {
		//next = n;
		TextDiv cur = this;
		//recursively set the endPosW and endPosC of the last daughter of the last daughter ...
		while (cur != null) {
			cur.endPosW = n.startPosW-1;
			cur.endPosC = n.startPosC-1;
			cur.posSave[2] = cur.endPosC;
			cur.posSave[3] = cur.endPosW;
			cur = ((cur.daughters==null) ? null : cur.daughters[cur.daughters.length-1]);
		}
	}

	//add TextDiv other to the links[] array
	public void linkWith (TextDiv other) {
		int nbLinksHere = ((links==null) ? 0 : links.length);
		TextDiv [] tmp = links;
		links = new TextDiv [nbLinksHere+1];
		for (int i=0; i<nbLinksHere; i++) links[i] = tmp[i];
		links[nbLinksHere] = other;
	}
	//Vai tro cua lexcialLinks de lam gi
	public void addLexicalLink (TextDiv other, float val) {
		Float dv = lexicalLinks.get(other.id);
		float v = ((dv==null) ? 0 : dv.floatValue());
		v += val / Math.sqrt(((float)(posSave[3]-posSave[1]+1)) * ((float)(other.posSave[3]-other.posSave[1]+1)));
		lexicalLinks.put(other.id, v);
		if (v > TextStructure.maxLexSimil)
			TextStructure.maxLexSimil = v;
	}
	//Xoa di mang alignedDautghters cua tat cac cac con chau
	public void clearLinks () {
		int nbDaughters = ((daughters!=null) ? daughters.length : 0);
		for (int i=0; i<nbDaughters; i++) daughters[i].clearLinks();
		links = null;
		alignedDaughters = null;
		startPosC = posSave[0];
		startPosW = posSave[1];
		endPosC = posSave[2];
		endPosW = posSave[3];
	}
	//Xoa lexcicalLinks cua tat ca cac con chau
	public void clearLexicalLinks () {
		lexicalLinks.clear();
		int nbDaughters = ((daughters!=null) ? daughters.length : 0);
		for (int i=0; i<nbDaughters; i++) daughters[i].clearLexicalLinks();
	}
	
	public static void getNormalizedSegments (int depth) {
		for (int i=0; (i < depth) && ( (TextStructure.source.currentlyAligned[0] != null 
				&& TextStructure.source.currentlyAligned[0].alignedDaughters != null)
				||(TextStructure.target.currentlyAligned[0] != null 
						&& TextStructure.target.currentlyAligned[0].alignedDaughters != null)
		) ; i++) {
			downOneLevelAligned();
		}
	}

	//Add all aligned daughters of a div to the source and target in corresponding
	public static void downOneLevelAligned () {
		// DblAlign.debug("nbDiv = " + currentLevel.length);
		Stack <TextDiv> srcDescend = new Stack <TextDiv> ();
		Stack <TextDiv> tarDescend = new Stack <TextDiv> ();
		for (int i=0; i<TextStructure.source.currentlyAligned.length; i++) {
			if (TextStructure.source.currentlyAligned[i] == null) {
				if (TextStructure.target.currentlyAligned[i].alignedDaughters == null) {
					System.err.println("Erreur : "+TextStructure.target.currentlyAligned[i].id);
					System.exit(1);
				}
				for (int j=0; j<TextStructure.target.currentlyAligned[i].alignedDaughters.length; j++) srcDescend.push(null);
			} else {
				for (int j=0; j<TextStructure.source.currentlyAligned[i].alignedDaughters.length; j++)
					srcDescend.push(TextStructure.source.currentlyAligned[i].alignedDaughters[j]);
			}
			if (TextStructure.target.currentlyAligned[i] == null) {
				for (int j=0; j<TextStructure.source.currentlyAligned[i].alignedDaughters.length; j++) tarDescend.push(null);
			} else {
				for (int j=0; j<TextStructure.target.currentlyAligned[i].alignedDaughters.length; j++)
					tarDescend.push(TextStructure.target.currentlyAligned[i].alignedDaughters[j]);
			}
		}
		TextStructure.source.currentlyAligned = new TextDiv [srcDescend.size()];
		srcDescend.copyInto(TextStructure.source.currentlyAligned);
		TextStructure.target.currentlyAligned = new TextDiv [tarDescend.size()];
		tarDescend.copyInto(TextStructure.target.currentlyAligned);
	}

	public static void align (TextDiv[] src, TextDiv[] tar, float [] lengthFactor, int depth, float displayVal) {
		//System.err.println(depth);
		if (src==null) {
			if (DblAlign.log != null) {
				for (int i=0; i<depth; i++)
					DblAlign.log.print("\t");
				DblAlign.log.printf("<null>  /  <%s> (%d)  (%.5f)\n", tar[0].id, 1-tar[0].startPosC+tar[0].endPosC, displayVal);
			}
			if (tar[0].daughters != null) {
				if (ignorePars && tar[0].daughters[0].type==PAR) {
					tar[0].alignedDaughters = new TextDiv [1];
					tar[0].alignedDaughters[0] = tar[0].daughters[0];
					TextDiv [] ttd = new TextDiv[tar[0].daughters.length];
					for (int i=0; i<tar[0].daughters.length; i++) {
						tar[0].daughters[i].startPosC = tar[0].daughters[0].startPosC;
						tar[0].daughters[i].startPosW = tar[0].daughters[0].startPosW;
						tar[0].daughters[i].endPosC = tar[0].daughters[tar[0].daughters.length-1].endPosC;
						tar[0].daughters[i].endPosW = tar[0].daughters[tar[0].daughters.length-1].endPosW;
						ttd[i] = tar[0].daughters[i];
					}
					align (null, ttd, null, depth+1, 0);
				} else {
					tar[0].alignedDaughters = new TextDiv [tar[0].daughters.length];
					for (int i=0; i<tar[0].daughters.length; i++) {
						tar[0].alignedDaughters[i] = tar[0].daughters[i];
						TextDiv [] ttd = new TextDiv[1];
						ttd[0] = tar[0].daughters[i];
						align (null, ttd, null, depth+1, 0);
					}
				}
			}
			return;
		}
		if (tar==null) {
			if (DblAlign.log != null) {
				for (int i=0; i<depth; i++)
					DblAlign.log.print("\t");
				DblAlign.log.printf("<%s> (%d)  /  <null>  (%.5f)\n", src[0].id, 1-src[0].startPosC+src[0].endPosC, displayVal);
			}
			if (src[0].daughters != null) {
				if (ignorePars && src[0].daughters[0].type==PAR) {
					src[0].alignedDaughters = new TextDiv [1];
					src[0].alignedDaughters[0] = src[0].daughters[0];
					TextDiv [] ttd = new TextDiv[src[0].daughters.length];
					for (int i=0; i<src[0].daughters.length; i++) {
						src[0].daughters[i].startPosC = src[0].daughters[0].startPosC;
						src[0].daughters[i].startPosW = src[0].daughters[0].startPosW;
						src[0].daughters[i].endPosC = src[0].daughters[src[0].daughters.length-1].endPosC;
						src[0].daughters[i].endPosW = src[0].daughters[src[0].daughters.length-1].endPosW;
						ttd[i] = src[0].daughters[i];
					}
					align (ttd, null, null, depth+1, 0);
				} else {
					src[0].alignedDaughters = new TextDiv [src[0].daughters.length];
					for (int i=0; i<src[0].daughters.length; i++) {
						src[0].alignedDaughters[i] = src[0].daughters[i];
						TextDiv [] ttd = new TextDiv[1];
						ttd[0] = src[0].daughters[i];
						align (ttd, null, null, depth+1, 0);
					}
				}
			}
			return;
		}
		//DblAlign.debug(this.toString());
		if (DblAlign.log != null) {
			for (int i=0; i<depth; i++)
				DblAlign.log.print("\t");
			for (int i=0; i<src.length; i++) {
				if (i>0) DblAlign.log.print(" + ");
				DblAlign.log.printf("<%s>", src[i].id);
			}
			DblAlign.log.printf(" (%d", 1-src[0].startPosC+src[0].posSave[2]);
			for (int i=1; i<src.length; i++) {
				DblAlign.log.printf(" - %d", src[i].posSave[2]-src[i].posSave[0]+1);
			}
			DblAlign.log.print(")  /  ");
			for (int i=0; i<tar.length; i++) {
				if (i>0) DblAlign.log.print(" + ");
				DblAlign.log.printf("<%s>", tar[i].id);
			}
			DblAlign.log.printf(" (%d", tar[0].posSave[2]-tar[0].posSave[0]+1);
			for (int i=1; i<tar.length; i++) {
				DblAlign.log.printf(" - %d", tar[i].posSave[2]-tar[i].posSave[0]+1);
			}
			DblAlign.log.printf(")  (%.5f)  (", displayVal);
			for (int i=0; i<src.length; i++) {
				for (int j=0; j<tar.length; j++) {
					if (i+j>0) DblAlign.log.print(" - ");
					Float f = src[i].lexicalLinks.get(tar[j].id);
					DblAlign.log.printf("%.5f", ((f==null) ? 0 : f.floatValue()));
				}
			}
			DblAlign.log.println(")");
		}

		if (src[0].type==STC) {return;}

		if (src[0].daughters == null) {
			System.out.println ("Error : no daughters (in source, "+src[0].id+").");
			System.exit(-1);
		}

		if (tar[0].daughters == null) {
			System.out.println ("Error : no daughters (in target, "+src[0].id+").");
			System.exit(-1);
		}

		if (src[0].daughters[0].type != tar[0].daughters[0].type) {
			System.out.println ("Error : div type mismatch at same level between source and target.");
			System.exit(-1);
		}

		TextDiv [] srcDivs = downOneLevel(src);   // Ceux qui sont lies au meme div du texte cible
		// que moi sont "fusionnes" avec moi...
		TextDiv [] tarDivs = downOneLevel(tar); // et reciproquement !

		if (ignorePars && srcDivs[0].type==PAR) {
			src[0].alignedDaughters = new TextDiv[1];
			src[0].alignedDaughters[0] = srcDivs[0];
			tar[0].alignedDaughters = new TextDiv[1];
			tar[0].alignedDaughters[0] = tarDivs[0];
			for (int k=0; k<srcDivs.length; k++) {
				for (int l=0; l<tarDivs.length; l++) {
					srcDivs[k].linkWith(tarDivs[l]);
					tarDivs[l].linkWith(srcDivs[k]);
				}
			}
			for (int k=0; k<srcDivs.length; k++) {
				srcDivs[k].startPosC = srcDivs[0].startPosC;
				srcDivs[k].startPosW = srcDivs[0].startPosW;
				srcDivs[k].endPosC = srcDivs[srcDivs.length-1].endPosC;
				srcDivs[k].endPosW = srcDivs[srcDivs.length-1].endPosW;
			}
			for (int l=0; l<tarDivs.length; l++) {
				tarDivs[l].startPosC = tarDivs[0].startPosC;
				tarDivs[l].startPosW = tarDivs[0].startPosW;
				tarDivs[l].endPosC = tarDivs[tarDivs.length-1].endPosC;
				tarDivs[l].endPosW = tarDivs[tarDivs.length-1].endPosW;
			}
			align(tarDivs[0].links, srcDivs[0].links, lengthFactor, depth+1, 0);
			return;
		}

		for (int i=0; i<srcDivs.length; i++) {
			for (int j=0; j<tarDivs.length; j++) {
				Float d = srcDivs[i].lexicalLinks.get(tarDivs[j].id);
				if (d != null)
					TextStructure.lexSimilArray[i][j] = d.floatValue(); //TextStructure.maxLexSimil;
				else
					TextStructure.lexSimilArray[i][j] = 0;
			}
		}

		float [] srcVec = new float [srcDivs.length];
		float [] tarVec = new float [tarDivs.length];
		for (int i=0; i<srcVec.length; i++) srcVec[i] = ((float)(srcDivs[i].endPosC-srcDivs[i].startPosC+1));// * lengthFactor[depth];
		for (int i=0; i<tarVec.length; i++) tarVec[i] = ((float)(tarDivs[i].endPosC-tarDivs[i].startPosC+1));// * lengthFactor[depth];

		//System.err.println(depth);
		Align.aveSrcSentLength = TextStructure.srcAverage[depth+1];
		Align.aveTarSentLength = TextStructure.tarAverage[depth+1];
		Align.sigmaSrcSentLength = TextStructure.srcSigma[depth+1];
		Align.sigmaTarSentLength = TextStructure.tarSigma[depth+1];

		Vector <Point> path = Align.getPath(srcVec, tarVec, false);
		srcVec = null;
		tarVec = null;

		src[0].alignedDaughters = new TextDiv[path.size()];
		tar[0].alignedDaughters = new TextDiv[path.size()];

		int srcPos = 0, tarPos = 0;
		for (int i=0; i<path.size(); i++) {
			Point p = path.get(i);
			for (int k=srcPos; k<srcPos+p.x; k++) {
				for (int l=tarPos; l<tarPos+p.y; l++) {
					srcDivs[k].linkWith(tarDivs[l]);
					tarDivs[l].linkWith(srcDivs[k]);
				}
			}
			for (int k=srcPos; k<srcPos+p.x; k++) {
				srcDivs[k].startPosC = srcDivs[srcPos].startPosC;
				srcDivs[k].startPosW = srcDivs[srcPos].startPosW;
				srcDivs[k].endPosC = srcDivs[srcPos+p.x-1].endPosC;
				srcDivs[k].endPosW = srcDivs[srcPos+p.x-1].endPosW;
			}
			for (int l=tarPos; l<tarPos+p.y; l++) {
				tarDivs[l].startPosC = tarDivs[tarPos].startPosC;
				tarDivs[l].startPosW = tarDivs[tarPos].startPosW;
				tarDivs[l].endPosC = tarDivs[tarPos+p.y-1].endPosC;
				tarDivs[l].endPosW = tarDivs[tarPos+p.y-1].endPosW;
			}
			if (p.x==0) {
				src[0].alignedDaughters[i] = null;
				tar[0].alignedDaughters[i] = tarDivs[tarPos];
				TextDiv [] ttd = new TextDiv[1];
				ttd[0] = tarDivs[tarPos];
				align(null, ttd, null, depth+1, p.annexInfo);
			} else if (p.y==0) {
				src[0].alignedDaughters[i] = srcDivs[srcPos];
				tar[0].alignedDaughters[i] = null;
				TextDiv [] ttd = new TextDiv[1];
				ttd[0] = srcDivs[srcPos];
				align(ttd, null, null, depth+1, p.annexInfo);
			} else {
				src[0].alignedDaughters[i] = srcDivs[srcPos];
				tar[0].alignedDaughters[i] = tarDivs[tarPos];
				align(tarDivs[tarPos].links, srcDivs[srcPos].links, lengthFactor, depth+1, p.annexInfo);
			}
			srcPos += p.x;
			tarPos += p.y;
		}
	}

	private static TextDiv[] downOneLevel (TextDiv[] currentLevel) {
		// DblAlign.debug("nbDiv = " + currentLevel.length);
		Stack <TextDiv> descend = new Stack <TextDiv> ();
		for (int i=0; i<currentLevel.length; i++) {
			if (currentLevel[i].daughters != null) {
				for (int j=0; j<currentLevel[i].daughters.length; j++)
					descend.push(currentLevel[i].daughters[j]);
			}
		}
		TextDiv[] res = new TextDiv [descend.size()];
		descend.copyInto(res);
		return res;
	}

	//Ham tinh do sau.
	public int computeStatistics (int [] count, float [] accu, float[] sigma, int depth) {
		int res = depth; //res se ghi lai do sau lon nhat trong so cac daughter; 
						 //nghia la daughter nao co bi chia thanh nhieu cap nhat thi se gan cho res
		if (daughters != null) {
			for (int i=0; i<daughters.length; i++) {
				int tmp = daughters[i].computeStatistics(count, accu, sigma, depth+1);
				if (tmp > res) res = tmp;
			}
		}
		count[depth]++; //mang count se ghi lai so luong daughter tuong ung voi muc depth
		accu[depth] += (float)(endPosC-startPosC+1); //Tong cac do lech vi tri dau va cuoi (hay con la do dai)
													//cua moi daughter tuong ung voi do sau depth
		if (sigma != null) sigma[depth] += (float)(endPosC-startPosC+1)*(float)(endPosC-startPosC+1); //Tong binh phuong do dai cua
																							//moi daughter
		return res;
	}

	//Tra ve khoi con chau tan cung chua vi tri pos
	public TextDiv getSegmentAt (int pos) {
		if (pos < posSave[1] || pos > posSave[3]) return null;
		if (daughters == null) return this;
		for (int i=0; i<daughters.length; i++)
			if (pos <= daughters[i].posSave[3]) return daughters[i].getSegmentAt(pos);
		return null;
	}

	//Ham nay tra ve so cau cua textDiv hien dang xet
	//Voi gia thiet la cau cuoi cung se textDiv nho nhat cua no
	public int countSentences () {
		if (daughters == null) return 1;
		int accu = 0;
		for (int i= 0; i<daughters.length; i++)
			accu += daughters[i].countSentences();
		return accu;
	}

	public float getSquareSentLengthSum () {
		if (daughters == null) return (float)((posSave[2]-posSave[0]+1)*(posSave[2]-posSave[0]+1));
		int accu = 0;
		for (int i= 0; i<daughters.length; i++)
			accu += daughters[i].getSquareSentLengthSum();
		return accu;
	}

	public void cleanEmptyParagraphs () {

		// 	if (type==STC) return;
		// 	if (type==PAR) {
		// 	    if (daughters != null) return;
		// 	    daughters = new TextDiv[1];
		// 	    daughters[0] = new TextDiv(id+"s1", "s", this, startPosC, startPosW);
		// 	    return;
		// 	}
		// 	for (int i=0; i<daughters.length; i++)
		// 	    daughters[i].cleanEmptyParagraphs();

		if (type==PAR || type==STC) return;
		if (type==DIV) {
			for (int i=0; i<daughters.length; i++) {
				if (daughters[i].daughters==null) {
					TextDiv[] tmp = daughters;
					daughters = new TextDiv[daughters.length-1];
					for (int j=0; j<i; j++) daughters[j] = tmp[j];
					for (int j=i+1; j<tmp.length; j++) daughters[j-1] = tmp[j];
				}
			}
		}
		for (int i=0; i<daughters.length; i++)
			daughters[i].cleanEmptyParagraphs();
	}

};
