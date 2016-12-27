
package fr.loria.xsilfide.DblAlign;

import java.util.*;

public class Align {

	public static float aveSrcSentLength;
	public static float aveTarSentLength;

	public static float sigmaSrcSentLength;
	public static float sigmaTarSentLength;


	static public final int BADTYPE        = 0;
	static public final int DESTRUCTION    = 1;
	static public final int SUBSTITUTION   = 2;
	static public final int INSERTION      = 3;
	static public final int CONTRACTION    = 4;
	static public final int MELANGE        = 5;
	static public final int EXPANSION      = 6;
	static public final int BIGDESTRUCTION = 7;
	static public final int BIGINSERTION   = 8;
        static public final int ONETHREE        = 9;
        static public final int THREEONE        = 10;
        static public final int TWOTHREE        = 11;
        static public final int THREETWO        = 12;
        static public final int THREETHREE        = 13;


	public static float charProp;

	public static int getType (Point al) {
		if (( al.x == 1 ) && ( al.y == 0 ))      return(DESTRUCTION);
		else if (( al.x == 1 ) && ( al.y == 1 )) return(SUBSTITUTION);
		else if (( al.x == 0 ) && ( al.y == 1 )) return(INSERTION);
		else if (( al.x == 2 ) && ( al.y == 1 )) return(CONTRACTION);
		else if (( al.x == 1 ) && ( al.y == 2 )) return(EXPANSION);
                else if (( al.x == 1 ) && ( al.y == 3 )) return(ONETHREE);
                else if (( al.x == 3 ) && ( al.y == 1 )) return(THREEONE);
                else if (( al.x == 2 ) && ( al.y == 3 )) return(TWOTHREE);
                else if (( al.x == 3 ) && ( al.y == 2 )) return(THREETWO);
		else if ((al.x > 1 ) && ( al.y == 0 ))   return(BIGDESTRUCTION);  
		else if ((al.x == 0 ) && ( al.y > 1 ))   return(BIGINSERTION);
		else if (( al.x == 2 ) && ( al.y == 2 )) return(MELANGE);
                else if (( al.x == 3 ) && ( al.y == 3 )) return(THREETHREE);
		else return(BADTYPE);
	}

