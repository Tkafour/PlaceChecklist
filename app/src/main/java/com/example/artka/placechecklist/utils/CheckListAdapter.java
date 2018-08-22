package com.example.artka.placechecklist.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artka.placechecklist.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder> {

    private final Context context;
    private final ArrayList<String> dataList;
    private final ArrayList<Boolean> isChecked;
    private final String address;

    public CheckListAdapter(Context context, ArrayList<String> data, String address) {
        this.context = context;
        this.dataList = data;
        this.address = address;
        if (Utility.getCheckBoxList(this.context, this.address) != null) {
            isChecked = Utility.getCheckBoxList(this.context, this.address);
        } else {
            isChecked = new ArrayList<>(dataList.size());
        }
    }

    @NonNull
    @Override
    public CheckListAdapter.CheckListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.checklist_item, parent, false);
        return new CheckListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckListAdapter.CheckListViewHolder holder, int position) {
        String task = dataList.get(position);
        holder.checkListText.setText(task);
        holder.checkListCheckBox.setTag(position);
        if (isChecked.size() < dataList.size()) {
            isChecked.add(false);
        }
        holder.checkListCheckBox.setChecked(isChecked.get(position));

        Utility.saveChecklist(context, address, dataList);
        Utility.saveCheckBoxes(context, address, isChecked);
    }

    private void allCheckBoxesChecked() {
        Utility.saveCheckBoxes(context, address, isChecked);
        for (int i = 0; i < isChecked.size(); i++) {
            if (isChecked.contains(false) || isChecked.contains(null)) {
                return;
            }
        }
        Toast.makeText(context, R.string.all_checked, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    class CheckListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.checklist_text)
        TextView checkListText;
        @BindView(R.id.checklist_checkbox)
        CheckBox checkListCheckBox;
        @BindView(R.id.delete_checklist_item)
        ImageView deleteItemView;

        public CheckListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            deleteItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.equals(deleteItemView)) {
                removeAt(getPosition());
            } else if (v.equals(checkListCheckBox)) {
                isChecked.set(getAdapterPosition(), checkListCheckBox.isChecked());
                allCheckBoxesChecked();
            }
        }

        public void removeAt(int position) {
            dataList.remove(position);
            isChecked.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, dataList.size());
            Utility.saveChecklist(context, address, dataList);
            Utility.saveCheckBoxes(context, address, isChecked);
        }
    }
}
