package monografia.alessandro;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

public class ProcessMap extends AsyncTask<Void, String, Bitmap> {

	private static final String PASTA = "/mnt/sdcard/Base/";
	private static final Scalar CIRCLECOLOR = new Scalar(21,27,92);
	private static final Scalar ARROWCOLOR = new Scalar(219,48,13);
	private static final Scalar CROSSCOLOR = new Scalar(219,48,13);
	private static final Scalar TRIANGLECOLOR = new Scalar(47,92,45);
	private int dimensions = 2;
	private static final double PI  =  3.14159265;
	private static final double DEGREES = 180/PI;
	private static final double RAD = PI/180;
	
	private ProgressDialog pd;
	private TrueSightActivity context;
	
	public ProcessMap(TrueSightActivity activity, int dim) {
		context = activity;
		dimensions = dim;
	}
	
	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setMessage("Atualizando mapa...");
		pd.show();
	}
	
	@Override
	protected Bitmap doInBackground(Void... __) {
		Point origem, destino, start, end;
		Bitmap mapa;
		
		if (context.origem == null) {
			context.origem = context.landmarks.get(0);
		}
		if (context.destino == null) {
			context.destino = context.landmarks.get(0);
		}
		
		if (dimensions == 2) {
			mapa = Utility.readIntoBitmap(PASTA + "mapa2d.jpg");
			origem = context.origem.pos2d;
			destino = context.destino.pos2d;
			start = context.origem.start2d;
			end = context.origem.end2d;
		} else {
			mapa = Utility.readIntoBitmap(PASTA + "mapa3d.jpg");
			origem = context.origem.pos3d;
			destino = context.destino.pos3d;
			start = context.origem.start3d;
			end = context.origem.end3d;
		}
		
		Mat matriz = Utility.readIntoMat(mapa);
		matriz = Utility.toRgb(matriz);
		
		//triangulo de visao
		Core.line(matriz, origem, start, TRIANGLECOLOR, 2);
		Core.line(matriz, origem, end, TRIANGLECOLOR, 2);
		
		Core.circle(matriz, origem, 4, CIRCLECOLOR, -1);
		
		matriz = drawCross(matriz, destino);
		
		if ( ( origem.x == destino.x)  && ( origem.y == destino.y ) )
			publishProgress( null, "Você chegou ao destino!");
		else {
			drawVision(matriz, start, end);
			drawArrow (matriz, origem, destino);
		}
			
		
		matriz = Utility.toRgba(matriz);
		Utils.matToBitmap(matriz, mapa);
		return mapa;
	}
	
	@Override
	protected void onProgressUpdate (String... msg) {
		if (msg.length == 1)
			pd.setMessage(msg[0]);
		else {
			if (msg[0] != null)
				pd.setMessage(msg[0]);
			
			Toast.makeText(context, msg[1], Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onPostExecute (Bitmap mapa) {
		pd.dismiss();
		context.mImageView.setImageBitmap(mapa);
	}
	
	private Mat drawCross (Mat image, Point p) {
		double x = p.x; double y = p.y;
		Point start = new Point (x-7, y-7);
		Point end = new Point (x+7, y+7);
		Core.line(image, start, end, CROSSCOLOR, 4);
		
		start = new Point (x-7, y+7);
		end = new Point (x+7, y-7);
		Core.line(image, start, end, CROSSCOLOR, 4);
		
		return image;
	}
	
	private Mat drawArrow (Mat image, Point origem, Point destino) {
		
		double xo = origem.x;
		double yo = origem.y;
		double xd = destino.x;
		double yd = destino.y;
		
		xd = xd-xo;
		yd = yd-yo;
		xd /= 2;
		yd /= 2;
		xd = xo+xd;
		yd = yo+yd;
		
		Point start = new Point (xo, yo);
		Point end = new Point (xd, yd);
		Core.line(image, start, end, ARROWCOLOR, 2);
		
		double x1, y1, x2, y2;
	    x1 = xd - 10; y1 = yd - 6;
	    x2 = xd - 10; y2 = yd + 6;
	    
	    xd-=xo; yd-=yo;
	    double angle = Math.atan2 (yd, xd) * DEGREES;
	    xd+=xo; yd+=yo;
	    Point pivot = new Point (xd,yd);
	    
	    Point target1 = new Point (x1,y1);
	    target1 = rotate(target1, pivot, angle);
	    Point target2 = new Point (x2,y2);
	    target2 = rotate(target2, pivot, angle);
	    
	    Core.line(image, pivot, target1, ARROWCOLOR, 2);
	    Core.line(image, pivot, target2, ARROWCOLOR, 2);
		
		return image;
	}
	
	private Mat drawVision (Mat image, Point start, Point end) {
				
		Core.line(image, start, end, CIRCLECOLOR, 5);
		
		return image;
	}
	
	private Point rotate (Point target, Point pivot, double degrees) {
		Point rotated = new Point (target.x - pivot.x, target.y - pivot.y);
		double theta = Math.atan2(rotated.y, rotated.x) * DEGREES;
		theta+=degrees;
		theta*=RAD;
		double r = Math.sqrt(Math.pow(rotated.x, 2) + Math.pow(rotated.y, 2));
		rotated.x = Math.cos(theta)*r;
		rotated.y = Math.sin(theta)*r;
		rotated.x += pivot.x;
		rotated.y += pivot.y;
		
		return rotated;
	}

}
