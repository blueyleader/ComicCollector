package com.blueyleader.comiccollector;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class VolumeAdapter extends BaseAdapter implements Filterable{
    ArrayList<displayHolder> set;
    ArrayList<displayHolder> filterSet;
    private ValueFilter valueFilter;

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
                String n1 = o1.name;
                String n2 = o2.name;
                if(n1.startsWith("The ")){
                    n1 = n1.substring(4);
                }
                if(n2.startsWith("The")){
                    n2 = n2.substring(4);
                }
                int name = n1.compareTo(n2);
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
                    int issue;
                    try {
                        int a = Integer.parseInt(o1.issue);
                        int b = Integer.parseInt(o2.issue);
                        issue= a-b;
                    }
                    catch (Exception e){
                        issue=o1.issue.compareTo(o2.issue);
                    }

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
        Log.d("ComicVine","alot of work");
        holder.ref = i;



        holder.comicList.removeAllViews();
        holder.issues = "";
        for(int x = 0;x<set.get(i).comics.size();x++){
            holder.issues = holder.issues + set.get(i).comics.get(x).issue + ", ";
            View child = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comic_view, viewGroup, false);
            TextView name = child.findViewById(R.id.issue_name);
            child.setTag(set.get(i).comics.get(x));
            child.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        new GetImageTask().execute((Comic)v.getTag());
                    } catch (Exception e) {
                        Log.d("ComicCollector", "got a long press");
                    }
                    return false;
                }
            });
            CheckBox col = child.findViewById(R.id.collected);
            col.setChecked(set.get(i).comics.get(x).collected);
            col.setTag(set.get(i).comics.get(x));
            col.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Comic comic = (Comic)view.getTag();
                    if(((CheckBox)view).isChecked()){
                        MainActivity.self.collected.add(comic.id);
                        comic.collected=true;
                    }
                    else{
                        MainActivity.self.collected.remove(comic.id);
                        comic.collected=true;
                    }
                    MainActivity.self.saveCollected();
                }
            });
            name.setText(set.get(i).comics.get(x).issue + " - " + set.get(i).comics.get(x).date + " - " + set.get(i).comics.get(x).name);

            holder.comicList.addView(child);
        }
        if(holder.issues.length()>0) {
            holder.issues = holder.issues.substring(0, holder.issues.length() - 2);
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
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (MainActivity.self);
            boolean col = sh.getBoolean("show_collected",false);
            boolean notCol = sh.getBoolean("show_not_collected",true);
            ArrayList<displayHolder> filterList=new ArrayList<>();

            boolean caseSen = sh.getBoolean("case_sensitive_search",false);
            boolean searchVolumeName = sh.getBoolean("search_volume_name",true);
            boolean searchVolumeDate = sh.getBoolean("search_volume_date",true);
            boolean searchIssueNum = sh.getBoolean("search_issue_num",true);
            boolean searchIssueName = sh.getBoolean("search_issue_name",false);
            boolean searchIssueDate = sh.getBoolean("search_issue_date",false);
            for(int i=0;i<filterSet.size();i++){
                boolean filter = false;
                String volName = filterSet.get(i).vol.name;
                String volDate = filterSet.get(i).vol.date;
                String check = constraint.toString();
                if(!caseSen){
                    volName = volName.toUpperCase();
                    volDate = volDate.toUpperCase();
                    check = check.toUpperCase();
                }

                if(searchVolumeName && volName.contains(check)){
                    filter = true;
                }
                if(searchVolumeDate && volDate.contains(check)){
                    filter = true;
                }

                if(searchIssueNum || searchIssueName || searchIssueDate) {
                    for (int x = 0; x < filterSet.get(i).comics.size() && !filter; x++) {
                        String issueNum = filterSet.get(i).comics.get(x).issue+"";
                        String issueName = filterSet.get(i).comics.get(x).name;
                        String issueDate = filterSet.get(i).comics.get(x).date;
                        if (!caseSen) {
                            issueNum = issueNum.toUpperCase();
                            issueName = issueName.toUpperCase();
                            issueDate = issueDate.toUpperCase();
                        }

                        if(searchIssueNum && issueNum.contains(check)){
                            filter = true;
                        }
                        if(searchIssueName && issueName.contains(check)){
                            filter = true;
                        }
                        if(searchIssueDate && issueDate.contains(check)){
                            filter = true;
                        }
                    }
                }

                ArrayList<Comic> filterComic=new ArrayList<>();
                for(int x = 0;x<filterSet.get(i).filterComics.size();x++){
                    if(col && filterSet.get(i).filterComics.get(x).collected || notCol && !filterSet.get(i).filterComics.get(x).collected){
                        filterComic.add(filterSet.get(i).filterComics.get(x));
                    }
                }

                filterSet.get(i).comics=filterComic;

                if(filterSet.get(i).comics.size()!=0) {
                    if (filter) {
                        filterList.add(filterSet.get(i));
                    } else if (!(constraint != null && constraint.length() > 0)) {
                        filterList.add(filterSet.get(i));
                    }
                }
            }

            results.count=filterList.size();
            results.values=filterList;
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            Log.d("ComicVine","filter results");
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
        ArrayList<Comic> filterComics;
        View child;
        boolean extended;

        private displayHolder(Volume v, ArrayList<Comic> c, boolean e) {
            vol = v;
            comics = c;
            filterComics = comics;
            extended = e;
        }
    }

    private class GetImageTask extends AsyncTask<Comic, int[], Bitmap> {
        Comic c;
        @Override
        protected Bitmap doInBackground(Comic... params) {
            c=params[0];
            try {
                URL url = new URL(c.image);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmapResult) {
            super.onPostExecute(bitmapResult);
            View view = LayoutInflater.from(MainActivity.self).inflate(R.layout.image_dialog, null, false);
            ImageView img = view.findViewById(R.id.imageView);
            img.setTag(c);
            img.setImageBitmap(bitmapResult);
            img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((Comic) v.getTag()).url));
                    MainActivity.self.startActivity(browserIntent);
                    return false;
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.self);
            builder.setView(view);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
