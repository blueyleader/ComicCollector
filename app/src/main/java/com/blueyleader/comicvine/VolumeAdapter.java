package com.blueyleader.comicvine;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

class VolumeAdapter extends RecyclerView.Adapter<VolumeAdapter.ViewHolder> {
    Volume[] set;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView issues;
        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.name_text);
            issues = v.findViewById(R.id.issue_text);
        }
    }


    public VolumeAdapter(HashMap<Integer, Volume> set) {
        updateData(set);
    }

    public void updateData(HashMap<Integer, Volume> set){
        this.set = set.values().toArray(new Volume[0]);

        Arrays.sort(this.set, new Comparator<Volume>() {
            public int compare(Volume o1, Volume o2) {
                // Intentional: Reverse order for this demo
                int name = o1.name.compareTo(o2.name);
                if(name == 0){
                    return o1.date.compareTo(o2.date);
                }
                return name;
            }
        });
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.textview, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.mTextView.setText(set[i].name + " " + set[i].date);

        Comic[] list = set[i].list.values().toArray(new Comic[0]);

        Arrays.sort(list, new Comparator<Comic>() {
            public int compare(Comic o1, Comic o2) {
                // Intentional: Reverse order for this demo
                return o1.issue.compareTo(o2.issue);
            }
        });
        String out = "";
        for(int x=0;x<list.length;x++){
            out=out+list[x].issue+",";
        }
        holder.issues.setText(out);
    }



    @Override
    public int getItemCount() {
        return set.length;
    }
}
