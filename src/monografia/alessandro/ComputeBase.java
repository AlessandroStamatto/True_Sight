package monografia.alessandro;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ComputeBase extends AsyncTask<Void, String, Void> {

	private ProgressDialog pd;
	private TrueSightActivity context;
	
	private static final String PASTA = "/mnt/sdcard/Base/";	

	public ComputeBase(TrueSightActivity activity) {
		context = activity;
	}
	
	private Point P(double x, double y) {
		Point p = new Point(x,y);
		return p;
	}
	
	private boolean readBase () {
		Landmark dimap = new Landmark("Dimap", PASTA + "dimap.jpg", "no DIMAp", P(396,58), P( 360, 46 ), P( 400, 46 ), P(432,171), P(418, 150 ), P(440, 167 ) );
		Landmark sinfo = new Landmark("Sinfo", PASTA + "sinfo.jpg", "na SINFO", P(383,107), P(370, 90), P(370, 121), P(389,197), P(361, 201), P(399, 179) );
		Landmark anfiteatro = new Landmark("anfiteatro", PASTA + "anfiteatro.jpg", "no anfiteatro", P(362,146), P(318, 154), P(370, 154), P(332, 209), P(304, 199), P(332, 221) );
		
		context.landmarks.add(dimap);
		context.landmarks.add(sinfo);
		context.landmarks.add(anfiteatro);
		
		return false;
	}

	@Override
	protected Void doInBackground(Void... __) {
		
		Mat image;
		
		
		if (!readBase()) { //caso nao exista base de descritores gere a mesma
			publishProgress("Base não encontrada, gerando...");
			StopWatch.start();
			for(Landmark l : context.landmarks) {
				publishProgress(l.name + ": Lendo imagem...");
				image = Utility.readIntoMat (l.path);
				publishProgress(l.name + ": Detectando Keypoints...");
				l.keypoints = Utility.computeKeypoints (image);
				publishProgress(l.name + ": Extraindo Descritor...");
				l.descritor = Utility.computeDescriptor (image, l.keypoints);
				image.release();
			}
			StopWatch.end();
		}
		
		Log.i("benchmark", "Tempo da base: " + StopWatch.time());
		
		return null;
	}
	
	
	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setMessage("Lendo Base de Dados...");
		pd.show();
	}
	
	@Override
	protected void onProgressUpdate (String... msg) {
		pd.setMessage(msg[0]);
	}
	
	@Override
	protected void onPostExecute (Void __) {
		pd.dismiss();
		//Toast.makeText(context, "Pressione o botão 'Menu' para navegar", Toast.LENGTH_SHORT).show();
		context.notInProcess = true;
	}
}