	public static Vector<Point> getPath(float[] x, float[] y, boolean smooth) {
		// If "smooth", lissage des chemins pour eviter toutes les deletions/insertions
		// X et Y sont les tailles des segments a aligner -- IMPERATIVEMENT !
		charProp = Parameters.getFloat("NB_CHAR_COEFF");
		int maxi, maxj;
		int[][] pathX;
		int[][] pathY;
		float[][] dist;
		int n;
		int i, j, oi, oj, di, dj;
		float d1, d2, d3, d4, d5, d6, dmin;

		float d31, d13, d32, d23, d33;
		
		float lexFact = 0;

		maxi = x.length+1;
		maxj = y.length+1;

		for (i=0; i<maxi; i++)
			for (j=0; j<maxj; j++)
				if (TextStructure.lexSimilArray[i][j] > lexFact) lexFact = TextStructure.lexSimilArray[i][j];

		if (lexFact > 0) lexFact = 4/lexFact;

		pathX = new int[maxi][maxj];
		pathY = new int[maxi][maxj];
		dist = new float[maxi][maxj];

		float tmpF;

		for (j = 0; j < maxj; j++) {
			for (i = 0; i < maxi; i++) {
				// substitution 1-1
				if (i>0 && j>0) {
					tmpF = 1/(1+lexFact*TextStructure.lexSimilArray[i-1][j-1]);
					d1 = dist[i-1][j-1] + tmpF*twoSideDistance(x[i-1], y[j-1], 0, 0);}
				else d1 = Float.MAX_VALUE;
				// deletion 1-0
				if (i>0) {
					tmpF = 1;
					d2 = dist[i-1][j] + tmpF*twoSideDistance(x[i-1], 0, 0, 0);}
				else d2 = Float.MAX_VALUE;
				// insertion 0-1
				if (j>0) {
					tmpF = 1;
					d3 = dist[i][j-1] + tmpF*twoSideDistance(0, y[j-1], 0, 0);}
				else d3 = Float.MAX_VALUE;
				// contraction 2-1
				if (i>1 && j>0){
					tmpF = (x[i-2]*TextStructure.lexSimilArray[i-2][j]
                                                +x[i-1]*TextStructure.lexSimilArray[i-1][j])/(x[i-2]+x[i-1]);
					tmpF = 1/(1+lexFact*tmpF);
					d4 = dist[i-2][j-1] + tmpF*twoSideDistance(x[i-2], y[j-1], x[i-1], 0);}
				else d4 = Float.MAX_VALUE;
				//expansion 1-2
				if (i>0 && j>1) {
					tmpF = (
							 y[j-2]*TextStructure.lexSimilArray[i][j-2]
						   + y[j-1]*TextStructure.lexSimilArray[i][j-1]
						   ) / (y[j-2]+y[j-1]);
					tmpF = 1/(1+lexFact*tmpF);
					d5 = dist[i-1][j-2] + tmpF*twoSideDistance(x[i-1], y[j-2], 0, y[j-1]);}
				else d5 = Float.MAX_VALUE;
				// fusion 2-2
				if (i>1 && j>1) {
					 tmpF = (
						      x[i-2]*y[j-1]*TextStructure.lexSimilArray[i-2][j-1]
						    + x[i-1]*y[j-1]*TextStructure.lexSimilArray[i-1][j-1]
						    + x[i-2]*y[j-2]*TextStructure.lexSimilArray[i-2][j-2]
						    + x[i-1]*y[j-2]*TextStructure.lexSimilArray[i-1][j-2]
						    ) / ((x[i-2]+x[i-1])*(y[j-2]+y[j-1]));
					tmpF = 1/(1+lexFact*tmpF);
					d6 = dist[i-2][j-2] + tmpF*twoSideDistance(x[i-2], y[j-2], x[i-1], y[j-1]);}
				else d6 = Float.MAX_VALUE;
                                			
				//3-1
				if(i > 2 && j > 0) {
					tmpF = (
							 x[i-3]*TextStructure.lexSimilArray[i-3][j]
						   + x[i-2]*TextStructure.lexSimilArray[i-2][j]
						   + x[i-1]*TextStructure.lexSimilArray[i-1][j]
						   ) / (x[i-3]+x[i-2]+x[i-1]);
					tmpF = 1/(1+lexFact*tmpF);
					d31 = dist[i-3][j-1] + tmpF*threeSideDistance(x[i-3], y[j-1], x[i-2], 0, x[i-1], 0);					
				} else d31 = Float.MAX_VALUE;
				
				//1-3
				if(i > 0 && j > 2) {
					tmpF = (
							 y[j-3]*TextStructure.lexSimilArray[i][j-3]
						   + y[j-2]*TextStructure.lexSimilArray[i][j-2]
						   + y[j-1]*TextStructure.lexSimilArray[i][j-1]
						   ) / (y[j-3]+y[j-2]+y[j-1]);
					tmpF = 1/(1+lexFact*tmpF);
					d13 = dist[i-1][j-3] + tmpF*threeSideDistance(x[i-1], y[j-3], 0, y[j-2], 0, y[j-1]);					
				} else d13 = Float.MAX_VALUE;
				
				//2-3
				if(i > 1 && j > 2) {
					tmpF = (
							 x[i-2]*y[j-3]*TextStructure.lexSimilArray[i-2][j-3]
						   + x[i-2]*y[j-2]*TextStructure.lexSimilArray[i-2][j-2]
						   + x[i-2]*y[j-1]*TextStructure.lexSimilArray[i-2][j-1]
						   + x[i-1]*y[j-3]*TextStructure.lexSimilArray[i-1][j-3]
						   + x[i-1]*y[j-2]*TextStructure.lexSimilArray[i-1][j-2]
						   + x[i-1]*y[j-1]*TextStructure.lexSimilArray[i-1][j-1]						    
						   ) / ((x[i-2] + x[i-1]) * (y[j-3] + y[j-2] + y[j-1]));
					tmpF = 1/(1+lexFact*tmpF);
					d23 = dist[i-2][j-3] + tmpF*threeSideDistance(x[i-2], y[j-3], x[i-1], y[j-2], 0, y[j-1]);
				} else d23 = Float.MAX_VALUE;
				
				//3-2
				if(i > 2 && j > 1) {
					tmpF = (
                                                    x[i-3]*y[j-2]*TextStructure.lexSimilArray[i-3][j-2]
						   + x[i-2]*y[j-2]*TextStructure.lexSimilArray[i-2][j-2]
						   + x[i-1]*y[j-2]*TextStructure.lexSimilArray[i-1][j-2]
						   + x[i-3]*y[j-1]*TextStructure.lexSimilArray[i-3][j-1]
						   + x[i-2]*y[j-1]*TextStructure.lexSimilArray[i-2][j-1]
						   + x[i-1]*y[j-1]*TextStructure.lexSimilArray[i-1][j-1]						    
						   ) / ((x[i-3] + x[i-2] + x[i-1]) * (y[j-2] + y[j-1]));
					tmpF = 1/(1+lexFact*tmpF);
					d32 = dist[i-3][j-2] + tmpF*threeSideDistance(x[i-3], y[j-2], x[i-2], y[j-1], x[i-1], 0);
				} else d32 = Float.MAX_VALUE;
				
				//3-3
				if(i > 2 && j > 2) {
					tmpF = (
                                                    x[i-3]*y[j-3]*TextStructure.lexSimilArray[i-3][j-3]
						   + x[i-2]*y[j-3]*TextStructure.lexSimilArray[i-2][j-3]
						   + x[i-1]*y[j-3]*TextStructure.lexSimilArray[i-1][j-3]
						   + x[i-3]*y[j-2]*TextStructure.lexSimilArray[i-3][j-2]
						   + x[i-2]*y[j-2]*TextStructure.lexSimilArray[i-2][j-2]
						   + x[i-1]*y[j-2]*TextStructure.lexSimilArray[i-1][j-2]
						   + x[i-3]*y[j-1]*TextStructure.lexSimilArray[i-3][j-1]
						   + x[i-2]*y[j-1]*TextStructure.lexSimilArray[i-2][j-1]
						   + x[i-1]*y[j-1]*TextStructure.lexSimilArray[i-1][j-1]						    
						   ) / ((x[i-3] + x[i-2] + x[i-1]) * (y[j-3] + y[j-2] + y[j-1]));
					tmpF = 1/(1+lexFact*tmpF);
					d33 = dist[i-3][j-3] + tmpF*threeSideDistance(x[i-3], y[j-3], x[i-2], y[j-2], x[i-1], y[j-1]);
				} else d33 = Float.MAX_VALUE;

				dmin = d1;
				if (d2 < dmin) dmin = d2;
				if (d3 < dmin) dmin = d3;
				if (d4 < dmin) dmin = d4;
				if (d5 < dmin) dmin = d5;
				if (d6 < dmin) dmin = d6;
				if (d31 < dmin) dmin = d31;
				if (d13 < dmin) dmin = d13;
				if (d23 < dmin) dmin = d23;
				if (d32 < dmin) dmin = d32;
				if (d33 < dmin) dmin = d33;

				//System.out.println(d1+" "+d2+" "+d3+" "+d4+" "+d5+" "+d6);
				dist[i][j] = dmin;
				if (dmin == Float.MAX_VALUE) {
					dist[i][j] = 0;
					pathX[i][j] = 0;
					pathY[i][j] = 0;
					//System.out.println("dmin="+dmin+" ");
				}
				else if (dmin == d1) {
					pathX[i][j] = i - 1;
					pathY[i][j] = j - 1;
					//System.out.println("d1="+d1+" ");
				}
				else if (dmin == d2) {
					pathX[i][j] = i - 1;
					pathY[i][j] = j;
					//System.out.println("d2="+d2+" ");
				}
				else if (dmin == d3) {
					pathX[i][j] = i;
					pathY[i][j] = j - 1;
					//System.out.println("d3="+d3+" ");
				}
				else if (dmin == d4) {
					pathX[i][j] = i - 2;
					pathY[i][j] = j - 1;
					//System.out.println("d4="+d4+" ");
				}
				else if (dmin == d5){
					pathX[i][j] = i - 1;
					pathY[i][j] = j - 2;
					//System.out.println("d5="+d5+" ");
				}
				else if (dmin == d6) {
					pathX[i][j] = i - 2;
					pathY[i][j] = j - 2;
					//System.out.println("d6="+d6+" ");
				}
				//System.out.println(i+", "+j+": "+pathX[i][j]+"  "+pathY[i][j]+"\n");
				else if (dmin == d13) {
					pathX[i][j] = i-1;
					pathY[i][j] = j - 3;
				} 
				else if (dmin == d31) {
					pathX[i][j] = i - 3;
					pathY[i][j] = j-1;
				}
				else if (dmin == d23) {
					pathX[i][j] = i - 2;
					pathY[i][j] = j - 3;
				}
				else if (dmin == d32) {
					pathX[i][j] = i - 3;
					pathY[i][j] = j - 2;
				} 
				else {
					pathX[i][j] = i - 3;
					pathY[i][j] = j - 3;
				}
			}
		}

		n = 0;

		Vector <Point> tmpPath = new Vector <Point> ();

		for (i = maxi-1, j = maxj-1 ; i > 0 || j > 0 ; i = oi, j = oj) {
			oi = pathX[i][j];
			oj = pathY[i][j];
			di = i - oi;
			dj = j - oj;
			if (di > 2 && dj == 1) {             // contraction
				tmpPath.add(new Point(3, 1, dist[i][j]));
				//System.out.println(tmpPath.toString());
			}
			else if (di == 1 && dj > 2) {             // expansion
				tmpPath.add(new Point(1, 3, dist[i][j]));
				//System.out.println(tmpPath.toString());
			} 
                        
			else
				tmpPath.add(new Point(di, dj, dist[i][j]));
			n++;
		}

		Vector <Point> path = new Vector <Point> ();

		if (!smooth){
			for (i = n-1; i >= 0; i--) {
				path.addElement(tmpPath.get(i));
			}    
		} // end for the cas for align sentences
		else {
			int memX=0, memY=0;
			int idx = n-1;
			Point p;
			if (idx >= 0)
				do {
					p = tmpPath.get(idx);
					if (p.x == 0 || p.y == 0) {
						memX += p.x;
						memY += p.y;
						idx--;
					}
				} while ((p.x == 0 || p.y == 0) && (idx >= 0));

			int count = 0;

			if ((idx >= 0) && ((memX > 0) || memY > 0)) {
				p = tmpPath.get(idx);
				p.x += memX;
				p.y += memY;
				path.addElement(p);
				count++; idx--;
			}

			while (idx >= 0) {
				p = tmpPath.get(idx);
				idx--;
				if (p.x == 0 || p.y == 0) {
					Point q = path.get(count - 1);
					q.x += p.x;
					q.y += p.y;
					//path.setPointAt(q, count-1);
					//System.out.println("path = "+path.toString());
				}
				else {
					path.addElement(p);
					count++;
				}
			}
		}
		return (path);
	}

