package com.example.mynote.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mynote.data.Notes;
import com.example.mynote.R;
import com.example.mynote.utils.ResourceParser;
import com.example.mynote.utils.SetupUtils;

import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener {
    RelativeLayout textsizeSetup;
    RelativeLayout sortSetup;
    RelativeLayout smartDelete;
    RelativeLayout cloudRecycle;
    RelativeLayout showTextLength;
    TextView fontSizeText;
    Switch quickDeleteSwitch;
    Switch showLengthSwitch;
    ViewGroup fontSizeSelector;
    RadioGroup sortGroup;
    TextView sortWayText;
    Dialog sortDialog;
    private int fontSizeId;
    private int sortWay;
    private boolean isQuickDelete = false;
    private boolean isShowTextLength = false;

    private static final Map<Integer, Integer> fontSizeBtnsMap = new HashMap<>();

    static {
        fontSizeBtnsMap.put(R.id.ll_font_small, ResourceParser.TEXT_SMALL);
        fontSizeBtnsMap.put(R.id.ll_font_normal, ResourceParser.TEXT_NORMAL);
        fontSizeBtnsMap.put(R.id.ll_font_large, ResourceParser.TEXT_LARGE);
        fontSizeBtnsMap.put(R.id.ll_font_super, ResourceParser.TEXT_SUPER);
    }

    
    private static final Map<Integer, Integer> fontSelectorSelectionMap = new HashMap<>();

    static {
        fontSelectorSelectionMap.put(ResourceParser.TEXT_SMALL, R.id.iv_small_select);
        fontSelectorSelectionMap.put(ResourceParser.TEXT_NORMAL, R.id.iv_medium_select);
        fontSelectorSelectionMap.put(ResourceParser.TEXT_LARGE, R.id.iv_large_select);
        fontSelectorSelectionMap.put(ResourceParser.TEXT_SUPER, R.id.iv_super_select);
    }

    private static final Map<Integer, String> fontSizeTextMap = new HashMap<>();

    static {
        fontSizeTextMap.put(ResourceParser.TEXT_SMALL, "小");
        fontSizeTextMap.put(ResourceParser.TEXT_NORMAL, "默认");
        fontSizeTextMap.put(ResourceParser.TEXT_LARGE, "大");
        fontSizeTextMap.put(ResourceParser.TEXT_SUPER, "超大");
    }

    private static final Map<Integer, Integer> sortWaySelectionMap = new HashMap<>();

    static {
        sortWaySelectionMap.put(ResourceParser.SORT_BY_CREATE_DATE, R.id.rb_create);
        sortWaySelectionMap.put(ResourceParser.SORT_BY_MODIFIED_DATE, R.id.rb_modified);
    }

    private static final Map<Integer, String> sortWayTextMap = new HashMap<>();

    static {
        sortWayTextMap.put(ResourceParser.SORT_BY_CREATE_DATE, "按创建日期");
        sortWayTextMap.put(ResourceParser.SORT_BY_MODIFIED_DATE, "按编辑日期");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        textsizeSetup = (RelativeLayout) findViewById(R.id.setup_text_size);
        sortSetup = (RelativeLayout) findViewById(R.id.setup_sort);
        smartDelete = (RelativeLayout) findViewById(R.id.setup_quick_delete);
        cloudRecycle = (RelativeLayout) findViewById(R.id.cloud_rb);
        showTextLength = (RelativeLayout) findViewById(R.id.show_text_length);
        fontSizeText = (TextView) findViewById(R.id.tv_font_size);
        quickDeleteSwitch = (Switch) findViewById(R.id.sw_quick_delete);
        showLengthSwitch = (Switch) findViewById(R.id.sw_text_length);
        fontSizeSelector = (ViewGroup) findViewById(R.id.font_size_selector);
        sortWayText = (TextView) findViewById(R.id.sort_way_text);
        for (int id : fontSizeBtnsMap.keySet()){
            findViewById(id).setOnClickListener(this);
        }

        showTextLength.setOnClickListener(this);
        textsizeSetup.setOnClickListener(this);
        sortSetup.setOnClickListener(this);
        smartDelete.setOnClickListener(this);
        cloudRecycle.setOnClickListener(this);

        fontSizeId = (Integer) SetupUtils.getParam(this,Notes.PREFERENCES_FONT_SIZE,
                ResourceParser.DEFAULT_FONT_SIZE);
        fontSizeText.setText(fontSizeTextMap.get(fontSizeId));

        sortWay = (Integer) SetupUtils.getParam(this,
                Notes.PREFERENCES_SORT_WAY, ResourceParser.DEFAULT_SORT_WAY);
        sortWayText.setText(sortWayTextMap.get(sortWay));

        isQuickDelete = (Boolean) SetupUtils.getParam(this,
                Notes.PREFERENCES_QUICK_DELETE,false);
        quickDeleteSwitch.setChecked(isQuickDelete);

        isShowTextLength = (Boolean) SetupUtils.getParam(this,
                Notes.PREFERENCES_SHOW_TEXT_LENGTH,false);
        showLengthSwitch.setChecked(isShowTextLength);
    }

    public void back(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (fontSizeBtnsMap.containsKey(v.getId())) {
            findViewById(fontSelectorSelectionMap.get(fontSizeId)).setVisibility(View.GONE);
            fontSizeId = fontSizeBtnsMap.get(v.getId());
            SetupUtils.setParam(this,Notes.PREFERENCES_FONT_SIZE, fontSizeId);
            findViewById(fontSelectorSelectionMap.get(fontSizeId)).setVisibility(View.VISIBLE);
            fontSizeText.setText(fontSizeTextMap.get(fontSizeId));
            fontSizeSelector.setVisibility(View.GONE);
        }
        switch (v.getId()) {
            case R.id.setup_text_size:
                fontSizeSelector.setVisibility(View.VISIBLE);
                findViewById(fontSelectorSelectionMap.get(fontSizeId)).setVisibility(View.VISIBLE);
                break;
            case R.id.setup_sort:
                createSortDialog();
                break;
            case R.id.setup_quick_delete:
                quickDeleteSwitch.setChecked(!quickDeleteSwitch.isChecked());
                SetupUtils.setParam(this,Notes.PREFERENCES_QUICK_DELETE,
                        quickDeleteSwitch.isChecked());
                break;
            case R.id.show_text_length:
                showLengthSwitch.setChecked(!showLengthSwitch.isChecked());
                SetupUtils.setParam(this,Notes.PREFERENCES_SHOW_TEXT_LENGTH,
                        showLengthSwitch.isChecked());
                break;
            case R.id.cloud_rb:
                break;
        }
    }

    private void createSortDialog() {
        sortDialog = new Dialog(this);
        View view  = LayoutInflater.from(this).inflate(R.layout.sort_dialog,null);

        sortGroup = (RadioGroup) view.findViewById(R.id.sort_rg);
        sortGroup.setOnCheckedChangeListener(this);
        sortGroup.check(sortWaySelectionMap.get(sortWay));
        sortDialog.setContentView(view);
        Window window = sortDialog.getWindow();
        if (window != null){
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DialogStyle);
        }
        sortDialog.show();
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDialog.dismiss();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (fontSizeSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(fontSizeSelector, ev)) {
            fontSizeSelector.setVisibility(View.GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int []location = new int[2];
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

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.rb_create){
            SetupUtils.setParam(this,Notes.PREFERENCES_SORT_WAY,
                    ResourceParser.SORT_BY_CREATE_DATE);
            sortWayText.setText(sortWayTextMap.get(ResourceParser.SORT_BY_CREATE_DATE));
        } else if (checkedId == R.id.rb_modified){
            SetupUtils.setParam(this,Notes.PREFERENCES_SORT_WAY,
                    ResourceParser.SORT_BY_MODIFIED_DATE);
            sortWayText.setText(sortWayTextMap.get(ResourceParser.SORT_BY_MODIFIED_DATE));
        }
        sortDialog.dismiss();
    }
}
