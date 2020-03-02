package nk00322.surrey.petsearch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.petsearch.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button mFirebaseBtn;
    private DatabaseReference mDatabase;
    private EditText nameField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        nameField = findViewById(R.id.name_field);
        mFirebaseBtn = findViewById(R.id.firebase_btn);
        mFirebaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1 - Create Child in root object
                // 2 - Assign some value to the child
                String name = nameField.getText().toString().trim();
                mDatabase.child("Name").setValue(name);


            }
        });

    }
}
