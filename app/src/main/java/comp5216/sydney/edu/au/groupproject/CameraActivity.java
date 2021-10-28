package comp5216.sydney.edu.au.groupproject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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

    public final int TAKE_PHOTO_CODE = 648;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    ImageView iv;
    Bitmap bitmap;
    String imageFileName;
    String type, season;
    Boolean clothChecked = null, seasonChecked = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
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

        if (bitmap != null && clothChecked != null && seasonChecked != null) {
            saveFile();
            finish();
            return;
        }

    }

    public void saveFile() {
        System.out.println("save start");
        iv.setDrawingCacheEnabled(true);
        Bitmap ivbm = Bitmap.createBitmap(iv.getDrawingCache());
        FileOutputStream fileOutputStream = null;
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + type);
        storageDir.mkdir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = storageDir + "/" + season + "_" + timeStamp + ".jpg";

        try {
            fileOutputStream = new FileOutputStream(imageFileName);
            ivbm.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(CameraActivity.this, "Saved!" + storageDir + "/" +
                    season + "_"+ timeStamp + ".jpg", Toast.LENGTH_LONG).show();
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



}