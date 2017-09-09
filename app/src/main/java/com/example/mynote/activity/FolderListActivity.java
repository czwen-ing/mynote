package com.example.mynote.activity;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mynote.R;
import com.example.mynote.adapter.NoteRecyclerAdapter;
import com.example.mynote.bean.Note;
import com.example.mynote.data.Constants;
import com.example.mynote.data.Notes;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.utils.ResourceParser;

import java.util.ArrayList;
import java.util.List;

public class FolderListActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<Note> notes;
    private NoteRecyclerAdapter adapter;
    long folderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);
        folderId = getIntent().getLongExtra("folderId",0);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        fabAdd.setOnClickListener(this);
        notes = new ArrayList<>();
        getNotesFromDB();
        if (notes != null){
            adapter = new NoteRecyclerAdapter(this,notes,ResourceParser.DEFAULT_SORT_WAY,1);
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this,"没有便签",Toast.LENGTH_SHORT).show();
        }
        collapsingToolbar.setTitle(getIntent().getStringExtra("folderName") + "("+ notes.size()+")" );
        adapter.setOnItemClickListener(new NoteRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int id) {
                Intent intent = new Intent(FolderListActivity.this, NoteEditActivity.class);
                intent.putExtra(Notes.NoteColumns.ID, id);
                intent.putExtra("position", position);
                startActivityForResult(intent, 1);
            }
        });
        Constants.isFolderChange = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.fab_add:
                Intent intent = new Intent(this,NoteEditActivity.class);
                intent.putExtra("folderId",folderId);
                intent.putExtra("isNewNote",true);
                startActivityForResult(intent,2);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Notes.RESULT_DELETE) {//删除
                int position = data.getIntExtra("position", 0);
                getNotesFromDB();
                adapter.notifyItemRemoved(position);
            } else if (resultCode == Notes.RESULT_MODIFIED) {//修改
                int position = data.getIntExtra("position", 0);
                getNotesFromDB();
                adapter.notifyItemChanged(position);
                adapter.notifyItemMoved(position, 0);
                recyclerView.smoothScrollToPosition(0);
            }
            Constants.isFolderChange = true;
        } else if (requestCode == 2) {
            if (resultCode == Notes.RESULT_NEW) {//新建
                getNotesFromDB();
                adapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
                collapsingToolbar.setTitle(getIntent().getStringExtra("folderName")
                        + "("+ notes.size()+")" );
                Constants.isFolderChange = true;
            }
        }
    }

    private void getNotesFromDB() {
        if (notes != null){
            notes.clear();
        }
        List<Note> list = DataUtils.getNotesFromDataBase(getContentResolver()
                , Notes.CONTENT_NOTE_URI
                , Notes.NoteColumns.PARENT_ID + "=?",new String[]{String.valueOf(folderId)}
                , ResourceParser.DEFAULT_SORT_WAY);
        if (list != null) {
            notes.addAll(list);
        }
    }
}
