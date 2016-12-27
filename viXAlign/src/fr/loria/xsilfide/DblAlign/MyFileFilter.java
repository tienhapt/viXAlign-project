package fr.loria.xsilfide.DblAlign;
import javax.swing.filechooser.*;
import java.io.File;
import java.util.Vector;
/**
 * Titre :
 * Description : Use for JFileChooser (javax.swing.JFileChooser)
 *      Exemple:
 *          JFileChooser chooser = new JFileChooser();
 *          String[] listExt = {"jpg","gif","bmp"};
 *          MyFileFilter filter = new MyFileFilter(listExt, "Image (*.jpg, *.gif, *.bmp)");
 *          chooser.setMyFileFilter(filter);
 *          chooser.showOpenDialog(parent);
 * Copyright :    Copyright (c) 2002 by Viet Commando
 * Société :
 * @author: vncommando@yahoo.com
 * @version 1.0
 */

public class MyFileFilter extends FileFilter {
    Vector list = new Vector();
    String description;

    public MyFileFilter() {
        description = "";
    }

    public MyFileFilter(String fileExt, String filtreName) {
        addFileExtension(fileExt);
        description = filtreName;
    }

    public MyFileFilter(String fileExt[], String filtreName) {
        addFileExtension(fileExt);
        description = filtreName;
    }

    public MyFileFilter(String fileExt[]) {
        addFileExtension(fileExt);
        description = new String("(");
        for (int i = 0; i < fileExt.length-1; i++)
            description += fileExt[i] + ", ";
        description += fileExt[fileExt.length-1] + ")";
    }

    private boolean isInList(String fileExt){
        if (fileExt == null)
            return false;
        int size = list.size();
        for (int i = 0; i < size; i++)
            if (((String)list.elementAt(i)).compareTo(fileExt) == 0)
                return true;
        return false;
    }

    public void addFileExtension(String fileExt){
        if (isInList(fileExt))
            return;
        list.add(fileExt.toLowerCase());
    }

    public void addFileExtension(String fileExt[]){
        for (int i = 0; i < fileExt.length; i++){
            if (isInList(fileExt[i]))
                return;
            list.add(fileExt[i].toLowerCase());
        }
    }

    public String getFileExtension(String fileName){
        int len = fileName.length();
        for (int i = len - 1; i > 0; i--)
            if (fileName.charAt(i) == '.')
                return fileName.substring(i+1).toLowerCase();
        return null;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        return isInList( getFileExtension(f.getName()) );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String filerName) {
        description = filerName;
    }
}
