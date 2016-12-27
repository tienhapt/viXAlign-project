
package fr.loria.xsilfide.DblAlign;

class DVec {

	public float stdDev; //Standard deviation
	public float mean; //mean
	//Distance of elements from the beginning
	public float[] dVec;

	public int nbDist;
	public int size() {return nbDist;}

	private boolean finalized;
	private boolean first;

	public DVec (int size) {
		dVec = new float [size];
		nbDist = 0;
		stdDev = mean = 0;
		finalized = false;
		first = true;
		//System.err.println("Creating " + this + "...");
	}

	//always add the new distance to the end of the vector dVec
	public void addDistance (float dist) {
		if (finalized) {
			System.err.println("Attention : ajout de distances au DVec finalise  " + this + " (ave = " + mean + ") ?\n");}
		dVec[nbDist] = dist;
		nbDist++;
		if (first) 
			first = false;
		else if (nbDist>1) {
			float d = dist-dVec[nbDist-2];
			mean += d;
			stdDev += d*d;
		}
	}

	public void finalized () {
		if (finalized) {
			//System.err.println("Attention : float finalisation du DVec " + this + " (ave = " + mean + ") ?\n");
		}
		else {
			if (nbDist>1) {
				mean /= (float)(nbDist-2);
				stdDev = (float)Math.sqrt(stdDev/(float)(nbDist-2) - mean*mean);
			}
			else {
				mean = stdDev = 0;
			}
			finalized = true;
		}
	}

	public float elementAt (int i) {
		return dVec[i];
	}

};
