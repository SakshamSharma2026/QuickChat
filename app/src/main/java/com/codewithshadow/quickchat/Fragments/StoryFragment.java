package com.codewithshadow.quickchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codewithshadow.quickchat.Models.UserModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Adapter.StoryAdapter;
import com.codewithshadow.quickchat.Models.StoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoryFragment extends Fragment {
    RecyclerView recyclerView;
    List<StoryModel> storyModelList;
    LinearLayoutManager manager;
    StoryAdapter storyAdapter;
    FirebaseAuth auth;
    FirebaseUser user;
    private List<String> followingList;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StoryFragment newInstance(String param1, String param2) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_story, container, false);
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.mystoryrecycler);
        storyModelList = new ArrayList<>();
        followingList = new ArrayList<>();
        manager = new LinearLayoutManager(getContext());
        storyAdapter = new StoryAdapter(getActivity(),storyModelList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(storyAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        checkFollowing();
        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                UserModel model = null;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    model = dataSnapshot.child("Info").getValue(UserModel.class);
                    if (!model.getUserid().equals(user.getUid())) {
                        followingList.add(dataSnapshot.getKey());
                        System.out.println(followingList);
                    }

                }
                readStory();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readStory()
    {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Story");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyModelList.clear();
                storyModelList.add(new StoryModel("",0,0, FirebaseAuth.getInstance().getCurrentUser().getUid(),"",""));
                for (String id : followingList)
                {
                    int countStory = 0;
                    StoryModel storyModel = null;
                    for (DataSnapshot snapshot2 : snapshot.child(id).getChildren()) {
                        storyModel = snapshot2.getValue(StoryModel.class);
                        if (timecurrent > storyModel.getTimestart() && timecurrent < storyModel.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0)
                    {
                        storyModelList.add(storyModel);
                    }
                }


                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}