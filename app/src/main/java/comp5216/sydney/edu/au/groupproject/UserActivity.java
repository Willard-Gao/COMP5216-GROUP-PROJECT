package comp5216.sydney.edu.au.groupproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class UserActivity extends Activity {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        String username = getIntent().getStringExtra("username");
        findViewById(R.id.aboutus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open url
                String website = getResources().getString(R.string.our_website);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
            }
        });
        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open url
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.our_email)});
                intent.setType("message/rfc882");
                startActivity(Intent.createChooser(intent, "choose one email"));
            }

        });
        findViewById(R.id.userpro).setVisibility(View.VISIBLE);

        textView = findViewById(R.id.username);
        textView.setText("username");
    }



    public void OnLogout(View view) {
        Intent data = new Intent();
        // Pass relevant data back as a result
        data.putExtra("logout", true);
        // Activity finishes OK, return the data
        setResult(RESULT_OK, data); // Set result code and bundle data for response
        finish(); // Close the activity, pass data to parent
    }

    public void onUploadClick(View view) {
        if (!isWifiConnect()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
            builder.setTitle(R.string.dialog_wifi_title)
                    .setMessage(R.string.dialog_wifi_msg)
                    .setPositiveButton(R.string.PROCEED, new
                            DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doUploadTask(1);
                                }
                            })
                    .setNegativeButton(R.string.STOP, new
                            DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doUploadTask(0);
                                }
                            });
            builder.create().show();
        } else {
            doUploadTask(1);
        }
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
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String username = user.getDisplayName();
                    StorageReference listRef = storage.getReference().child(username);
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
            com.google.firebase.storage.UploadTask uploadTask = null;
            try {
                uploadTask = fileRef.putStream(new FileInputStream(uploadFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Upload file " +
                            uploadFile.getAbsolutePath() + " Failed, " +
                            exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
