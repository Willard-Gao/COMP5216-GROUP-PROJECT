package comp5216.sydney.edu.au.groupproject;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Boolean isSignedIn = false;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Long currentLevel = 0L;
    Long uploadThreshold = 0L;
    ListView listView;
    ArrayList<File> allFile = new ArrayList<>();
    Map<String, List<File>> wardrobe = new HashMap<>();
    WardrobeAdapter wardrobeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.i("User", user.getDisplayName());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);


        wardrobe.put("Hats", new LinkedList<>());
        wardrobe.put("Tops", new LinkedList<>());
        wardrobe.put("Bottoms", new LinkedList<>());
        wardrobe.put("Shoes", new LinkedList<>());
        wardrobe.put("Accessories", new LinkedList<>());


        // 文件分类
        File mediaStorageDirImage = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + File.separator + "project");
        if (mediaStorageDirImage.exists()) {
            findAllImage(mediaStorageDirImage);
        } else {
            mediaStorageDirImage.mkdirs();
        }

        for (File file : allFile) {
            String[] names = file.getName().split("_");
            if ("Hats".equals(names[0])) {
                wardrobe.get("Hats").add(file);
            } else if ("Tops".equals(names[0])) {
                wardrobe.get("Tops").add(file);
            } else if ("Bottoms".equals(names[0])) {
                wardrobe.get("Bottoms").add(file);
            } else if ("Shoes".equals(names[0])) {
                wardrobe.get("Shoes").add(file);
            } else if ("Accessories".equals(names[0])) {
                wardrobe.get("Accessories").add(file);
            }
        }
        ArrayList<Type> typeList = new ArrayList<>(5);
        typeList.add(new Type("Hats", wardrobe.get("Hats")));
        typeList.add(new Type("Tops", wardrobe.get("Tops")));
        typeList.add(new Type("Bottoms", wardrobe.get("Bottoms")));
        typeList.add(new Type("Shoes", wardrobe.get("Shoes")));
        typeList.add(new Type("Accessories", wardrobe.get("Accessories")));
        wardrobeAdapter = new WardrobeAdapter(this, typeList);
        listView.setAdapter(wardrobeAdapter);
    }

    public Boolean shouldStartSignIn() {
        return (!isSignedIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    public void findAllImage(File mediaStorageDir) {
        try {
            String[] file01 = mediaStorageDir.list();
            for (int i = 0; i < file01.length; i++) {
                File file02 = new File(mediaStorageDir + "/" + file01[i]);

                if (file02.isDirectory()) {
                    //Iteration
                    findAllImage(file02);
                } else if (file02.getName().endsWith(".jpg")) {
                    allFile.add(file02);
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void startSignIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

// Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            isSignedIn = true;
            String username = String.valueOf(user.getDisplayName());
            Log.i("USER", username);
            Toast.makeText(getApplicationContext(),
                    "Log in success, username: " + username,
                    Toast.LENGTH_SHORT).show();
            // set up username
//            TextView textview = (TextView) findViewById(R.id.username);
//            textview.setText(username);
            // ...
        } else {
            if (response != null){
                Toast.makeText(getApplicationContext(),
                        "Log in failed, error message: " + response.getError(),
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),
                        "User stopped logging in", Toast.LENGTH_SHORT).show();
                startSignIn();
            }
        }
    }

    private Uri getPhotoUri(String absolutePath) {
        Uri photoUri = null;
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
            photoUri = Uri.fromFile(new File(absolutePath));
        }
        return photoUri;
    }




    public void onDeleteAccount(View view) {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),
                                "Log out Success!", Toast.LENGTH_LONG).show();
                        startSignIn();
                    }
                });
    }

    // sharing to instagram
    public void OnShare(View view) {
        String type = "image/*";
//        File mediaStorageDir = ".";
        String filename = "./tmp.jpg";
        String mediaPath = filename;
        createInstagramIntent(type, mediaPath);
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

    public void signedOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),
                                "Log out Success!", Toast.LENGTH_LONG).show();
                        startSignIn();
                    }
                });
    }


    public void doUploadTask(int type) {
        List tmpFileList = new ArrayList();
        try {
            new Thread() {
                @Override
                public void run() {
                    File imgDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).
                            getAbsolutePath() + File.separator + "images");
                    File[] imgFiles = imgDir.listFiles();
                    StorageReference listRef = storage.getReference().child("images");
                    Long start = System.currentTimeMillis();
                    Long finalStart = start;
                    listRef.listAll()
                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    for (StorageReference item : listResult.getItems()) {
                                        tmpFileList.add(item.getName());
                                    }
                                    Long end = System.currentTimeMillis();
                                    System.out.println("list bucket files, time spent " + (end - finalStart) + " ms");

                                    if (type == 0) {
                                        while (!isWifiConnect()) {
                                            try {
                                                sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    for (File tmp : imgFiles) {
                                        if (tmpFileList.contains(tmp.getName())) {
                                            continue;
                                        }
                                        while (currentLevel > uploadThreshold) {
                                            try {
                                                System.out.println( String.format("current traffic %s is bigger than " +
                                                        "upload threshold %s, wait a moment", currentLevel, uploadThreshold));
                                                sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        upLoadByUser(tmp);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                    super.run();
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getActiveNetworkInfo();
        if (mWifiInfo.isConnected() && mWifiInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public void upLoadByUser(File uploadFile) {
        try {
            StorageReference storageRef = storage.getReference();
            Uri fileUri = Uri.fromFile(uploadFile);
            StorageReference fileRef;
            fileRef = storageRef.child("images/" + fileUri.getLastPathSegment());
            UploadTask uploadTask = null;
            try {
                uploadTask = fileRef.putStream(new FileInputStream(uploadFile));
                currentLevel += uploadFile.length();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    currentLevel -= uploadFile.length();
                    Toast.makeText(getApplicationContext(), "Upload file " +
                            uploadFile.getAbsolutePath() + " Failed, " +
                            exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    currentLevel -= uploadFile.length();
                    System.out.println("current traffic : " + currentLevel);
                }
            });
        } catch (Exception e) {
            currentLevel -= uploadFile.length();
            e.printStackTrace();
        }
    }

    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Extract name value from result
                    Log.i("TEST", String.valueOf(R.string.log_out));
                    Boolean flag = result.getData().getExtras().getBoolean("R.string.log_out");
                    if (flag){
                        Log.i("USER", "log out");
                        signedOut();
                    }
                }
            }
    );

    // click button for "user page"
    public void onUserClick(View view) {
        Intent intent = new Intent(MainActivity.this, UserActivity.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        intent.putExtra("username", String.valueOf(user.getDisplayName()));
        // communicate with 2 views
        if (intent != null) {
            mLauncher.launch(intent);
        }
    }

    public void onSearchClick(View view) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        finish();
        startActivity(intent);
    }

    public void onCameraClick(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        finish();
        startActivity(intent);
    }




    class WardrobeAdapter extends ArrayAdapter<Type> {
        public WardrobeAdapter(Context context, ArrayList<Type> itemList) {
            super(context, 0, itemList);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Type type = getItem(i);
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, viewGroup, false);
            }

            TextView typeName = view.findViewById(R.id.typeName);
            GridView gridView = view.findViewById(R.id.grid);
            setGridView(type, gridView);
            typeName.setText(type.getName());
            return view;
        }

        private void setGridView(Type type, GridView gridView) {
            int size = type.getClothes().size();
            int length = 100;
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            float density = dm.density;
            int gridviewWidth = (int) (size * (length + 4) * density);
            int itemWidth = (int) (length * density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
            gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
            gridView.setColumnWidth(itemWidth); // 设置列表项宽
            gridView.setHorizontalSpacing(5); // 设置列表项水平间距
            gridView.setStretchMode(GridView.NO_STRETCH);
            gridView.setNumColumns(size); // 设置列数量=列表集合数

            GriditemAdapter adapter = new GriditemAdapter(getApplicationContext(),
                    type.getClothes());
            gridView.setAdapter(adapter);
        }
    }
}