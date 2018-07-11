package com.blueyleader.comicvine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class ComicAdapter extends BaseAdapter {
    public Comic[] set;

    public ComicAdapter(HashMap<Integer, Comic> set){
        updateData(set);
    }
    public void updateData(HashMap<Integer, Comic> set){
        this.set = set.values().toArray(new Comic[0]);

        Arrays.sort(this.set, new Comparator<Comic>() {
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
        final ComicHolder holder;
        if(convertView == null) {
            holder = new ComicHolder();
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_view, viewGroup, false);
            holder.issueText = convertView.findViewById(R.id.issue_name);
            convertView.setTag(holder);
        }
        else{
            holder = (ComicHolder) convertView.getTag();
        }

        holder.ref = i;
        holder.issueText.setText(set[i].issue + " - " + set[i].date + " - " + set[i].name);

        return convertView;
    }

    public class ComicHolder {
        TextView issueText;
        int ref;
    }
}