	// To compute distances

	static private float align_cst_c  = (float)1.25;
	static private float align_cst_s2 = (float)6.78;

	static public float BIG_DISTANCE = (float)2500000;

	static private    int penalty21    = 200; /* 23 : -100 * ln([prob of 2-1 match] / [prob of 1-1 match]) */
	static private    int penalty22    = 44; /* 44 : -100 * ln([prob of 2-2 match] / [prob of 1-1 match]) */
	static private    int penalty01    = 482; /* 45 : -100 * ln([prob of 0-1 match] / [prob of 1-1 match]) */
        static private    int penalty10    = 547;
        static private    int penalty12    = -177; 
        static private    int penalty31    = 426; 
        static private    int penalty13    = -265;
        static private    int penalty23    = 795;
        static private    int penalty32    = 657;
        static private    int penalty33    = 4691; 
	//them tham so cho penalty13 31 32 23 33
	

	/* Local Distance Function */

	/* 
	 * Returns the area under a normal distribution
	 * from -inf to z standard deviations
	 */
	private static float probaNorm (float fz) {
		double t, pd;
		double z = (double)fz;

		t = 1.0D / (1.0D + 0.23164190000000001D * z);
		pd = 1.0D - 0.39894230000000003D *
		Math.exp((-z * z) / 2.0D) *
		((((1.3302744289999999D * t - 1.8212559779999999D) *
				t + 1.781477937D) *
				t - 0.356563782D) *
				t + 0.31938153000000002D) * t;
		/* see Abramowitz, M., and I. Stegun (1964), 26.2.17 p. 932 */
		return (float)pd;
	}

