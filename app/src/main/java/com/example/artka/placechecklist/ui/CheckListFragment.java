package com.example.artka.placechecklist.ui;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.utils.CheckListAdapter;
import com.example.artka.placechecklist.utils.Utility;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CheckListFragment extends Fragment {

    private static final String ADDRESS = "address";
    private ArrayList<String> tasksList;

    @BindView(R.id.checklist_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab_checklist)
    FloatingActionButton floatingActionButton;

    private Unbinder unbinder;

    private CheckListAdapter checkListAdapter;
    private String addressText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checklist_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            addressText = bundle.getString(ADDRESS);
        }

        tasksList = new ArrayList<>();

        if (Utility.getCheckList(getContext(), addressText) != null) {
            tasksList = Utility.getCheckList(getContext(), addressText);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checkListAdapter = new CheckListAdapter(getContext(), tasksList, addressText);
        recyclerView.setAdapter(checkListAdapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                final EditText editText = new EditText(getContext());
                dialog.setTitle(R.string.dialog_add_task)
                        .setView(editText)
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String taskValue = editText.getText().toString();
                                tasksList.add(taskValue);
                                checkListAdapter.notifyDataSetChanged();
                            }
                        });
                dialog.show();

            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.saveChecklist(getContext(), addressText, tasksList);
    }

}
