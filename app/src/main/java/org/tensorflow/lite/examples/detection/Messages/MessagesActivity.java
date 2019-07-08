package org.tensorflow.lite.examples.detection.Messages;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.tensorflow.lite.examples.detection.Homescreen;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.SDFileHandler;
import org.tensorflow.lite.examples.detection.SQLHandler.AppDatabase;
import org.tensorflow.lite.examples.detection.SQLHandler.Msg;
import org.tensorflow.lite.examples.detection.SetupActivity;
import org.tensorflow.lite.examples.detection.SharedPreferences.SharedPreferenceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MessagesActivity extends AppCompatActivity implements
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{

    private RecyclerView recyclerView;
    private List<Msg> msgList = new ArrayList<>();
    MsgAdapter adapter;
    AppDatabase database;
    CoordinatorLayout coordinatorLayout;
    int type;
    private static final String TAG = MessagesActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = findViewById(R.id.coordinator);

        database = AppDatabase.getAppDatabase(this);

        recyclerView = findViewById(R.id.recycler_msgs);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new MsgAdapter(msgList, this);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.msg_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.spinner_inbox);
        Spinner spinner_inbox = (Spinner) menuItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.messages_drop, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.single_spinner_row);
        spinner_inbox.setAdapter(adapter);
        spinner_inbox.setPadding(4, 0, 4, 0);
        String node_type = SharedPreferenceHandler.getStringValues(getApplicationContext(),
                SetupActivity.TYPE);
        if(node_type.equals(SetupActivity.IN)){
            Log.d(TAG, node_type);
            menu.findItem(R.id.spinner_inbox).setVisible(false);
            type = 2;
            notifyChange();
        }

        spinner_inbox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = position;
                notifyChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return true;
    }

    private void notifyChange() {
            String msg_type = convertIntToMsgType(type);
            msgList.clear();
            msgList.addAll(database.dao().getMsgs(msg_type));
            adapter.notifyDataSetChanged();
    }

    private String convertIntToMsgType(int type){
        if(type == 0) return Homescreen.OWN;
        else if (type == 1) return Homescreen.RECEIVED;
        else return Homescreen.INTERMEDIATE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyChange();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MsgAdapter.MsgViewHolder) {
            final Msg msg = msgList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(viewHolder.getAdapterPosition());
            database.dao().deleteMsg(msg);
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Msg has been removed!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", view -> {

                // undo is selected, restore the deleted item
                adapter.restoreItem(msg, deletedIndex);
                database.dao().insertMsg(msg);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
