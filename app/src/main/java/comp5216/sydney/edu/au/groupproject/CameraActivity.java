package comp5216.sydney.edu.au.groupproject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore;
    public String photoFileName = "photo.jpg";

    private static final int MY_PERMISSIONS_REQUEST_OPEN_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
    public final int TAKE_PHOTO_CODE = 648;


    private File file;
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    ImageView iv;
    Bitmap bitmap;

    private ArrayList<File> allFile = new ArrayList<File>();

    String currentPhotoPath;

    String type, season;
    Boolean clothChecked = null, seasonChecked = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
    }

    public void onSaveClick(View v) throws IOException {
        iv = (ImageView) findViewById(R.id.imageView5);
//        saveBtn = findViewById(R.id.button13);
//        saveBtn.setOnClickListener(v -> {
            if(bitmap == null) {
                Toast.makeText(CameraActivity.this, "Please take photo.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if(clothChecked == null) {
                Toast.makeText(CameraActivity.this, "Please select the clothes types.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if(seasonChecked == null) {
                Toast.makeText(CameraActivity.this, "Please select the seasons.",
                        Toast.LENGTH_LONG).show();
                return;
            }

        if(bitmap != null && clothChecked != null && seasonChecked != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = season + timeStamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            System.out.println(storageDir);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = image.getAbsolutePath();
            galleryAddPic();

            System.out.println("end");
        }
//            if(!sessionsType.get(0).isChecked() && !sessionsType.get(1).isChecked()
//                    &&!sessionsType.get(2).isChecked() &&!sessionsType.get(3).isChecked()){
//                Toast.makeText(CameraActivity.this, "Please select the seasons.",
//                        Toast.LENGTH_LONG).show();
//                return;

//            File file = new File(Environment.getExternalStorageDirectory() + "/" + selectedCloType.getText().toString());
//            new File(Environment.getExternalStorageDirectory() + "/" + selectedCloType.getText().toString()).mkdir();
//            try {
//                new File(Environment.getExternalStorageDirectory() + "/" +
//                        selectedCloType.getText().toString() + "/114514.jpg").createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            file = new File(Environment.getExternalStorageDirectory() + "/" + selectedCloType.getText().toString() + "/114514.jpg");
//            System.out.println(file.getAbsolutePath());
//
//            try {
//                //文件输出流
//                FileOutputStream fileOutputStream=new FileOutputStream(file);
//                //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
//                bitmap.compress(Bitmap.CompressFormat.JPEG,70,fileOutputStream);
//                //写入，这里会卡顿，因为图片较大
//                fileOutputStream.flush();
//                //记得要关闭写入流
//                fileOutputStream.close();
//                //成功的提示，写入成功后，请在对应目录中找保存的图片
//                Toast.makeText(CameraActivity.this,
//                        "写入成功！目录"+Environment.getExternalStorageDirectory() +
//                                "/" + selectedCloType.getText().toString() + "/114514.jpg",Toast.LENGTH_LONG).show();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                //失败的提示
//                Toast.makeText(CameraActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                //失败的提示
//                Toast.makeText(CameraActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
//            }
//            System.out.println("clothes type: " + selectedCloType.getText().toString());
//            System.out.println(bitmap.toString());
//        });
//        System.out.println(R.id.radioButton);
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

        if (requestCode == 648 && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            bitmap = (Bitmap) bundle.get("data");
            iv.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getFileUri(String fileName, int type) {
        Uri fileUri = null;
        try {
            String typestr = "/images/"; //default to images type
            if (type == 1) {
                typestr = "/images/";
            }

            // Get safe storage directory depending on type
            File mediaStorageDir = new File(this.getExternalFilesDir(null).getAbsolutePath(),
                    typestr+fileName);
            //File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
            //        typestr+fileName);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.getParentFile().exists() && !mediaStorageDir.getParentFile().mkdirs()) {
                Log.d("Error", "failed to create directory");
            }

            // Create the file target for the media based on filename
            file = new File(mediaStorageDir.getParentFile().getPath() + File.separator + fileName);

            // Wrap File object into a content provider, required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(
                        this.getApplicationContext(),
                        "au.edu.sydney.comp5216.groupproject.fileProvider", file);
            } else {
                fileUri = Uri.fromFile(mediaStorageDir);
            }
        } catch (Exception ex) {
            Log.d("getFileUri", ex.getStackTrace().toString());
        }
        return fileUri;
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


//    private File createImageFile() throws IOException {
        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = season + timeStamp;
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



}