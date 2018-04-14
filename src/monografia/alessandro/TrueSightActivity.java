package monografia.alessandro;

import java.io.FileOutputStream;
import java.util.ArrayList;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ImageView;
import android.widget.Toast;

public class TrueSightActivity extends Activity {

	private static final String TAG = "TrueSightActivity";
	private static final String PASTA = "/mnt/sdcard/Base/";
	private static final String INITIALWOLF = PASTA + "tela.jpg";
	private static final int ACTIVITY_SELECT_CAMERA = 0;	
	private static final int ACTIVITY_SELECT_IMAGE = 1;
	
	private String 				mCurrentImagePath = null;
	private MenuItem			mItemCamera;
	private MenuItem			mItemGallery;
	private SubMenu				mItemDestinations;
	private MenuItem			mDimap, mSinfo, mAnfiteatro;
	private SubMenu				mItemMap;
	private MenuItem			mMap2d, mMap3d;
	private Uri					fileUri;
	public boolean				notInProcess = false;
	private int					dimensions = 2;
	
	public ImageView 			mImageView;
	
	public Landmark origem;
	public Landmark destino;
	
	//Atualizar para 2 vetores...
	public FeatureDetector 		surfDet = FeatureDetector.create(FeatureDetector.SURF);
	public DescriptorExtractor 	surfExt = DescriptorExtractor.create(DescriptorExtractor.SURF);
	public ArrayList<Landmark>	landmarks = new ArrayList<Landmark>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageView = new ImageView(this);
        setContentView(mImageView);
        Bitmap initial = Utility.readIntoBitmap (INITIALWOLF);
        mImageView.setImageBitmap(initial);
    
        Utility.context = this;
        String SURFSETTINGS = Utility.getTempFileName("yml");
        Utility.writeFile(SURFSETTINGS, "%YAML:1.0\nhessianThreshold: 4000.\noctaves: 3\noctaveLayers: 4\nupright: 0\n");
        surfDet.read(SURFSETTINGS);
        
        ComputeBase task = new ComputeBase(this);
		task.execute();
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        if (notInProcess) { 
        	Toast.makeText(this, "Pressione o botão 'Menu' para navegar", Toast.LENGTH_SHORT).show();
        	Bitmap initial = Utility.readIntoBitmap (INITIALWOLF);
            mImageView.setImageBitmap(initial);
        }
    }

    
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	mItemCamera = menu.add("1. Fotografar Landmark");
    	mItemGallery = menu.add("2. Analisar Foto");
    	
    	mItemDestinations = menu.addSubMenu("3. Escolher Destino");
    	mDimap = mItemDestinations.add("dimap");
    	mSinfo = mItemDestinations.add("sinfo");
    	mAnfiteatro = mItemDestinations.add("anfiteatro");
    	
    	mItemMap = menu.addSubMenu("4. Atualizar Mapa");
    	mMap2d = mItemMap.add("Mapa 2D");
    	mMap3d = mItemMap.add("Mapa 3D");
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(TAG, "Item Selecionado: " + item);
    	
    	if (item == mItemCamera) {
    		
    		notInProcess = false;
    		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    		
    		fileUri = Utility.getOutputMediaFileUri();
    		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
    		startActivityForResult(cameraIntent, ACTIVITY_SELECT_CAMERA);
    	}
    	else if (item == mItemGallery) {
    		notInProcess = false;
    		Intent galleryIntent = new Intent (Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
    		startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
    	}
    	else if (item == mMap2d) {
    		dimensions = 2;
    		
    		ProcessMap task = new ProcessMap(this, dimensions);
    		task.execute();
    	}
    	else if (item == mMap3d) {
    		dimensions = 3;

    		ProcessMap task = new ProcessMap(this, dimensions);
    		task.execute();
    	}
    	else {
    		if (item == mDimap) {
    			Toast.makeText(this, "Destino: Dimap", Toast.LENGTH_SHORT).show();
    			destino = landmarks.get(0);
        	}
        	else if (item == mSinfo) {
        		Toast.makeText(this, "Destino: Sinfo", Toast.LENGTH_SHORT).show();
        		destino = landmarks.get(1);
        	}
        	else if (item == mAnfiteatro) {
        		Toast.makeText(this, "Destino: Anfiteatro", Toast.LENGTH_SHORT).show();
        		destino = landmarks.get(2);
        	}
    	}
    	return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == ACTIVITY_SELECT_CAMERA 
    			&& resultCode == Activity.RESULT_OK) {
    		
    		mCurrentImagePath = fileUri.getPath();
    		
    		try {
    			BitmapFactory.Options option = new BitmapFactory.Options();
    			option.inSampleSize = 4;
    			int degrees = Utility.getRotationFromImage(mCurrentImagePath);
    			Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImagePath, option);
    			
    			if (degrees != 0)
    				bitmap = Utility.rotate(bitmap, degrees);
    			
    			FileOutputStream out = new FileOutputStream(mCurrentImagePath);
    			bitmap.compress(CompressFormat.JPEG, 100, out);
    		} catch (Exception e) {
    			Log.e(TAG, "FUUUUUU");
    		}
    		
    		ContentValues values = new ContentValues();
    		values.put(Images.Media.MIME_TYPE, "image/jpeg");
    		values.put(Images.Media.DATA, mCurrentImagePath);
    		values.put(Images.Media.DISPLAY_NAME, "Landmark");
    		values.put(Images.Media.TITLE, "Landmark");
    		getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
    	}
    	
    	else if (requestCode == ACTIVITY_SELECT_IMAGE && resultCode == RESULT_OK) {
    		try {    			
    			Uri currImageUri = data.getData();
    			String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };
    			Cursor cursor = managedQuery(currImageUri, proj, null, null, null);
    			int columnIndex = cursor.getColumnIndex(proj[0]);
    			cursor.moveToFirst();
    			mCurrentImagePath = cursor.getString(columnIndex);
    			
    		} catch (Exception e) {
    			Log.e(TAG, "Erro ao pegar imagem " + e);
    		}
    		
    		ProcessImage task = new ProcessImage(this);
    		task.execute(mCurrentImagePath);
    		
    	}
    }
    
}