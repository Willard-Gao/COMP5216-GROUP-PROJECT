package comp5216.sydney.edu.au.groupproject;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    public final int TAKE_PHOTO_CODE = 648;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    ImageView iv;
    Bitmap bitmap;
    String imageFileName;
    String type, season, name;
    Boolean clothChecked = null, seasonChecked = null;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        editText = findViewById(R.id.editView14);
    }

    public void onSaveClick(View v) throws IOException {
        if (bitmap == null) {
            Toast.makeText(CameraActivity.this, "Please take photo.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (clothChecked == null) {
            Toast.makeText(CameraActivity.this, "Please select the clothes types.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (seasonChecked == null) {
            Toast.makeText(CameraActivity.this, "Please select the seasons.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (editText.length() == 0) {
            Toast.makeText(CameraActivity.this, "Please write the comments.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (bitmap != null && clothChecked != null && seasonChecked != null && editText.length() != 0) {
            saveFile();
            finish();
            Intent intent = new Intent(CameraActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }

    public void saveFile() {
        System.out.println("save start");
        iv.setDrawingCacheEnabled(true);
        Bitmap ivbm = Bitmap.createBitmap(iv.getDrawingCache());
        FileOutputStream fileOutputStream = null;
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + File.separator + "project");
        if (!storageDir.exists()) storageDir.mkdir();

        imageFileName = type + "_" + season + "_" + editText.getText().toString() + ".jpg";
        String filename = storageDir + "/" + imageFileName;
        try {
            fileOutputStream = new FileOutputStream(filename);
            ivbm.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(CameraActivity.this, "Saved!" + storageDir + "/" +
                    imageFileName, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        galleryAddPic();
    }


    public void onTakePhoto(View v) {
//        if (!marshmallowPermission.checkPermissionForCamera()
//                || !marshmallowPermission.checkPermissionForExternalStorage()) {
//            marshmallowPermission.requestPermissionForCamera();
//        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_PHOTO_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        iv = (ImageView) findViewById(R.id.imageView5);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            bitmap = (Bitmap) bundle.get("data");
            iv.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onRBClothClick(View view) {
        // Is the button now checked?
        clothChecked = ((RadioButton) view).isChecked();
        System.out.println(clothChecked);

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton1:
                if (clothChecked)
                    type = "Tops";
                    break;
            case R.id.radioButton2:
                if (clothChecked)
                    type = "Bottoms";
                    break;
            case R.id.radioButton3:
                if (clothChecked)
                    type = "Shoes";
                break;
            case R.id.radioButton4:
                if (clothChecked)
                    type = "Hats";
                break;
            case R.id.radioButton5:
                if (clothChecked)
                    type = "Accessories";
                break;
        }
    }

    public void onRBSeasonClick(View view) {
        // Is the button now checked?
        seasonChecked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton6:
                if (seasonChecked)
                    season = "Spring";
                break;
            case R.id.radioButton7:
                if (seasonChecked)
                    season = "Summer";
                break;
            case R.id.radioButton8:
                if (seasonChecked)
                    season = "Autumn";
                break;
            case R.id.radioButton9:
                if (seasonChecked)
                    season = "Winter";
                break;
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageFileName);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    public void onShareClick(View view) {
        String type = "image/*";
//        File mediaStorageDir = ".";
//        String filename = "./tmp.jpg";
//        String mediaPath = filename;
        createInstagramIntent(type, imageFileName);
    }

    private void createInstagramIntent(String type, String mediaPath) {
        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);
        // Set the MIME type
        share.setType(type);
        // Create the URI from the media
        File media = new File(mediaPath);
        Uri uri = getPhotoUri(mediaPath);
        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);
        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }

    private Uri getPhotoUri(String absolutePath) {
        Uri photoUri = null;
        File storageDir = new File(Environment.getExternalStorageDirectory().toString()
                + File.separator + "project");
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                values.put(MediaStore.Images.Media.DATA, absolutePath);
                photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (Exception e) {
                Log.e("PHOTO URI", e.getMessage());
                e.printStackTrace();
            }
        } else {
            photoUri = Uri.fromFile(new File(storageDir, absolutePath));
        }
        return photoUri;
    }

    public void onSearchClick(View view) {
        Intent intent = new Intent(CameraActivity.this, SearchActivity.class);
        finish();
        startActivity(intent);
    }

    public void onWaedrobeClick(View view) {
        Intent intent = new Intent(CameraActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}