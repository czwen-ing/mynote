package com.example.mynote.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {
    public static Bitmap imageZoom(Bitmap bitMap, double maxSize) {
        if (bitMap != null) {
            //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            //将字节换成KB
            double mid = b.length / 1024;
            //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
            if (mid > maxSize) {
                //获取bitmap大小 是允许最大大小的多少倍
                double i = mid / maxSize;
                //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
                bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
                        bitMap.getHeight() / Math.sqrt(i));
                Log.e("TAG", "           " + bitMap.getByteCount());
            }
        }
        return bitMap;
    }

    public static Bitmap zoomImage(Bitmap bmp, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bmp.getWidth();
        float height = bmp.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    public static Bitmap scaleBitmapInSameSize(Bitmap bitmap, int viewWidth){
        // 获取这个图片的宽和高
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        float scaleSize = 1;
        // 计算缩放率
        scaleSize= ((float) viewWidth) / width;
        // 缩放图片动作
        matrix.postScale(scaleSize, scaleSize);
        return Bitmap.createBitmap(bitmap, 0, 0, (int) width,
                (int) height, matrix, true);
    }

    /**
     * 根据Uri获取图片文件的绝对路径
     */
    public static String getImageFilePath(Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }

        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @return
     */

    public static Bitmap getSmallBitmap(String filePath, int newWidth, int newHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);


        // Calculate inSampleSize

        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);


        // Decode bitmap with inSampleSize set

        options.inJustDecodeBounds = false;


        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        Bitmap newBitmap = compressImage(bitmap, 500);

        if (bitmap != null) {

            bitmap.recycle();

        }

        return newBitmap;

    }

    /**
     * 图片压缩处理，size参数为压缩比，比如size为2，则压缩为1/4
     **/

    public static Bitmap compressBitmap(String path, byte[] data, Context context, Uri uri, int size, boolean width) {

        BitmapFactory.Options options = null;

        if (size > 0) {

            BitmapFactory.Options info = new BitmapFactory.Options();

            /**如果设置true的时候，decode时候Bitmap返回的为数据将空*/

            info.inJustDecodeBounds = false;

            decodeBitmap(path, data, context, uri, info);

            int dim = info.outWidth;

            if (!width) dim = Math.max(dim, info.outHeight);

            options = new BitmapFactory.Options();

            /**把图片宽高读取放在Options里*/

            options.inSampleSize = size;

        }

        Bitmap bm = null;

        try {

            bm = decodeBitmap(path, data, context, uri, options);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return bm;

    }

    /**
     * 把byte数据解析成图片
     */

    private static Bitmap decodeBitmap(String path, byte[] data, Context context, Uri uri, BitmapFactory.Options options) {

        Bitmap result = null;

        if (path != null) {

            result = BitmapFactory.decodeFile(path, options);

        } else if (data != null) {

            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        } else if (uri != null) {

            ContentResolver cr = context.getContentResolver();

            InputStream inputStream = null;

            try {

                inputStream = cr.openInputStream(uri);

                result = BitmapFactory.decodeStream(inputStream, null, options);

                inputStream.close();

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        return result;

    }

    /**
     * 质量压缩
     *
     * @param image
     * @param maxSize
     */

    public static Bitmap compressImage(Bitmap image, int maxSize) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // scale

        int options = 80;

        // Store the bitmap into output stream(no compress)

        image.compress(Bitmap.CompressFormat.JPEG, options, os);

        // Compress by loop

        while (os.toByteArray().length / 1024 > maxSize) {

            // Clean up os

            os.reset();

            // interval 10

            options -= 10;

            image.compress(Bitmap.CompressFormat.JPEG, options, os);

        }


        Bitmap bitmap = null;

        byte[] b = os.toByteArray();

        if (b.length != 0) {

            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

        }

        return bitmap;

    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static int calculateInSampleSize(BitmapFactory.Options options,

                                            int reqWidth, int reqHeight) {

        // Raw height and width of image

        final int height = options.outHeight;

        final int width = options.outWidth;

        int inSampleSize = 1;


        if (height > reqHeight || width > reqWidth) {


            // Calculate ratios of height and width to requested height and

            // width

            final int heightRatio = Math.round((float) height / (float) reqHeight);

            final int widthRatio = Math.round((float) width / (float) reqWidth);


            // Choose the smallest ratio as inSampleSize value, this will

            // guarantee

            // a final image with both dimensions larger than or equal to the

            // requested height and width.

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

        }


        return inSampleSize;

    }

    public static byte[] bmpToByte(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        return os.toByteArray();
    }
}