	/* 
	 * Return -100 * log probability that an English sentence of length
	 * len1 is a translation of a foreign sentence of length len2.  The
	 * probability is based on two parameters, the mean and variance of
	 * number of foreign characters per English character.
	 */
	private static float matchValue (float len1, float len2)  {
		if (len1 == 0 && len2 == 0) return 0;

		float normL1 = (len1-aveSrcSentLength)/sigmaSrcSentLength;
		float normL2 = (len2-aveTarSentLength)/sigmaTarSentLength;
		// Compute the area under the normal distribution between normL1 and normL2
		if (normL1 == normL2) {
			//System.err.println("*");
			return 0;
		}
		float pd;
		boolean useSimple = false;
		if (useSimple) {
			float z = normL1-normL2;
			if (z<0) z = -z;
			pd = 2*(1-probaNorm(z));
		} else {
			float area, area1, area2;
			if (normL1 < normL2) {
				area1 = ((normL1 < 0) ? 1-probaNorm(-normL1) : probaNorm(normL1)); // area before normL1
				area2 = ((normL2 < 0) ? probaNorm(-normL2) : 1-probaNorm(normL2)); // area after normL2
			} else {
				area1 = ((normL1 < 0) ? probaNorm(-normL1) : 1-probaNorm(normL1)); // area after normL1
				area2 = ((normL2 < 0) ? 1-probaNorm(-normL2) : probaNorm(normL2)); // area before normL2
			}
			area = 1-area1-area2;
			pd = 1-area;
		}

		if (pd > 0)
			return (-100 * (float)Math.log(pd));
		else
			return BIG_DISTANCE;
	}

