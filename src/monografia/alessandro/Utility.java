package monografia.alessandro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Utility {
	
	private static final String TAG = "Utility";
	public static TrueSightActivity context;
	
	public static Mat readIntoMat (String imagePath) {
		BitmapFactory.Options bitOpts = new BitmapFactory.Options();
		bitOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bitOpts);
		
		Mat rgba = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
		rgba = Utils.bitmapToMat(bitmap);
		
		Log.i("ImagemMat lida do caminho:", imagePath);
		
		return rgba;
	}
	
	public static Mat readIntoMat (Bitmap bitmap) {
				
		Mat rgba = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
		rgba = Utils.bitmapToMat(bitmap);
		
		return rgba;
	}
	
	public static Bitmap readIntoBitmap (String imagePath) {
		BitmapFactory.Options bitOpts = new BitmapFactory.Options();
		bitOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bitOpts);
		
		
		Log.i("Imagem(Bitmap) lida do caminho:", imagePath);
		
		return bitmap;
	}
	
	public static Mat toRgb (Mat rgba) {
		Mat rgb = new Mat();
		Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB);
		return rgb;
	}
	
	public static Mat toRgba (Mat rgb) {
		Mat rgba = new Mat();
		Imgproc.cvtColor(rgb, rgba, Imgproc.COLOR_RGB2RGBA);
		return rgba;
	}
	
	public static ArrayList<KeyPoint> computeKeypoints(Mat image) {
		ArrayList<KeyPoint> keypoints = new ArrayList<KeyPoint>();
		context.surfDet.detect(image, keypoints);
		return keypoints;
	}
	
	public static Mat computeDescriptor(Mat image, ArrayList<KeyPoint> keypoints) {
		Mat descriptor = new Mat();
		context.surfExt.compute(image, keypoints, descriptor);
		return descriptor;
	}
	
	public static ArrayList<DMatch> computeMatch(Mat image, Mat reference) {
		ArrayList<DMatch> matches = new ArrayList<DMatch>();
		DescriptorMatcher brute = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		brute.match(image, reference, matches);
		
		return matches;
	}
	
	public static double computeEuclid(ArrayList<DMatch> matches) {
		double dist = 0;
		int tam = 0;
		double minDist = Double.MAX_VALUE;
		
		//minDist calculado para remoção de outliers
		for (DMatch match: matches) {
			minDist = Math.min(minDist, match.distance);
		}
		
		for (DMatch match: matches) {
			if (match.distance < (3*minDist)) {
				dist += match.distance;
				tam++;
			}
		}
		if (tam > 0)
			dist /= tam;
		else
			dist = Double.MAX_VALUE;
		
		return dist;
	}

	public static int getRotationFromImage(String imagePath) {
    	int degrees = 0;
    	
    	try {
    		ExifInterface exif = new ExifInterface (imagePath);
    		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
    		switch (orientation) {
    		case ExifInterface.ORIENTATION_ROTATE_90:
    			degrees = 90; break;
    		case ExifInterface.ORIENTATION_ROTATE_180:
    			degrees = 180; break;
    		case ExifInterface.ORIENTATION_ROTATE_270:
    			degrees = 270; break;
    		default:
    			degrees = 0; break;
    		}
    	}
    	catch (Exception e){
    		Log.i(TAG, "fuuuuuuu!");
    	}
    	
    	return degrees;
    }
    
	public static Bitmap rotate (Bitmap b, int degrees) {
    	if (degrees != 0 && b != null) {
    		Matrix m = new Matrix();
    		m.setRotate(degrees, (float) b.getWidth() /2
    							,(float) b.getHeight()/2);
    		try {
    			Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    			if (b != b2) {
    				b.recycle();
    				b = b2;
    			}
    		} catch (OutOfMemoryError ex) {
    			Log.i(TAG, "Sem memoria suficiente =(");
    		}
    	}
    	
    	return b;
    }
    
	public static Uri getOutputMediaFileUri () {
    	return Uri.fromFile(getOutputMediaFile());
    }
    
	public static File getOutputMediaFile() {
    	File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Landmarks");
    	
    	if (! mediaStorageDir.exists()) {
    		if (! mediaStorageDir.mkdirs()) {
    			Log.d(TAG, "Falhou ao criar diretorio");
    			return null;
    		}
    	}
    	
    	String timeStamp = new SimpleDateFormat ("yyyyMMdd_HHmmss").format(new Date());
    	
    	File mediaFile;
    	mediaFile = new File (mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    	
    	return mediaFile;
    }
    
	public static void writeFile(String path, String content) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(new File(path));
            FileChannel fc = stream.getChannel();
            fc.write(Charset.defaultCharset().encode(content));
        } catch (IOException e) {
            Log.e("IOfail: ", "Failed to write file \"" + path + "\". Exception is thrown: " + e);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                	Log.e("IOfail: ","Exception is thrown: " + e);
                }
        }
    }
    
	public static String readFile(String path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(path));
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } catch (IOException e) {
        	Log.e("IOfail: ","Failed to read file \"" + path + "\". Exception is thrown: " + e);
            return null;
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                	Log.e("IOfail: ","Exception is thrown: " + e);
                }
        }
    }
	
    public static String getTempFileName(String extension)
    {
        File cache = context.getCacheDir();
        if (!extension.startsWith("."))
            extension = "." + extension;
        try {
            File tmp = File.createTempFile("surf", extension, cache);
            String path = tmp.getAbsolutePath();
            tmp.delete();
            return path;
        } catch (IOException e) {
            Log.e("Utility: ", "Failed to get temp file name. Exception is thrown: " + e);
        }
        return null;
    }
	
}