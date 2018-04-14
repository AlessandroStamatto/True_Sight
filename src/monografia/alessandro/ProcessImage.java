package monografia.alessandro;

import java.util.ArrayList;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ProcessImage extends AsyncTask<String, String, Bitmap> {
	
	private String mCurrentImagePath;
	private ProgressDialog pd;
	private TrueSightActivity context;
	private static final Scalar COLOR = new Scalar(41,241,237);
	
	private static final int nBench = 1; // simular nBench * 3 imagens na base de dados
	

	public ProcessImage(TrueSightActivity activity) {
		context = activity;
	}

	@Override
	protected Bitmap doInBackground(String... imagePath) {
		
		mCurrentImagePath = imagePath[0];

		//Por simplicidade readIntoMat retorna apenas uma Mat
		//mas preciso do Bitmap, devido a isso
		//o trecho abaixo é uma duplicação dessa função
		BitmapFactory.Options bitOpts = new BitmapFactory.Options();
		bitOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImagePath, bitOpts);
		
		Mat rgba = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
		rgba = Utils.bitmapToMat(bitmap);
				
		Log.i("Caminho", mCurrentImagePath);

		publishProgress("Detectando Keypoints...");
		ArrayList<KeyPoint> keypoints = Utility.computeKeypoints(rgba);
		
		publishProgress("Extraindo descritor...");
		Mat descriptor = Utility.computeDescriptor(rgba, keypoints);
		
		publishProgress("Fusionando resultado a imagem...");
		rgba = Utility.toRgb(rgba);
		Features2d.drawKeypoints(rgba, keypoints, rgba, COLOR, Features2d.DRAW_RICH_KEYPOINTS);
		rgba = Utility.toRgba(rgba);
		
		Utils.matToBitmap(rgba, bitmap);

		rgba.release();
		
		double min = 99999, current;
		Landmark menor = null;
		ArrayList<DMatch> matches;

		StopWatch.start();
		for (int i = nBench; i > 0; --i) {
		
			for (Landmark l: context.landmarks) {
				publishProgress("Comparando com " + l.name + "...");
				matches = Utility.computeMatch(descriptor, l.descritor);
				current = Utility.computeEuclid(matches);
				if (current < min) {
					menor = l; 
					min = current;
				}
			}
		}
		StopWatch.end();
		
		context.origem = menor;
		
		if (min < 0.3333333)
			publishProgress(null, "Você esta no " + menor.noLocal + "!");
		else
			publishProgress(null, "talvez você esteja no " + menor.noLocal + "?");
		
		Log.i("benchmark", "tempo de comparação: " + StopWatch.time());
		
		descriptor.release();
		
		return bitmap;
	}
	
	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setMessage("Carregando Imagem...");
		pd.show();
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
	protected void onPostExecute (Bitmap bitmap) {
		context.mImageView.setImageBitmap(bitmap);
		pd.dismiss();
		context.notInProcess = true;
		//Toast.makeText(context, "Pressione o botão 'Menu' para navegar", Toast.LENGTH_SHORT).show();
	}
}
