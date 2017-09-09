package com.example.mynote.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.mynote.R;
import com.example.mynote.adapter.BgSelectorAdapter;
import com.example.mynote.bean.Weather;
import com.example.mynote.data.Constants;
import com.example.mynote.data.Notes;
import com.example.mynote.data.Notes.NoteColumns;
import com.example.mynote.data.Notes.MediaColumns;
import com.example.mynote.ui.DateTimePickerDialog;
import com.example.mynote.ui.RichTextEditor;
import com.example.mynote.utils.DataUtils;
import com.example.mynote.utils.ImageUtils;
import com.example.mynote.utils.ResourceParser;
import com.example.mynote.utils.ResourceParser.NoteBgResources;
import com.example.mynote.utils.ResourceParser.TextAppearanceResources;
import com.example.mynote.utils.ScreenUtils;
import com.example.mynote.utils.SetupUtils;
import com.google.gson.Gson;
import com.google.gson.internal.Streams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoteEditActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_ALBUM = 2;
    private static final int PHOTO_RESULT = 3;
    private boolean isNewNote = false;//是否新建的便签
    private boolean isLocalModefied;//是否局部修改 判断note内容设置是否改变
    private boolean isQuickDelete = false;
    private boolean isMenuShow = false;
    private BgSelectorAdapter adapter;
    ViewGroup bg_ll;
    Button back;
    ImageButton menuOpen;
    TextView date_tv;
    TextView time_tv;
    TextView clock_tv;
    ImageButton delete;
    ImageButton camera_bt;
    ImageButton location_bt;
    ImageButton weather_bt;
    ImageButton addBackground;
    RichTextEditor note_et;
    RecyclerView bgSelector;
    Switch remindSwitch;
    TextView lengthView;
    PopupWindow menuWindow;

    private String content;
    private long modifiedTime;
    private long parentId;
    private int bgId;
    private int noteId;
    private long alarmDate = 0;
    private boolean hasAlarm = false;
    private int textLength;

    private int position;
    private int fontSizeId;

    private boolean isShowTextLength = false;
    private String currentFilePath;
    private LocationClient locationClient;
    private String currentLocation;
    private String location;

    HashMap<Integer, byte[]> imageMap = new HashMap<>();
    private String city = "梅州";

    WindowManager.LayoutParams params;
    public static final String[] NOTE_PROJECTION = new String[]{
            NoteColumns.PARENT_ID,
            NoteColumns.MODIFIED_DATE,
            NoteColumns.ALARM_DATE,
            NoteColumns.BG_ID,
            NoteColumns.CONTENT,
            NoteColumns.TEXT_LENGTH,
            NoteColumns.LOCATION,
    };
    public static final String[] MEDIA_PROJECTION = new String[]{
            MediaColumns.ID,
            MediaColumns.IMAGE_PATH,
            MediaColumns.IMAGE_INDEX,
            MediaColumns.IMAGE_BLOB
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Intent intent = getIntent();
        if (intent != null){
            parentId = intent.getLongExtra("folderId",0);
            isNewNote = intent.getBooleanExtra("isNewNote", false);
            noteId = intent.getIntExtra(NoteColumns.ID, 0);
            position = intent.getIntExtra("position", 0);
        }
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new SimpleLocationListener());
        checkPermissions();
        initView();
        if (isNewNote) {
            initNewNote();
        } else {
            note_et.setNoteId(noteId);//设置编辑器对应的便签Id
            getDataFromDataBase();
            setNote();
        }
    }

    private void initView() {
        adapter = new BgSelectorAdapter(this);

        bg_ll = (ViewGroup) findViewById(R.id.edit_bg_ll);
        back = (Button) findViewById(R.id.bt_back);
        menuOpen = (ImageButton) findViewById(R.id.menu_open);
        date_tv = (TextView) findViewById(R.id.date_text);
        time_tv = (TextView) findViewById(R.id.time_text);
        clock_tv = (TextView) findViewById(R.id.clock_text);
        delete = (ImageButton) findViewById(R.id.edit_delete);
        camera_bt = (ImageButton) findViewById(R.id.edit_camera);
        location_bt = (ImageButton) findViewById(R.id.edit_location);
        weather_bt = (ImageButton) findViewById(R.id.edit_weather);
        addBackground = (ImageButton) findViewById(R.id.edit_add_bg);
        note_et = (RichTextEditor) findViewById(R.id.note_et);
        bgSelector = (RecyclerView) findViewById(R.id.bg_selector);
        lengthView = (TextView) findViewById(R.id.text_length);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bgSelector.setLayoutManager(layoutManager);
        bgSelector.setAdapter(adapter);

        back.setOnClickListener(this);
        delete.setOnClickListener(this);
        camera_bt.setOnClickListener(this);
        menuOpen.setOnClickListener(this);
        location_bt.setOnClickListener(this);
        weather_bt.setOnClickListener(this);
        addBackground.setOnClickListener(this);
        bg_ll.setOnClickListener(this);

        adapter.setOnItemClickListener(new BgSelectorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                bgId = position;
                bg_ll.setBackgroundResource(NoteBgResources.getNoteBgImageResource(bgId));
                isLocalModefied = true;
            }
        });

        fontSizeId = (Integer) SetupUtils.getParam(this,Notes.PREFERENCES_FONT_SIZE
                , ResourceParser.DEFAULT_FONT_SIZE);
        isQuickDelete = (Boolean) SetupUtils.getParam(this,Notes.PREFERENCES_QUICK_DELETE, false);
        isShowTextLength = (Boolean) SetupUtils.getParam(this,Notes.PREFERENCES_SHOW_TEXT_LENGTH, false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            note_et.setTextAppearance(TextAppearanceResources.getTextAppearanceResource(fontSizeId));
//        }
        note_et.setTextSize(TextAppearanceResources.getTextSize(fontSizeId));
        note_et.setOnTextChangeListener(new RichTextEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(int count) {
                lengthView.setText(String.valueOf(count));
            }
        });
        if (isShowTextLength){
            lengthView.setVisibility(View.VISIBLE);
        } else {
            lengthView.setVisibility(View.GONE);
        }

    }


    private void initNewNote() {
        getCurrentDateAndTime(System.currentTimeMillis());//设置时间为当前时间
        bg_ll.setBackgroundResource(NoteBgResources.getNoteBgImageResource(bgId));
    }

    private void getDataFromDataBase() {
        Uri uri = ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
        Cursor cursor = getContentResolver().query(uri, NOTE_PROJECTION, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            parentId = cursor.getInt(cursor.getColumnIndex(NoteColumns.PARENT_ID));
            modifiedTime = cursor.getLong(cursor.getColumnIndex(NoteColumns.MODIFIED_DATE));
            bgId = cursor.getInt(cursor.getColumnIndex(NoteColumns.BG_ID));
            alarmDate = cursor.getLong(cursor.getColumnIndex(NoteColumns.ALARM_DATE));
            content = cursor.getString(cursor.getColumnIndex(NoteColumns.CONTENT));
            textLength = cursor.getInt(cursor.getColumnIndex(NoteColumns.TEXT_LENGTH));
            location = cursor.getString(cursor.getColumnIndex(NoteColumns.LOCATION));
            cursor.close();
        }

        getImageInfoFromDB();
    }

    private void getImageInfoFromDB() {
        Cursor cursor = getContentResolver().query(Notes.CONTENT_MEDIA_URI, MEDIA_PROJECTION,
                MediaColumns.NOTE_ID + "=?", new String[]{
                        String.valueOf(noteId)}, null);
        imageMap.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaColumns.IMAGE_PATH));
                    byte[] bmpBlob = cursor.getBlob(cursor.getColumnIndex(MediaColumns.IMAGE_BLOB));
                    int imageIndex = cursor.getInt(cursor.getColumnIndex(MediaColumns.IMAGE_INDEX));
                    Log.e("TAG", "imagePath         " + imagePath);
                    Log.e("TAG", "imageIndex         " + imageIndex);
                    imageMap.put(imageIndex, bmpBlob);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            throw new IllegalArgumentException("未能找到此Id的数据" + noteId);
        }
    }

    private void setNote() {
        getCurrentDateAndTime(modifiedTime);//设置时间为上次创建或修改的时间
        showEditData(content);
        setAlarmText();
        lengthView.setText(String.valueOf(textLength));
        bg_ll.setBackgroundResource(NoteBgResources.getNoteBgImageResource(bgId));
    }

    private void setAlarmText() {
        if (alarmDate > 0){
            clock_tv.setVisibility(View.VISIBLE);
            long time = alarmDate - System.currentTimeMillis();
            if (time > 0){
                clock_tv.setText(timeToString(time));
            } else {
                clock_tv.setText("已过期");
            }
        } else {
            clock_tv.setVisibility(View.GONE);
        }
    }


    protected void showEditData(String content) {
        note_et.clearAllLayout();
        List<String> textList = cutStringByImgTag(content);
        for (int i = 0; i < textList.size(); i++) {
            String text = textList.get(i);
            if (text.contains("<图片>")) {
//                String imagePath = imageMap.get(i);
                byte[] bmpBlob = imageMap.get(i);
                Log.e("TAG", "imageMap " + imageMap.size());
                Log.e("TAG", "index" + i);
                Log.e("TAG","bmpBlob.length"+bmpBlob.length);
//                Log.e("TAG", "imagePath " + imagePath);
                int width = ScreenUtils.getScreenWidth(this);
                int height = ScreenUtils.getScreenHeight(this);
                note_et.measure(0, 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bmpBlob, 0, bmpBlob.length);
//                Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, width, height);
                note_et.addImageViewAtIndex(note_et.getLastIndex(), bitmap, "");
                if (i == textList.size() - 1) {
                    note_et.addEditTextAtIndex(note_et.getLastIndex(), "");
                }
            } else {
                note_et.addEditTextAtIndex(note_et.getLastIndex(), text);
            }
//            note_et.addEditTextAtIndex(i,"");
        }
    }

    public static List<String> cutStringByImgTag(String targetStr) {
        List<String> splitTextList = new ArrayList<>();
        Pattern pattern = Pattern.compile("<图片>");
        Matcher matcher = pattern.matcher(targetStr);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                splitTextList.add(targetStr.substring(lastIndex, matcher.start()));
            }
            splitTextList.add(targetStr.substring(matcher.start(), matcher.end()));
            lastIndex = matcher.end();
        }
        if (lastIndex != targetStr.length()) {
            splitTextList.add(targetStr.substring(lastIndex, targetStr.length()));
        }
        return splitTextList;
    }

    private void getCurrentDateAndTime(long currentTime) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        Date date = new Date(currentTime);
        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date);
        date_tv.setText(dateStr);
        time_tv.setText(timeStr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                if (isWorthSaving()) {
                    saveNote();
                }
                finish();
                break;
            case R.id.menu_open:
                createMenuPopupWindow();
                break;
            case R.id.edit_delete:
                if (!isQuickDelete) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteEditActivity.this);
                    builder.setTitle("删除便签");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setMessage("你确定要删除这条便签吗？");
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    deleteNote();
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                } else {
                    deleteNote();
                }
                break;
            case R.id.edit_camera:
                createPhotoPopupWindow();
                break;
            case R.id.edit_location:
                createLocationPopWin(v);
                if (locationClient.isStarted()) {
                    locationClient.stop();
                }
                locationClient.start();
                break;
            case R.id.edit_weather:
                sendWeatherRequest();
                break;
            case R.id.edit_add_bg:
                bgSelector.setVisibility(View.VISIBLE);
                break;
            case R.id.menu_close:
                menuWindow.dismiss();
                break;
            case R.id.remind_clock:
                setReminder();
                break;
            case R.id.remind_switch:
                if (remindSwitch.isChecked()) {
                    setReminder();
                } else {
                    Toast.makeText(this, "关闭菜单", Toast.LENGTH_SHORT).show();
                    closeAlarm();
                }
                break;
            case R.id.image_share:

                menuWindow.dismiss();
                break;
            case R.id.send_to_desktop:

                menuWindow.dismiss();
                break;
            case R.id.send_to_qq:

                menuWindow.dismiss();
                break;
            default:
                break;
        }
    }

    TextView lastLocationText;
    TextView currLocationText;

    private void createLocationPopWin(View item) {
        View view = LayoutInflater.from(this).inflate(R.layout.location_popup_window,null);
        lastLocationText = (TextView) view.findViewById(R.id.last_location);
        currLocationText = (TextView) view.findViewById(R.id.current_location);
        lastLocationText.setText(location);
//        if (!TextUtils.isEmpty(currentLocation)){
//            currLocationText.setText(currentLocation);
//        }
        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        int[] location = new int[2];
        item.getLocationOnScreen(location);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.showAtLocation(item,Gravity.NO_GRAVITY, (location[0] +
                item.getWidth() / 2) - view.getMeasuredWidth() / 2, location[1]
                - view.getMeasuredHeight() + item.getHeight()/2);
    }


    private void createMenuPopupWindow() {
        final Window window = getWindow();
        params = window.getAttributes();
        params.alpha = 0.5f;
        window.setAttributes(params);
        View view = LayoutInflater.from(this).inflate(R.layout.menu_layout, null);
        menuWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        menuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
        menuWindow.setAnimationStyle(R.style.MenuStyle);
        menuWindow.setFocusable(true);
        menuWindow.setClippingEnabled(false);
        menuWindow.setOutsideTouchable(true);
        menuWindow.showAtLocation(view, Gravity.TOP, 0, 0);
        view.findViewById(R.id.menu_close).setOnClickListener(this);
        view.findViewById(R.id.remind_clock).setOnClickListener(this);
        view.findViewById(R.id.image_share).setOnClickListener(this);
        view.findViewById(R.id.send_to_qq).setOnClickListener(this);
        view.findViewById(R.id.send_to_desktop).setOnClickListener(this);
        remindSwitch = (Switch) view.findViewById(R.id.remind_switch);
        remindSwitch.setOnClickListener(this);
        if (alarmDate != 0){
            remindSwitch.setChecked(true);
        }
    }

    private void setReminder() {
        DateTimePickerDialog dialog = new DateTimePickerDialog(this, System.currentTimeMillis());
        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                setAlarmClock(date,true);
                hasAlarm = true;
                long time = alarmDate - System.currentTimeMillis();
                if (time > 0){
                    Toast.makeText(NoteEditActivity.this, "将在"
                            + timeToString(time) + "提醒您", Toast.LENGTH_SHORT)
                            .show();
                }
                remindSwitch.setChecked(true);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!hasAlarm) {
                    remindSwitch.setChecked(false);
                }
            }
        });
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DialogStyle);
        }
        dialog.show();
    }

    public String timeToString(long time){
        long min = time/60000;
        long hour = min/60;
        long day = hour/24;
        if (day!=0){
            return day + "天"
                    + (hour-day*24) + "时"
                    + (min - hour*60) + "分后";
        } else {
            if (hour != 0){
                return hour + "小时"
                        + (min -(hour * 60)) + "分钟后";
            } else {
                return min + "分钟后";
            }
        }
    }

    private void setAlarmClock(long date, boolean set) {
        alarmDate = date;
        saveNote();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceive.class);
        intent.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI,noteId));
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);
        if (set){
            manager.set(AlarmManager.RTC_WAKEUP, date, pi);
        } else {
            manager.cancel(pi);
        }
        setAlarmText();
    }

    private void closeAlarm() {
        hasAlarm = false;
        setAlarmClock(0,false);
    }

    private void deleteNote() {
        if (!isNewNote) {
//            getContentResolver().delete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), null, null);
//            getContentResolver().delete(Notes.CONTENT_MEDIA_URI,
//                    MediaColumns.NOTE_ID + "=?", new String[]{
//                            String.valueOf(noteId)});
            DataUtils.deleteNote(getContentResolver(), noteId);
            Intent intent = new Intent();
            intent.putExtra("position", position);
            setResult(Notes.RESULT_DELETE, intent);
        }
        finish();
    }

    private void sendWeatherRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(Constants.CITY_URL + city + Constants.KEY)
                            .build();
                    Response response = client.newCall(request).execute();
                    String data = response.body().string();
                    if (!TextUtils.isEmpty(data)) {
                        parseJson(data);
                        Log.e("TAG", data);
                    } else {
                        Log.e("TAG", "获取数据失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJson(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Gson gson = new Gson();
            final Weather weather = gson.fromJson(weatherContent, Weather.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (weather != null && weather.status.equals("ok")) {
                        Toast.makeText(NoteEditActivity.this,
                                "当前温度为"+ weather.now.temperature
                                        + "，天气为" +weather.now.more.info,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createPhotoPopupWindow() {
        final Window window = getWindow();
        params = getWindow().getAttributes();
        params.alpha = 0.5f;
        window.setAttributes(params);
        View view = LayoutInflater.from(this).inflate(R.layout.photo_popup_window, null);

        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
        popupWindow.setAnimationStyle(R.style.DialogStyle);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        view.findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        view.findViewById(R.id.choose_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
            }
        });
    }

    private void openAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, CHOOSE_ALBUM);
    }

    private void openCamera() {
        currentFilePath = Environment.getExternalStorageDirectory().getPath()
                + "/DCIM/Camera"
                + getPhotoFileName();
        File file = new File(currentFilePath);
        if (file.exists()) {
            file.delete();
        }
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
//                    Log.e("TAG", "  take photo bmp  " + bmp.getByteCount());
//                    Log.e("TAG", "  take photo  bmp " + bmp.getWidth() + "       " + bmp.getHeight());
                note_et.insertImage(currentFilePath);

            } else if (requestCode == CHOOSE_ALBUM) {
                if (data == null) {
                    Log.e("TAG", "请求数据失败");
                    return;
                }
                Uri uri = data.getData();
                String path = uri.getPath();
                Log.e("TAG", "  CHOOSE_ALBUM    " + path);
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                note_et.insertImage(path);

            }
        } else {
            Toast.makeText(NoteEditActivity.this, "获取照片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions(){
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
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "必须获取权限", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    requestLocation();
                }
            } else {
                Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void requestLocation() {
        initLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
//        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    private class SimpleLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder location = new StringBuilder();
            city = bdLocation.getCity();
            if (city == null){
                Toast.makeText(NoteEditActivity.this, "请检测网络", Toast.LENGTH_SHORT).show();
                return;
            }
            location.append(bdLocation.getCity())
                    .append(bdLocation.getDistrict())
                    .append(bdLocation.getStreet());
            currLocationText.setText(location);
        }
    }

    @Override
    public void onBackPressed() {
        if (isWorthSaving()) {
            saveNote();
            finish();
        }
        super.onBackPressed();
    }

    private boolean isWorthSaving() {
        String text = getEditData();
        if (isNewNote) {
            return !TextUtils.isEmpty(text);
        } else {
            if (isLocalModefied) {
                return true;
            }
            if (TextUtils.isEmpty(text)) {
                deleteNote();
                return false;
            }
            return !text.equals(content) && !TextUtils.isEmpty(content);
        }
    }

    private void saveNote() {
        if (isNewNote) {
            content = getEditData();
            ContentValues values = new ContentValues();
            values.put(NoteColumns.PARENT_ID,parentId);
            values.put(NoteColumns.CONTENT, content);
            values.put(NoteColumns.CREATED_DATE, System.currentTimeMillis());
            values.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
            values.put(NoteColumns.ALARM_DATE, alarmDate);
            values.put(NoteColumns.BG_ID, bgId);
            values.put(NoteColumns.TEXT_LENGTH, lengthView.getText().toString());
            if (!TextUtils.isEmpty(currentLocation)){
                values.put(NoteColumns.LOCATION,currentLocation);
                Log.e("TAG","location"+currentLocation);
            }
            Uri uri = getContentResolver().insert(Notes.CONTENT_NOTE_URI, values);
            if (uri != null) {
                noteId = Integer.valueOf(uri.getPathSegments().get(1));
                Log.e("NOTE", "           " + noteId);
            }
            for (Integer i : imageMap.keySet()) {
                DataUtils.addImageToDB(getContentResolver(), noteId, i,"", imageMap.get(i));
            }
            setResult(Notes.RESULT_NEW);
        } else {
            content = getEditData();
            ContentValues values = new ContentValues();
            values.put(NoteColumns.CONTENT, content);
            values.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
            values.put(NoteColumns.ALARM_DATE, alarmDate);
            values.put(NoteColumns.BG_ID, bgId);
            values.put(NoteColumns.TEXT_LENGTH, lengthView.getText().toString());
            if (!TextUtils.isEmpty(currentLocation)){
                values.put(NoteColumns.LOCATION,currentLocation);
                Log.e("TAG","location"+currentLocation);
            }
            getContentResolver().update(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), values, null, null);
            Intent intent = new Intent();
            intent.putExtra("position", position);
            setResult(Notes.RESULT_MODIFIED, intent);
        }
//        finish();
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        super.onDestroy();
    }

    private String getEditData() {
        List<RichTextEditor.EditData> editList = note_et.buildEditData();
        if (editList == null) {
            return null;
        }
        imageMap.clear();
        StringBuilder content = new StringBuilder();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<图片>");
                Log.e("TAG","getEditData" + itemData.imageIndex);
                imageMap.put(itemData.imageIndex, ImageUtils.bmpToByte(itemData.bitmap));
            }
        }
        return content.toString();
    }

    private void hideSoftInput() {
        InputMethodManager m = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        m.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (bgSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(bgSelector, ev)) {
            bgSelector.setVisibility(View.GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }
}
