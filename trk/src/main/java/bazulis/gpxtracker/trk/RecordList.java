package bazulis.gpxtracker.trk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bazulis.gpxtracker.trk.util.RecordListAdapter;

public class RecordList extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        loadList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }

    private void loadList() {
        final List<File> files = readFiles();
        final ListView listView = (ListView) findViewById(R.id.list_files);
        RecordListAdapter adapter = new RecordListAdapter(files, getLayoutInflater());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent req = getIntent();
                int requestCode = req.getIntExtra("requestCode", 0);
                if (requestCode == 0) {
                    Intent intent = new Intent(getApplicationContext(), GPXDetails.class);
                    intent.putExtra("filename", files.get(i).getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 0);
                }
                if (requestCode == 1) {
                    Intent result = new Intent();
                    result.putExtra("filename", files.get(i).getAbsolutePath());
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }
        });
    }

    private List<File> readFiles() {
        List<File> files = new ArrayList<>();
        String adr = Environment.getExternalStorageDirectory().getPath() + "/gpx";
        File file = new File(adr);
        if (!file.exists()) {
            boolean mk = file.mkdir();
            System.out.println(mk);
        }
        Collections.addAll(files, file.listFiles());
        Comparator<File> comparator = Collections.reverseOrder();
        Collections.sort(files, comparator);
        return files;
    }
}
