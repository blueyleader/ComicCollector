package com.blueyleader.comicvine;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class VolumeAdapter extends BaseAdapter implements Filterable{
    ArrayList<displayHolder> set;
    ArrayList<displayHolder> filterSet;
    private ValueFilter valueFilter;

    int[] collected;

    public VolumeAdapter(HashMap<Integer, Volume> set) {
        updateData(set);
        getFilter();
    }

    public void updateData(HashMap<Integer, Volume> set){
        this.set = new ArrayList<>();
        Volume[] vol = set.values().toArray(new Volume[0]);

        //TODO add more sort options
        Arrays.sort(vol, new Comparator<Volume>() {
            public int compare(Volume o1, Volume o2) {
                int name = o1.name.compareTo(o2.name);
                if(name == 0){
                    return o1.date.compareTo(o2.date);
                }
                return name;
            }
        });

        Comic[][] comicSet = new Comic[vol.length][];

        for(int x=0;x<vol.length;x++){
            comicSet[x] = vol[x].list.values().toArray(new Comic[0]);

            Arrays.sort(comicSet[x], new Comparator<Comic>() {
                public int compare(Comic o1, Comic o2) {
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
            Comic a = comicSet[x][0];

        }

        for(int x=0;x<vol.length;x++){
            this.set.add(new displayHolder(vol[x],new ArrayList<>(Arrays.asList(comicSet[x])), false));
        }
        filterSet=this.set;
    }

    @Override
    public int getCount() {
        return set.size();
    }

    @Override
    public Object getItem(int i) {
        return set.get(i);
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

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ref = i;



        holder.comicList.removeAllViews();
        holder.issues = "";
        for(int x = 0;x<set.get(i).comics.size();x++){
            holder.issues = holder.issues + set.get(i).comics.get(x).issue + ", ";
            View child = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_view, viewGroup, false);
            TextView name = child.findViewById(R.id.issue_name);
            name.setText(set.get(i).comics.get(x).issue + " - " + set.get(i).comics.get(x).date + " - " + set.get(i).comics.get(x).name);
            holder.comicList.addView(child);
        }

        holder.nameText.setText(set.get(i).vol.name + " (" + set.get(i).vol.date + ")");
        holder.issueText.setText(holder.issues);

        if(set.get(i).extended){
            holder.issueText.setVisibility(View.GONE);
            holder.comicList.setVisibility(View.VISIBLE);
        }
        else{
            holder.issueText.setVisibility(View.VISIBLE);
            holder.comicList.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if(valueFilter==null) {

            valueFilter=new ValueFilter();
        }

        return valueFilter;
    }

    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results=new FilterResults();
            if(constraint!=null && constraint.length()>0){
                ArrayList<displayHolder> filterList=new ArrayList<>();
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (MainActivity.self);
                boolean caseSen = sh.getBoolean("case_sensitive_search",false);
                Set<String> searchVolume = sh.getStringSet("search_volume",new HashSet<String>());
                for(int i=0;i<filterSet.size();i++){
                    boolean filter = false;
                    String volName = filterSet.get(i).vol.name;
                    String volDate = filterSet.get(i).vol.date;
                    String check = constraint.toString();
                    if(!caseSen){
                        Log.d("ComicVine","toupper");
                        volName = volName.toUpperCase();
                        volDate = volDate.toUpperCase();
                        check = check.toUpperCase();
                    }


                    if(searchVolume != null){
                        Iterator<String> iterator = searchVolume.iterator();
                        while(iterator.hasNext() && !filter){
                            switch(iterator.next()){
                                case "0":
                                    if(volName.contains(check)){
                                        filter = true;
                                    }
                                    break;
                                case "1":
                                    if(volDate.contains(check)){
                                        filter = true;
                                    }
                                    break;
                            }
                        }
                    }

                    Set<String> searchComic = sh.getStringSet("search_comic", new HashSet<String>());

                    for(int x=0;x<filterSet.get(i).comics.size() && !filter;x++) {
                        String issueNum = filterSet.get(i).comics.get(x).issue;
                        String issueName = filterSet.get(i).comics.get(x).name;
                        String issueDate = filterSet.get(i).comics.get(x).date;
                        if(!caseSen){
                            issueNum = issueNum.toUpperCase();
                            issueName = issueName.toUpperCase();
                            issueDate = issueDate.toUpperCase();
                        }

                        if(searchComic != null) {
                            Iterator<String> iterator = searchComic.iterator();
                            while(iterator.hasNext() && !filter) {
                                switch(iterator.next()) {
                                    case "0":
                                        if(issueNum.contains(check)) {
                                            filter = true;
                                        }
                                        break;
                                    case "1":
                                        if(issueName.contains(check)) {
                                            filter = true;
                                        }
                                        break;
                                    case "2":
                                        if(issueDate.contains(check)) {
                                            filter = true;
                                        }
                                        break;
                                }
                            }
                        }
                    }

                    if(filter){
                        filterList.add(filterSet.get(i));
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
            }else{
                results.count=filterSet.size();
                results.values=filterSet;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            set=(ArrayList<displayHolder>) results.values;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder {
        TextView nameText;
        TextView issueText;
        LinearLayout comicList;
        int ref;
        String issues;
    }

    public class displayHolder {
        Volume vol;
        ArrayList<Comic> comics;
        boolean extended;

        public displayHolder(Volume v){
            vol = v;
        }

        public displayHolder(Volume v, ArrayList<Comic> c) {
            vol = v;
            comics = c;
        }

        public displayHolder(Volume v, ArrayList<Comic> c, boolean e) {
            vol = v;
            comics = c;
            extended = e;
        }
    }
}
