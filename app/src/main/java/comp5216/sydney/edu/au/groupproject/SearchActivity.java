package comp5216.sydney.edu.au.groupproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    EditText editText;
    Spinner seasonSpinner;
    Spinner classificationSpinner;
    ArrayList<File> allFile = new ArrayList<>();
    ArrayList<File> files = new ArrayList<>();
    SearchAdapter searchAdapter;
    GridView gridView;
    String season;
    String classification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        editText = findViewById(R.id.searchstyle);
        seasonSpinner = findViewById(R.id.season_spinner);
        classificationSpinner = findViewById(R.id.classification_spinner);
        gridView = findViewById(R.id.search_res);

        File mediaStorageDirImage = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + File.separator + "project");
        if (mediaStorageDirImage.exists()) {
            findAllImage(mediaStorageDirImage);
        } else {
            mediaStorageDirImage.mkdirs();
        }
        files = (ArrayList<File>) allFile.clone();
        searchAdapter = new SearchAdapter(this, files);
        gridView.setAdapter(searchAdapter);

        seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] seasons = getResources().getStringArray(R.array.seasonArray);
                season = seasons[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                season = null;
            }
        });

        classificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] classificatons = getResources().getStringArray(R.array.classificationArray);
                classification = classificatons[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                classification = null;
            }
        });

    }

    public void search(View view) {
        files.clear();
        for (File file : allFile) {
            String[] filePro = file.getName().split("_");
            if ((filePro[1].equals(season) || season == null) && (filePro[0].equals(classification) || classification == null)) {
                if ("".contentEquals(editText.getText()) || filePro[2].contains(editText.getText().toString())) {
                    files.add(file);
                }
            }
        }
        searchAdapter.notifyDataSetChanged();
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

    class SearchAdapter extends BaseAdapter {
        Context context;
        List<File> list;

        public SearchAdapter(Context context, List<File> itemList) {
            this.list = itemList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public File getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            File file = getItem(i);
//            if (view == null) {
//                view = LayoutInflater.from(context).inflate(R.layout.search_list_item, viewGroup, false);
//            }
            view = getLayoutInflater().inflate(R.layout.search_list_item, viewGroup, false);
//            LayoutInflater layoutInflater = LayoutInflater.from(context);
//            view = layoutInflater.inflate(R.layout.search_list_item, null);
            TextView imageName = view.findViewById(R.id.photoName);
            ImageView imageView = view.findViewById(R.id.photoImg);
            imageName.setText(file.getName().split("_")[2]);
            imageView.setImageURI(Uri.parse(getItem(i).toString()));
//            imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
//            imageView.setPadding(8, 8, 8, 8);
            return view;
        }
    }

//    // click button for "user page"
//    public void onUserClick(View view) {
//        Intent intent = new Intent(SearchActivity.this, UserActivity.class);
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        intent.putExtra("username", String.valueOf(user.getDisplayName()));
//        // communicate with 2 views
//        if (intent != null) {
//            mLauncher.launch(intent);
//        }
//    }


    public void onWaedrobeClick(View view) {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    public void onCameraClick(View view) {
        Intent intent = new Intent(SearchActivity.this, CameraActivity.class);
        finish();
        startActivity(intent);
    }
}
