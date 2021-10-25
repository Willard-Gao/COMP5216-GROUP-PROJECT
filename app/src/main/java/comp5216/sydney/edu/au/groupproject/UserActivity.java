package comp5216.sydney.edu.au.groupproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String editTaskTitle = getIntent().getStringExtra("taskTitle");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String username = getIntent().getStringExtra("username");
        TextView textView = findViewById(R.id.username);
        textView.setText(username);
    }

    public void OnLogout(View view) {
        Intent data = new Intent();
        // Pass relevant data back as a result
        data.putExtra("logout", true);
        // Activity finishes OK, return the data
        setResult(RESULT_OK, data); // Set result code and bundle data for response
        finish(); // Close the activity, pass data to parent
    }
}
