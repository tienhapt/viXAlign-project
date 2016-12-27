
package fr.loria.xsilfide.DblAlign;

public class Point
{
    public int x;
    public int y;

    public float annexInfo;
  
    public Point() {
	this(0, 0, 0);
    }
  
    public Point(Point p) {
	this(p.x, p.y, 0);
    }
  
    public Point(int x, int y) {
	this(x, y, 0);
    }

    public Point(int x, int y, float a) {
	this.x = x;
	this.y = y;
	annexInfo = a;
    }

    public String toString() {
	return "[x=" + x + ",y=" + y + "]";
    }
}

// EOF

