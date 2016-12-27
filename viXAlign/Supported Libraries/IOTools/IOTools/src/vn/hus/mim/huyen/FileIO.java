package vn.hus.mim.huyen;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.net.*;
import javax.swing.*;

/** FileIO.java
 * Methods for file I/O operations
 * @author Nguyen Thi Minh Huyen
 * @version 1
 */

public class FileIO {

    public FileIO(){
    }

    /** Load properties from a file 
     * @param fileIn Properties file name
     * @return a Properties object
     */
    public static Properties loadPROPS(String fileIn){
	
	Properties p = new Properties();
	
	try {
	    p.load(new FileInputStream(fileIn));
	} catch (IOException ioe) {
	    System.err.println("Can't read properties file "
			       + fileIn + ": ");
	    ioe.printStackTrace();
	    return null;
  	}

	return p;
    }

     
    /** Open a file to read with a given encoding 
     * @param fileIn reading file name, enc character encoding (UTF8/ASCII)
     * @return BufferedReader object
     * @exception FileNotFoundException, IOException 
     */
    public static BufferedReader openIN(String fileIn, String enc) {
  	BufferedReader in = null;

  	try {
	    if (enc.equals("UTF-8")) {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn),"UTF-8"));
	
		/** For Windows users: Edited file using UTF8 format contains a redundant character at the begining, this character should be deleted  */	
		System.out.println(fileIn+": Delete first character of UTF-8 file (Y/N)? (Windows user)");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		if(stdin.readLine().equalsIgnoreCase("Y"))
		    in.read();	
	    }
	    else
		in = new BufferedReader(new FileReader(fileIn));
  	} catch (FileNotFoundException e) {
	    System.out.println(fileIn + " does not exist!");
	    return null;
	} catch(IOException exc) {
	    exc.printStackTrace();
  	}
	
	return in;
    }
  

    /** Open a file to write with a given encoding 
     * @param fileIn writing file name, enc character encoding (UTF8/ASCII)
     * @return PrintWriter object
     * @exception IOException 
     */
    public static PrintWriter openOUT(String fileOut, String enc) {
	
	PrintWriter out = null;

  	try {	
	    File f = new File(fileOut);
	    if (f.exists()) {
  		System.out.println(fileOut + " exists ... Do you want to overwrite it? (Y/N)");
  		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	  	if(!(stdin.readLine().equalsIgnoreCase("Y")))
		    return null;
	    }
	    if (enc.equals("UTF-8"))
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
	    else
		out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
  	} catch(IOException exc) {
	    exc.printStackTrace();
  	}
	return out;
   }
    
    protected static final int BUFSIZE = 8192;
    
    /** Utility function to do expensive, long processing asynchronously 
     * @param input file name
     * @return Reader object
     * @exception MalformedURLException, IOException
     * @throws FileNotFoundException
     */
    public static InputStream openLargeInput(String name) throws FileNotFoundException  {
	InputStream is;
	try {
	    URL u = new URL(name);
	    is = new ProgressMonitorInputStream(null, "Reading" + " "
						+name,
						u.openStream());
	} catch (MalformedURLException e) {
	    is = new ProgressMonitorInputStream(null, "Reading" + " "
						+name,
						new FileInputStream(name));
	} catch (IOException f) {
	    is = new ProgressMonitorInputStream(null, "Reading" + " "
						+name,
						new FileInputStream(name));
	}
	InputStream buf = (InputStream) (new BufferedInputStream(is, BUFSIZE));
	return buf;
	
    }

    
}
