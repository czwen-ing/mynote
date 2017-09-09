package com.example.mynote.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mynote.R;
import com.example.mynote.adapter.FoldersListAdapter;
import com.example.mynote.bean.Folder;
import com.example.mynote.data.Constants;
import com.example.mynote.data.Notes;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.data.Notes.NoteColumns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FoldersActivity extends AppCompatActivity {

    private ListView folderListView;
    private List<Folder> folders;
    private FoldersListAdapter adapter;
    private ImageButton createFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        folders = new ArrayList<>();
        getFoldersFromDB();
        folderListView = (ListView) findViewById(R.id.folder_list_view);
        registerForContextMenu(folderListView);
        createFolder = (ImageButton) findViewById(R.id.create_folder);
        if (folders != null) {
            adapter = new FoldersListAdapter(this, folders);
            folderListView.setAdapter(adapter);
            folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(FoldersActivity.this, FolderListActivity.class);
                    intent.putExtra("folderId", folders.get(position).getId());
                    Log.e("TAG", " folder notes folders.get(position).getId()" + folders.get(position).getId());
                    intent.putExtra("folderName", folders.get(position).getName());
                    startActivity(intent);
                }
            });
        }
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });
        Constants.isFolderChange = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Constants.isFolderChange) {
            getFoldersFromDB();
            adapter.notifyDataSetChanged();
        }
    }

    private void getFoldersFromDB() {
        if (folders != null) {
            folders.clear();
        }
        List<Folder> list = DataUtils.getFoldersFromDB(getContentResolver());
        if (list != null) {
            folders.addAll(list);
        }
    }

    public void back(View view) {
        finish();
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_foler_name);
        builder.setView(view);
        showSoftInput();
        builder.setTitle("新建便签夹");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("新建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSoftInput();
                String name = etName.getText().toString();
                if (DataUtils.checkVisibleFolderName(getContentResolver(), etName.getText().toString())) {
                    Toast.makeText(FoldersActivity.this, "已存在此便签夹", Toast.LENGTH_SHORT).show();
                    etName.setSelection(0, etName.length());
                    return;
                }
                if (!TextUtils.isEmpty(name)) {
                    ContentValues values = new ContentValues();
                    values.put(Notes.FolderColumns.FOLDER_NAME, name);
                    getContentResolver().insert(Notes.CONTENT_FOLDER_URI, values);
                    getFoldersFromDB();
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.show();
        final Button positive = (Button) dialog.findViewById(android.R.id.button1);
        if (TextUtils.isEmpty(etName.getText().toString())) {
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.folder_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long folderId = info.id;
        int position = info.position;
        switch (item.getItemId()) {
            case R.id.delete_folder:
                showDeleteFolderDialog(folderId);
                break;
            case R.id.change_folder_name:
                showChangeFolderNameDialog(folderId,folders.get(position).getName());
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteFolderDialog(final long folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FoldersActivity.this);
        builder.setTitle("删除便签夹");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage("你确定要删除此便签夹以及其包含的便签吗？");
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        deleteFolder(folderId);
                        getFoldersFromDB();
                        adapter.notifyDataSetChanged();
                        Constants.isFolderChange = true;
                    }
                });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void deleteFolder(long folderId) {
        HashSet<Long> ids = new HashSet<>();
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI, new String[]{NoteColumns.ID},
                NoteColumns.PARENT_ID + "=?", new String[]{String.valueOf(folderId)},null);
        if (cursor != null){
            while (cursor.moveToNext()){
                long noteId = cursor.getLong(cursor.getColumnIndex(NoteColumns.ID));
                ids.add(noteId);
            }
            cursor.close();
        }
        DataUtils.deleteBatchNotes(resolver,ids);
        resolver.delete(ContentUris.withAppendedId(Notes.CONTENT_FOLDER_URI,folderId),null,null);
    }

    private void showChangeFolderNameDialog(final long folderId, String folderName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_foler_name);
        etName.setText(folderName);
        etName.setSelection(folderName.length());
        builder.setView(view);
        showSoftInput();
        builder.setTitle("修改便签夹名称");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSoftInput();
                String name = etName.getText().toString();
                if (DataUtils.checkVisibleFolderName(getContentResolver(), etName.getText().toString())) {
                    Toast.makeText(FoldersActivity.this, "已存在此便签夹", Toast.LENGTH_SHORT).show();
                    etName.setSelection(0, etName.length());
                    return;
                }
                if (!TextUtils.isEmpty(name)) {
                    ContentValues values = new ContentValues();
                    values.put(Notes.FolderColumns.FOLDER_NAME, name);
                    getContentResolver().update(ContentUris.withAppendedId(Notes.CONTENT_FOLDER_URI,folderId)
                            , values,null,null);
                    getFoldersFromDB();
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.show();
        final Button positive = (Button) dialog.findViewById(android.R.id.button1);
        if (TextUtils.isEmpty(etName.getText().toString())) {
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
}
