package com.blueyleader.comicvine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class VolumeAdapter extends BaseAdapter {
    Volume[] set;
    Comic[][] comicSet;
    boolean[] extended;

    int[] collected;

    public VolumeAdapter(HashMap<Integer, Volume> set) {
        updateData(set);
    }

    public void updateData(HashMap<Integer, Volume> set){
        this.set = set.values().toArray(new Volume[0]);

        //TODO add more sort options
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

        this.comicSet = new Comic[this.set.length][];

        for(int x=0;x<this.set.length;x++){
            this.comicSet[x] = this.set[x].list.values().toArray(new Comic[0]);

            Arrays.sort(this.comicSet[x], new Comparator<Comic>() {
                public int compare(Comic o1, Comic o2) {
                    // Intentional: Reverse order for this demo
                    int issue = o1.issue.compareTo(o2.issue);
                    if(issue == 0){
                        int date = o1.date.compareTo(o2.date);
                        if(date == 0){
                            return o1.name.compareTo(o2.name);
                        }
                    }
                    return issue;
                }
            });
        }

        this.extended = new boolean[this.set.length];
    }

    @Override
    public int getCount() {
        return set.length;
    }

    @Override
    public Object getItem(int i) {
        return set[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.volume_view, viewGroup, false);

            holder.nameText = convertView.findViewById(R.id.name_text);
            holder.issueText = convertView.findViewById(R.id.issue_text);
            holder.comicList = convertView.findViewById(R.id.comic_view);

            //holder.adapter = new ComicAdapter(set[i].list);
            //holder.comicList.setAdapter(holder.adapter);
            //holder.extended = false;

            holder.issues = "";
            LayoutInflater inflater = LayoutInflater.from(convertView.getContext());
            for(int x = 0;x<comicSet[i].length;x++){
                holder.issues = holder.issues + comicSet[i][x].issue + ", ";
                View child = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_view, viewGroup, false);
                TextView name = child.findViewById(R.id.issue_name);
                holder.comicList.addView(child);
            }

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ref = i;



        holder.comicList.removeAllViews();
        holder.issues = "";
        for(int x = 0;x<comicSet[i].length;x++){
            holder.issues = holder.issues + comicSet[i][x].issue + ", ";
            View child = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_view, viewGroup, false);
            TextView name = child.findViewById(R.id.issue_name);
            name.setText(comicSet[i][x].issue + " - " + comicSet[i][x].date + " - " + comicSet[i][x].name);
            holder.comicList.addView(child);
        }

        holder.nameText.setText(set[i].name + " (" + set[i].date + ")");
        holder.issueText.setText(holder.issues);

        if(extended[i]){
            holder.issueText.setVisibility(View.GONE);
            holder.comicList.setVisibility(View.VISIBLE);
        }
        else{
            holder.issueText.setVisibility(View.VISIBLE);
            holder.comicList.setVisibility(View.GONE);
        }

        return convertView;
    }

    public class ViewHolder {
        TextView nameText;
        TextView issueText;
        //ListView comicList;
        //ComicAdapter adapter;
        LinearLayout comicList;
        int ref;
        String issues;
        //boolean extended;
    }
}
