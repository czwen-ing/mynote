package com.example.mynote.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynote.adapter.FoldersListAdapter;
import com.example.mynote.bean.Folder;
import com.example.mynote.bean.Note;
import com.example.mynote.data.Constants;
import com.example.mynote.data.Notes;
import com.example.mynote.data.Notes.NoteColumns;
import com.example.mynote.data.Notes.FolderColumns;
import com.example.mynote.R;
import com.example.mynote.utils.ResourceParser;
import com.example.mynote.adapter.NoteRecyclerAdapter;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.utils.SetupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageButton menuButton;
    ImageButton toggleList;//切换Note的显示模式
    FloatingActionButton fab;//添加新Note
    RecyclerView recyclerView;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView tv_selected_count;
    CheckBox selectAll;//全选
    ViewGroup searchLayout;//搜索编辑框所在的布局
    TextView searchView;//搜索按钮，点击进入搜索模式
    ImageButton search_bt_back;//搜索模式的返回按钮
    ViewGroup overlapLayout;//搜素模式下搜索内容为空时的半透明覆盖层
    EditText searchEdit;//搜索编辑框
    NoteRecyclerAdapter adapter;
    List<Note> notes = new ArrayList<>();
    private StaggeredGridLayoutManager layoutManager;

    ModeCallback modeCallback;//长按进入删除模式
    List<Note> searchNotes = new ArrayList<>();//搜索到包含关键字的便签集合
    MyClickListener clickListener;
    private boolean isInRecyclerMode = true;//是否以瀑布流形式显示Notes

    private boolean isInSearchMode = false;//是否进入搜索模式
    private int sortWay;//排序方式
    private boolean isQuickDelete = false;
    public static final int FOLDER_ACTIVITY = 3;
    private static final int NOTE_ITEM = 1;
    private static final int NEW_NOTE = 2;

    public static final String[] NOTE_PROJECTION = new String[]{
            NoteColumns.ID,
            NoteColumns.PARENT_ID,
            NoteColumns.BG_ID,
            NoteColumns.CREATED_DATE,
            NoteColumns.MODIFIED_DATE,
            NoteColumns.CONTENT,
            NoteColumns.ALARM_DATE
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    overlapLayout.setVisibility(View.VISIBLE);
                    getNotesFromDataBase();
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    overlapLayout.setVisibility(View.GONE);
                    String s = (String) msg.obj;
                    getSearchNotes(s);
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Constants.isFolderChange){
            Constants.isFolderChange = false;
            getNotesFromDataBase();
            adapter.notifyDataSetChanged();
        }
    }

    private void checkPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }


    private void init() {

        sortWay = (Integer) SetupUtils.getParam(this,Notes.PREFERENCES_SORT_WAY,
                ResourceParser.DEFAULT_SORT_WAY);
        isQuickDelete = (Boolean) SetupUtils.getParam(this,Notes.PREFERENCES_QUICK_DELETE,false);

        getNotesFromDataBase();
        modeCallback = new ModeCallback();
        clickListener = new MyClickListener();

        searchLayout = (ViewGroup) findViewById(R.id.search_layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        menuButton = (ImageButton) findViewById(R.id.bt_menu);
        toggleList = (ImageButton) findViewById(R.id.toggle_list_recycler);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        searchView = (TextView) findViewById(R.id.search_tv);
        search_bt_back = (ImageButton) findViewById(R.id.search_back);
        overlapLayout = (ViewGroup) findViewById(R.id.overlap_layout);
        searchEdit = (EditText) findViewById(R.id.search_et);

        navigationView.setNavigationItemSelectedListener(new NavigationItemSelectedListener());
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NoteRecyclerAdapter(this, notes,sortWay);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new NoteRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int noteId) {
                if (adapter.isInChoiceMode()) {
                    modeCallback.onItemCheckedStateChanged(null, position, noteId, !adapter.isSelectedItem(position));
                    return;
                }
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra(NoteColumns.ID, noteId);
                intent.putExtra("position", position);
                startActivityForResult(intent, NOTE_ITEM);
            }
        });
        adapter.setOnItemLongClickListener(new NoteRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position, int id) {
                toolbar.startActionMode(modeCallback);
                modeCallback.onItemCheckedStateChanged(null, position, id, true);
            }
        });
        menuButton.setOnClickListener(clickListener);
        toggleList.setOnClickListener(clickListener);
        fab.setOnClickListener(clickListener);
        searchView.setOnClickListener(clickListener);
        search_bt_back.setOnClickListener(clickListener);
        searchLayout.setOnClickListener(clickListener);
        overlapLayout.setOnClickListener(clickListener);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(searchEdit.getText().toString().trim())){
                    handler.removeCallbacksAndMessages(null);
                    handler.sendEmptyMessage(0);
                    return;
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = s.toString().trim();
                handler.removeCallbacksAndMessages(null);
                handler.sendMessage(msg);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 从便签集合notes中获取到包含关键字的便签，并存放至searchNotes集合中
     * @param s 搜索的关键字
     */
    private void getSearchNotes(String s) {
        searchNotes.clear();
        getNotesFromDataBase();
        for (Note note : notes) {
            if (note.getContent().contains(s)) {
                searchNotes.add(note);
            }
        }
        notes.clear();
        notes.addAll(searchNotes);
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {//按时间的长短排序，长的在前
                if (o1.getModifiedTime() > o2.getModifiedTime()) {
                    return -1;
                } else if (o1.getModifiedTime() > o2.getModifiedTime()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.toggle_list_mode:
                if (isInRecyclerMode) {
                    isInRecyclerMode = false;
                    item.setIcon(R.drawable.grid);
                    layoutManager.setSpanCount(1);
                    adapter.setItemView(1);
                    getNotesFromDataBase();

                    adapter.notifyDataSetChanged();
                } else {
                    isInRecyclerMode = true;
                    item.setIcon(R.drawable.list);
                    adapter.setItemView(0);
                    layoutManager.setSpanCount(2);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NOTE_ITEM) {
            if (resultCode == Notes.RESULT_DELETE) {//删除
                int position = data.getIntExtra("position", 0);
                getNotesFromDataBase();
                if (isInSearchMode){//判断是否还处于搜索模式，是则继续搜索
                    getSearchNotes(searchEdit.getText().toString().trim());
                }
                adapter.notifyItemRemoved(position);
            } else if (resultCode == Notes.RESULT_MODIFIED) {//修改
                int position = data.getIntExtra("position", 0);
                getNotesFromDataBase();
                if (isInSearchMode){//判断是否还处于搜索模式，是则继续搜索
                    getSearchNotes(searchEdit.getText().toString().trim());
                }
                adapter.notifyItemChanged(position);
                adapter.notifyItemMoved(position, 0);
                recyclerView.smoothScrollToPosition(0);
            }
        } else if (requestCode == NEW_NOTE) {
            if (resultCode == Notes.RESULT_NEW) {//新建
                getNotesFromDataBase();
                adapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
            }
        } else if (requestCode == FOLDER_ACTIVITY){
            if (resultCode == RESULT_OK){
                getNotesFromDataBase();
                adapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * 从数据库中获取所有便签
     */
    public void getNotesFromDataBase() {
        notes.clear();
        Cursor cursor = getContentResolver().query(Notes.CONTENT_NOTE_URI, NOTE_PROJECTION, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setNoteId(cursor.getInt(cursor.getColumnIndex(NoteColumns.ID)));
                note.setBgId(cursor.getInt(cursor.getColumnIndex(NoteColumns.BG_ID)));
                note.setCreatedTime(cursor.getLong(cursor.getColumnIndex(NoteColumns.CREATED_DATE)));
                note.setModifiedTime(cursor.getLong(cursor.getColumnIndex(NoteColumns.MODIFIED_DATE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteColumns.CONTENT)));
                note.setAlarmDate(cursor.getLong(cursor.getColumnIndex(NoteColumns.ALARM_DATE)));
                notes.add(note);
            }
            Collections.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {//按时间的长短排序，长的在前
                    if (sortWay == ResourceParser.SORT_BY_CREATE_DATE){
                        if (o1.getCreatedTime() > o2.getCreatedTime()) {
                            return -1;
                        } else if (o1.getCreatedTime() > o2.getCreatedTime()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        if (o1.getModifiedTime() > o2.getModifiedTime()) {
                            return -1;
                        } else if (o1.getModifiedTime() > o2.getModifiedTime()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                }
            });
            cursor.close();
        }
    }

    /**
     * 多个删除
     */
    public void batchDelete() {
        DataUtils.deleteBatchNotes(getContentResolver(), adapter.getSelectedItemIds());
        modeCallback.finishActionMode();
        getNotesFromDataBase();
        adapter.notifyDataSetChanged();
    }



    private class ModeCallback implements AbsListView.MultiChoiceModeListener, MenuItem.OnMenuItemClickListener {
        ActionMode actionMode;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            adapter.setChoiceMode(true);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            adapter.setLongClickable(false);
            fab.setVisibility(View.GONE);

            //设置状态栏的颜色，原本为黑色
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorGrayBg));

            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.action_mode_layout, null);
            mode.setCustomView(view);
            selectAll = (CheckBox) view.findViewById(R.id.select_all);
            tv_selected_count = (TextView) view.findViewById(R.id.tv_selected_count);
            selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    adapter.selectAll(isChecked);
                    updateView();
                }
            });

            getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            menu.findItem(R.id.action_mode_delete).setOnMenuItemClickListener(this);
            menu.findItem(R.id.action_mode_moveto).setOnMenuItemClickListener(this);
            return true;
        }

        private void updateView() {
            tv_selected_count.setText("已选择" + adapter.getSelectedCount() + "项");
            selectAll.setChecked(adapter.isAllSelected());
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            fab.setVisibility(View.VISIBLE);
            adapter.setLongClickable(true);
            adapter.setChoiceMode(false);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            adapter.setCheckItem(position, checked);
            updateView();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (adapter.getSelectedCount() == 0) {
                return true;
            }
            switch (item.getItemId()) {
                case R.id.action_mode_delete:
                    if (!isQuickDelete){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("删除便签");
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setMessage("你确定要删除这" + adapter.getSelectedCount() + "便签吗？");
                        builder.setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        batchDelete();
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    } else {
                        batchDelete();
                    }
                    break;
                case R.id.action_mode_moveto:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("选择文件夹");
                    List<Folder> folders = DataUtils.getFoldersFromDB(getContentResolver());
                    if (folders!= null && folders.size() > 0){
                        Log.e("TAG","folder  count "+folders.size());
                        final FoldersListAdapter folderAdapter = new FoldersListAdapter(MainActivity.this,
                                folders,true);
                        builder.setAdapter(folderAdapter, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                DataUtils.batchMoveToFolder(getContentResolver(),
                                        adapter.getSelectedItemIds(), folderAdapter.getItemId(which));
                                modeCallback.finishActionMode();
                            }
                        });
                    } else {
                        builder.setMessage("还没有文件夹");
                    }
                    builder.show();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void finishActionMode() {
            actionMode.finish();
        }
    }

    private class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_folder:
                    Intent folderIntent = new Intent(MainActivity.this,FoldersActivity.class);
                    startActivityForResult(folderIntent,FOLDER_ACTIVITY);
                    break;
                case R.id.nav_new_folder:
                    showCreateFolderDialog();
                    break;
                case R.id.nav_new_note:
                    startNoteEditActivity();
                    break;
                case R.id.nav_wastepaper:
                    Toast.makeText(MainActivity.this, "wastepaper", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_setup:
                    Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                    startActivity(intent);
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        }
    }

    private class MyClickListener implements View.OnClickListener {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:
                    startNoteEditActivity();
                    break;
                case R.id.search_tv:
                    enterSearchMode();
                    break;
                case R.id.search_back:
                case R.id.overlap_layout:
                    exitSearchMode();
                    break;
                default:
                    break;
            }
        }
    }

    private void startNoteEditActivity() {
        Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
        intent.putExtra("isNewNote", true);
        startActivityForResult(intent, NEW_NOTE);
    }

    private void exitSearchMode() {
        searchView.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        overlapLayout.setVisibility(View.GONE);
        hideSoftInput();
        fab.show();
        isInSearchMode = false;
        adapter.setLongClickable(true);
        getNotesFromDataBase();
        adapter.notifyDataSetChanged();
    }

    private void enterSearchMode() {
        searchEdit.setText("");//每次进入前将搜索的关键字清空
        searchView.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        overlapLayout.setVisibility(View.VISIBLE);
        fab.hide();
        isInSearchMode = true;
        adapter.setLongClickable(false);
    }

    @Override
    public void onBackPressed() {
        if (isInSearchMode){
            exitSearchMode();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "请设置相关权限，否则影响一些功能", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showCreateFolderDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_foler_name);
        builder.setView(view);
        showSoftInput();
        builder.setTitle("新建便签夹");
        builder.setNegativeButton("取消",null);
        builder.setPositiveButton("新建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSoftInput();
                String name = etName.getText().toString();
                if (DataUtils.checkVisibleFolderName(getContentResolver(),etName.getText().toString())){
                    Toast.makeText(MainActivity.this,"已存在此便签夹",Toast.LENGTH_SHORT).show();
                    etName.setSelection(0, etName.length());
                    return;
                }
                if (!TextUtils.isEmpty(name)) {
                    ContentValues values = new ContentValues();
                    values.put(FolderColumns.FOLDER_NAME, name);
                    getContentResolver().insert(Notes.CONTENT_FOLDER_URI, values);
                }
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.show();
        final Button positive = (Button) dialog.findViewById(android.R.id.button1);
        if (TextUtils.isEmpty(etName.getText().toString())){
            positive.setEnabled(false);
        }
        etName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(etName.getText())) {
                    positive.setEnabled(false);
                } else {
                    positive.setEnabled(true);
                }
            }
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
