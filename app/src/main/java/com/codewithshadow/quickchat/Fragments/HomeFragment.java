package com.codewithshadow.quickchat.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.codewithshadow.quickchat.Adapter.MessageUserAdapter;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    FirebaseUser user;
    List<UserModel> list;
    MessageUserAdapter adapter;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    AppSharedPreferences appSharedPreferences;
    DatabaseReference ref;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //AppSharedPreferences
        appSharedPreferences = new AppSharedPreferences(Objects.requireNonNull(getActivity()));

        //Firebase
        ref = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //RecyclerView
        recyclerView = view.findViewById(R.id.user_recycler);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView.setHasFixedSize(true);

        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        list = new ArrayList<>();


        //Search Edit Text
        EditText sv = view.findViewById(R.id.item_search_input);

        sv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchInDatabase(s);

            }
        });

        //Function
        Read_Users();


        swipeRefreshLayout.setOnRefreshListener(() -> {
            list.clear();
            Read_Users();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            },3000);
        });
        return view;

    }


    //--------------------------Search------------------------//
    private void SearchInDatabase(Editable s) {
            List<UserModel> mylist = new ArrayList<>();
            for (UserModel object : list) {
                if (object.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                    mylist.add(object);
                }
            }
            adapter  = new MessageUserAdapter(getContext(), mylist,true);
            recyclerView.setAdapter(adapter);
    }


    //----------------------------------Read Users--------------------------------//
    private void Read_Users() {
        ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel model = dataSnapshot.child("Info").getValue(UserModel.class);
                    assert model != null;
                    if (!model.getUserid().equals(user.getUid())) {
                        list.add(model);
                    }

                }
                adapter = new MessageUserAdapter(getContext(), list, true);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}

