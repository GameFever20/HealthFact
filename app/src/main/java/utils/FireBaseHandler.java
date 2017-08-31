package utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Aisha on 8/31/2017.
 */

public class FireBaseHandler {

    private DatabaseReference mDatabaseRef;
    private FirebaseDatabase mFirebaseDatabase;


    public FireBaseHandler() {

        mFirebaseDatabase = FirebaseDatabase.getInstance();

    }

    public void downloadHealthFact(String healthFactUID, final OnHealthFactlistener onHealthFactlistener) {


        DatabaseReference myRef = mFirebaseDatabase.getReference().child("HealthFact/" + healthFactUID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HealthFact healthFact = dataSnapshot.getValue(HealthFact.class);

                if (healthFact != null) {
                    healthFact.setmHealthFactID(dataSnapshot.getKey());
                }
                onHealthFactlistener.OnHealthFactlistener(healthFact, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onHealthFactlistener.OnHealthFactlistener(null, false);
            }
        });


    }

    public void downloadHealthFactList(int limit, String lastHealthFactID, final OnHealthFactlistener onHealthFactlistener) {


        mDatabaseRef = mFirebaseDatabase.getReference().child("HealthFact/");

        Query myref2 = mDatabaseRef.orderByKey().limitToLast(limit).endAt(lastHealthFactID);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HealthFact> healthFactsArrayList = new ArrayList<HealthFact>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthFact healthFact = snapshot.getValue(HealthFact.class);
                    if (healthFact != null) {
                        healthFact.setmHealthFactID(snapshot.getKey());
                    }
                    healthFactsArrayList.add(healthFact);
                }

                healthFactsArrayList.remove(healthFactsArrayList.size() - 1);
                Collections.reverse(healthFactsArrayList);
                onHealthFactlistener.OnHealthFactListlistener(healthFactsArrayList, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onHealthFactlistener.OnHealthFactListlistener(null, false);

            }
        });


    }


    public void downloadHealthFactList(int limit, final OnHealthFactlistener onHealthFactlistener) {


        mDatabaseRef = mFirebaseDatabase.getReference().child("HealthFact/");

        Query myref2 = mDatabaseRef.limitToLast(limit);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<HealthFact> healthFactArrayList = new ArrayList<HealthFact>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    HealthFact healthFact = snapshot.getValue(HealthFact.class);
                    if (healthFact != null) {

                        healthFact.setmHealthFactID(snapshot.getKey());

                    }
                    healthFactArrayList.add(healthFact);
                }

                Collections.reverse(healthFactArrayList);

                onHealthFactlistener.OnHealthFactListlistener(healthFactArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onHealthFactlistener.OnHealthFactListlistener(null, false);

            }
        });


    }


    public void uploadHealthFact(final HealthFact healthFact, final OnHealthFactlistener onHealthFactlistener) {


        mDatabaseRef = mFirebaseDatabase.getReference().child("HealthFact/");

        healthFact.setmHealthFactID(mDatabaseRef.push().getKey());

        DatabaseReference mDatabaseRef1 = mFirebaseDatabase.getReference().child("HealthFact/" + healthFact.getmHealthFactID());


        mDatabaseRef1.setValue(healthFact).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onHealthFactlistener.OnHealthFactlistener(healthFact, true);
                onHealthFactlistener.onHealthFactUpload(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Failed to Upload Story", e.getMessage());

                onHealthFactlistener.onHealthFactUpload(false);
                onHealthFactlistener.OnHealthFactlistener(null, false);
            }
        });


    }

    public interface OnHealthFactlistener {


        public void OnHealthFactlistener(HealthFact healthFact, boolean isSuccessful);

        public void OnHealthFactListlistener(ArrayList<HealthFact> healthFacts, boolean isSuccessful);


        public void onHealthFactUpload(boolean isSuccessful);
    }
}