	public static float twoSideDistance(float x1, float y1, float x2, float y2) {
		x2 *= charProp;
		y2 *= charProp;
		float res = 0;
		if (x2 == 0 && y2 == 0)
			if (x1 == 0)                /* insertion */
				res = matchValue(0, 5*y1)+ penalty01;
			else if (y1 == 0)           /* deletion */
				res = matchValue(5*x1, 0)+ penalty10;
			else 
				res = matchValue(x1, y1);   /* substitution */
		else if (x2 == 0)              /* expansion */
			res = matchValue(x1, y1 + y2) + (matchValue(0, y1) + matchValue(0,y2))/8+penalty12;
		else if (y2 == 0)              /* contraction */
			res = matchValue(x1 + x2, y1)  + (matchValue(x1, 0) + matchValue(x2, 0))/8+penalty21;
		else                          /* merger */
			res = matchValue(x1 + x2, y1 + y2) + (matchValue(0, y1) + matchValue(0, y2) + matchValue(x1, 0) + matchValue(x2, 0))/4+penalty22;
		return res;
	}
	
	public static float threeSideDistance(float x1, float y1, float x2, float y2, float x3, float y3) {
		float res = 0;
		if (x2 == 0 && x3 == 0) //1-3
			res = matchValue(x1,y1+y2+y3) + (matchValue(0,y1) + matchValue(0, y2) + matchValue(0,y3)) /9 +penalty13;
		else if (y2 == 0 && y3 == 0) //3-1
			res = matchValue(x1+x2+x3,y1) + (matchValue(x1,0) + matchValue(x2, 0) + matchValue(x3,0)) /9 +penalty31;
		else if (x3 == 0)
			res = matchValue(x1+x2,y1+y2+y3) + (matchValue(x1,0) + matchValue(x2, 0) + matchValue(0,y1) 
			                                      + matchValue(0,y2) + matchValue(0,y3)) /9 +penalty23;
		else if (y3 == 0)
			res = matchValue(x1+x2+x3,y1+y2) + (matchValue(x1,0) + matchValue(x2, 0) + matchValue(x3,0) 
            									  + matchValue(0,y1) + matchValue(0,y2)) /9 +penalty32;
		else
			res = matchValue(x1+x2+x3,y1+y2+y3) + (matchValue(x1,0) + matchValue(x2, 0) + matchValue(x3,0) 
			                                      + matchValue(0,y1) + matchValue(0,y2) + matchValue(0, y3)) /9+penalty33;
		return res;
	}

};


// EOF
