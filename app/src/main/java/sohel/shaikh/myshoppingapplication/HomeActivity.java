package sohel.shaikh.myshoppingapplication;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab_btn;
    private RecyclerView recyclerView;
    private TextView totalsumResult;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    //global variable
    private String type;
    private int amount;
    private String note;
    private String post_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser muser = mAuth.getCurrentUser();
        assert muser != null;
        String uId = muser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        fab_btn = findViewById(R.id.fab);
        totalsumResult = findViewById(R.id.total_ammount);
        recyclerView = findViewById(R.id.recycler_home);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot Snapshot) {

                int totalammount = 0;

                for (DataSnapshot snap : Snapshot.getChildren()) {

                    MyData data = snap.getValue(MyData.class);

                    totalammount += data.getAmount();
                    String sttotal = String.valueOf(totalammount + ".00");

                    totalsumResult.setText(sttotal);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog();
            }
        });


    }

    private void customDialog() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myview = inflater.inflate(R.layout.input_data, null);

        final AlertDialog dialog = mydialog.create();

        dialog.setView(myview);

        final EditText type = myview.findViewById(R.id.edt_type);
        final EditText amount = myview.findViewById(R.id.edt_ammount);
        final EditText note = myview.findViewById(R.id.edt_note);
        Button btnSave = myview.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int ammint = Integer.parseInt(mAmount);

                if (TextUtils.isEmpty(mType)) {
                    type.setError("Required Field..");
                    return;
                }
                if (TextUtils.isEmpty(mAmount)) {
                    amount.setError("Required Field..");
                    return;
                }
                if (TextUtils.isEmpty(mNote)) {
                    note.setError("Required Field..");
                    return;
                }
                String id = mDatabase.push().getKey();

                String date = DateFormat.getDateInstance().format(new Date());

                MyData data = new MyData(mType, ammint, mNote, date, id);
                mDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(), "Data Add", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            }
        });

        dialog.show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<MyData, MyViewHolder> adapter = new FirebaseRecyclerAdapter<MyData, MyViewHolder>(MyData.class,
                R.layout.item_data, MyViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(final MyViewHolder myViewHolder, final MyData myData, final int i) {
                myViewHolder.setType(myData.getType());
                myViewHolder.setNote(myData.getNote());
                myViewHolder.setDate(myData.getDate());
                myViewHolder.setAmmount(myData.getAmount());
                myViewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(HomeActivity.this,"click:: " + i,Toast.LENGTH_SHORT).show();

                        post_key = getRef(i).getKey();
                        type = myData.getType();
                        note = myData.getNote();
                        amount = myData.getAmount();
                        updateData();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public void updateData() {

        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View mView = inflater.inflate(R.layout.update_inputfield, null);

        final AlertDialog dialog = mydialog.create();
        dialog.setView(mView);

        final EditText edt_Type = mView.findViewById(R.id.edt_type_upd);
        final EditText edt_Ammoun = mView.findViewById(R.id.edt_ammount_upd);
        final EditText edt_Note = mView.findViewById(R.id.edt_note_upd);

        edt_Type.setText(type);
        edt_Type.setSelection(type.length());

        edt_Ammoun.setText(String.valueOf(amount));
        edt_Ammoun.setSelection(String.valueOf(amount).length());

        edt_Note.setText(note);
        edt_Note.setSelection(note.length());


        Button btnUpdate = mView.findViewById(R.id.btn_SAVE_upd);
        Button btnDelete = mView.findViewById(R.id.btn_delete_upd);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type=edt_Type.getText().toString().trim();

                String mAmmount=String.valueOf(amount);

                mAmmount=edt_Ammoun.getText().toString().trim();

                note=edt_Note.getText().toString().trim();

                int intammount=Integer.parseInt(mAmmount);

                String date=DateFormat.getDateInstance().format(new Date());

                MyData data=new MyData(type,intammount,note,date,post_key);

                mDatabase.child(post_key).setValue(data);


                dialog.dismiss();

            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });


        dialog.show();

    }

}