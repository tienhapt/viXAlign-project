
package fr.loria.xsilfide.DblAlign;

// import
import java.util.Vector;

public class Path
{
  protected int cur;        // index of current point
  protected int num_points; // number of points
  protected int nx;         // max x
  protected int ny;         // max y
  protected Vector path;    // set of points

  public Path (int x, int y, int size) {
      num_points = size;
      nx = x;
      ny = y;
      cur = 0;
      path = new Vector(size);
    }

  public int getNumberOfPoint()
    {
      return(this.path.size());
    }

  public void setPointAt (Point p, int at)
    {
      cur = at;
      path.insertElementAt(p, at);    
    }
  
  public void setPointAt (int i, int j, int at)
    {
      Point point;
      
      point = new Point(i, j);
      this.setPointAt(point, at);
    }
  
  public Point getPointAt(int at)
    {
      return((Point)path.elementAt(at));
    }
  
  public String toString ()
    {
      String s = new String ();
      
      for (int i = 0; i < this.path.size(); i++ ) {
	s = s + this.getPointAt(i).toString();
      }
      return(s);
  }
}

// EOF

