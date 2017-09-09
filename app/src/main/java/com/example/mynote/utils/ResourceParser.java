package com.example.mynote.utils;


import com.example.mynote.R;

public class ResourceParser {
    public static final int WHITE_BG = 0;
    public static final int BLUE_BG = 1;
    public static final int YELLOW_BG = 2;
    public static final int GREEN_BG = 3;
    public static final int RED_BG = 4;
    public static final int BALLOON_BG = 5;
    public static final int LOVE_BG = 6;
    public static final int MOUNTAIN_BG = 7;
    public static final int PAPER_AIRPLANE_BG = 8;
    public static final int STARRY_SKY_BG = 9;

    public static final int BG_DEFAULT = WHITE_BG;

    public static final int TEXT_SMALL       = 0;
    public static final int TEXT_NORMAL      = 1;
    public static final int TEXT_LARGE       = 2;
    public static final int TEXT_SUPER       = 3;

    public static final int DEFAULT_FONT_SIZE = TEXT_NORMAL;

    public static final int SORT_BY_CREATE_DATE = 0;
    public static final int SORT_BY_MODIFIED_DATE = 1;
    public static final int DEFAULT_SORT_WAY = SORT_BY_MODIFIED_DATE;

    public static class NoteBgResources {
        private final static int[] BG_EDIT_RESOURCES = new int[]{
                android.R.color.white,
                R.color.colorBlue,
                R.color.colorYellow,
                R.color.colorGreen,
                R.color.colorRed,
                R.drawable.balloon_bg,
                R.drawable.love_bg,
                R.drawable.mountain_bg,
                R.drawable.paper_airplane_bg,
                R.drawable.starry_sky_bg
        };

        private final static int[] BG_List_RESOURCES = new int[]{
                android.R.color.white,
                R.color.colorBlue,
                R.color.colorYellow,
                R.color.colorGreen,
                R.color.colorRed,
                R.drawable.balloon_list,
                R.drawable.love_list,
                R.drawable.mountain_list,
                R.drawable.paper_airplane_list,
                R.drawable.starry_sky_list
        };

        private final static String[] BG_EDIT_NAME = new String[]{
                "标准",
                "蓝色",
                "黄色",
                "绿色",
                "红色",
                "气球",
                "爱心",
                "山脉",
                "纸飞机",
                "星空",
        };
        public static int getNoteBgImageResource(int id) {
            return BG_EDIT_RESOURCES[id];
        }
        public static int getNoteListBgResource(int id) {
            return BG_List_RESOURCES[id];
        }
        public static String getNoteBgName(int id) {
            return BG_EDIT_NAME[id];
        }
        public static int getBgResourcesCount(){
            return BG_EDIT_RESOURCES.length;
        }
    }

    public static class TextAppearanceResources {
        private final static int [] TEXTAPPEARANCE_RESOURCES = new int [] {
                R.style.TextAppearanceSmall,
                R.style.TextAppearanceNormal,
                R.style.TextAppearanceLarge,
                R.style.TextAppearanceSuper
        };

        private final static int [] TEXTSIZE_RESOURCES = new int [] {
                17,//小
                20,//默认
                26,//大
                33//超大
        };

        public static int getTextAppearanceResource(int id) {
            /**
             * HACKME: Fix bug of store the resource id in shared preference.
             * The id may larger than the length of resources, in this case,
             * return the {@link ResourceParser#BG_DEFAULT_FONT_SIZE}
             */
            if (id >= TEXTAPPEARANCE_RESOURCES.length) {
                return DEFAULT_FONT_SIZE;
            }
            return TEXTAPPEARANCE_RESOURCES[id];
        }

        public static int getTextSize(int id) {
            /**
             * HACKME: Fix bug of store the resource id in shared preference.
             * The id may larger than the length of resources, in this case,
             * return the {@link ResourceParser#BG_DEFAULT_FONT_SIZE}
             */
            if (id >= TEXTSIZE_RESOURCES.length) {
                return DEFAULT_FONT_SIZE;
            }
            return TEXTSIZE_RESOURCES[id];
        }

        public static int getResourcesSize() {
            return TEXTAPPEARANCE_RESOURCES.length;
        }
    }


}
