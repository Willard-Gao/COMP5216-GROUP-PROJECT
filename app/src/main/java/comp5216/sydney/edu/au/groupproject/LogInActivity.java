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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LogInActivity extends AppCompatActivity {

    private Boolean isSignedIn = false;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    List<UploadTask> uploadQueue = new LinkedList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.i("User", user.getDisplayName());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
    }

    public Boolean shouldStartSignIn() {
        return (!isSignedIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    @Override
    public void onStart() {
        super.onStart();

        startUploadThread();
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

    public void startUploadThread() {
        try {
            new Thread() {
                @Override
                public void run() {
                    StorageReference storageRef = storage.getReference();
                    while (true) {
                        while (!isWifiConnect()) {
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Long startTime = System.currentTimeMillis();
                        while (uploadQueue.size() <= 10) {
                            Log.i("Current upload queue size = ", String.valueOf(uploadQueue.size()));
                            Long currentTime = System.currentTimeMillis();
                            if ((currentTime - startTime) / 1000 > 5 * 60) {
                                break;
                            }
                        }
                        for (UploadTask task : uploadQueue) {
                            Uri fileUri = Uri.fromFile(task.getUploadFile());
                            StorageReference fileRef;
                            fileRef = storageRef.child( task.getUsername() + "/" + fileUri.getLastPathSegment());
                            com.google.firebase.storage.UploadTask uploadTask = fileRef.putFile(fileUri);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(LogInActivity.this, "Upload file " +
                                            task.getUploadFile().getAbsolutePath() + " Failed, " +
                                            exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(LogInActivity.this, "Upload file " +
                                            task.getUploadFile().getAbsolutePath() + " Success", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            // ...
        } else {
            if (response != null) {
                Toast.makeText(getApplicationContext(),
                        "Log in failed, error message: " + response.getError(),
                        Toast.LENGTH_LONG).show();
            } else {
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

    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getActiveNetworkInfo();
        if (mWifiInfo.isConnected() && mWifiInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }



    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Extract name value from result
                    Log.i("TEST", String.valueOf(R.string.log_out));
                    Boolean flag = result.getData().getExtras().getBoolean("logout");
                    if (flag) {
                        Log.i("USER", "log out");
                        signedOut();
                    }
                }
            }
    );

    // click button for "user page"
    public void onUserActivityClick(View view) {
        Intent intent = new Intent(LogInActivity.this, UserActivity.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        intent.putExtra("username", String.valueOf(user.getDisplayName()));
        // communicate with 2 views
        if (intent != null) {
            mLauncher.launch(intent);
        }
    }


    public void addIntoQueue(File uploadFile, String username) {
        synchronized (uploadQueue) {
            UploadTask uploadTask = new UploadTask();
            uploadTask.setUploadFile(uploadFile);
            uploadTask.setUsername(username);
            uploadQueue.add(uploadTask);
        }
    }

//    public long getKB(){
//        getApplicationInfo();
//        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
//    }
}