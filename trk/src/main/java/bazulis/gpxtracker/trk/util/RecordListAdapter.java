package bazulis.gpxtracker.trk.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import bazulis.gpxtracker.trk.R;

/**
 * Created by bazulis on 14.7.12.
 * Pritaikytas failo saraso adapteris
 */
public class RecordListAdapter extends BaseAdapter {
    private List<File> files;
    private LayoutInflater inflater;

    public RecordListAdapter(List<File> files, LayoutInflater inf) {
        this.files = files;
        inflater = inf;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int arg) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.files_list, parent, false);
        final TextView filename = (TextView) convertView.findViewById(R.id.filename);
        filename.setText(files.get(position).getName());
        return convertView;
    }
}
