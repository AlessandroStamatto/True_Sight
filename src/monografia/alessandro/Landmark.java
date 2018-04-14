package monografia.alessandro;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

public class Landmark {
	
	public String				name;
	public String				path;
	public ArrayList<KeyPoint>	keypoints;
	public Mat					descritor;
	
	public Point				pos2d;
	public Point				start2d;
	public Point				end2d;
	
	public Point				pos3d;
	public Point				start3d;
	public Point				end3d;
	
	public String noLocal;
	
	
	public Landmark(String nam, String pat, String local, Point pos2, Point start2, Point end2,Point pos3, Point start3, Point end3) {
		name = nam;
		path = pat;
		start2d = start2;
		end2d = end2;
		start3d = start3;
		end3d = end3;
		pos2d = pos2;
		pos3d = pos3;
		
		noLocal = local;
	}
}
