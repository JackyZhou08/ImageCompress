package com.jacky.imagecompress;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;


/**
 * Options.inJustDecodeBounds 设置为true，则bitmap不会加载到内存中，获取到的bitmap实例为null，都是可以知道图片的width与height
 *
 * ExifInterface描述多媒体的文件的一些附加信息，例如方向等
 *
 * Matrix
 *
 */
public class MainActivity extends AppCompatActivity {
    private final int requestCode = 0;
    private TextView tv_original_image,tv_compress_image,tv_original_bitmap,tv_compress_bitmap;
    private ImageView iv_compress_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_original_image = (TextView) findViewById(R.id.tv_original_image);
        tv_original_bitmap = (TextView) findViewById(R.id.tv_original_bitmap);
        tv_compress_image = (TextView) findViewById(R.id.tv_compress_image);
        tv_compress_bitmap = (TextView) findViewById(R.id.tv_compress_bitmap);
        iv_compress_image = (ImageView) findViewById(R.id.iv_compress_image);


    }

    public void select(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == this.requestCode && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            Cursor cursor = getContentResolver().query(imageUri, new String[]{MediaStore.Images.Media.DATA},
                    null, null, null);
            cursor.moveToFirst();
            final String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            tv_original_image.setText(new File(imagePath).length()/1024 + "KB");
            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
            tv_original_bitmap.setText(originalBitmap.getByteCount()/1024  + "KB");

//            compressImageFromRGB_565(imagePath);
//            compressImageFromInSampleSize(imagePath);
//            compressIamgeFromMatrix(imagePath);
//            compressImageFromCreateScaledBitmap(imagePath);
            compressImageFromQuality(imagePath);

        }
    }

    /**
     * 质量压缩,实际的bitmap内存不变，都是bytes的length会工具quality的变小而变小，这种情况就比较适合去上传二进制文件
     * 一般图片的上传都需要base64编码然后再上传给服务器的，这种方法就适用
     * @param imagePath
     */
    private void compressImageFromQuality(String imagePath) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
        int quality = 50;
        originalBitmap.compress(Bitmap.CompressFormat.JPEG,quality,byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        iv_compress_image.setImageBitmap(bitmap);
        tv_compress_bitmap.setText(bytes.length/1024  + "KB");


    }

    /**
     * 把宽和高压缩成固定的长度
     * @param imagePath
     */
    private void compressImageFromCreateScaledBitmap(String imagePath) {
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
        originalBitmap = Bitmap.createScaledBitmap(originalBitmap,150,150,true);
        iv_compress_image.setImageBitmap(originalBitmap);
        tv_compress_bitmap.setText(originalBitmap.getByteCount()/1024  + "KB");
    }

    /**
     * Matrix法压缩
     * @param imagePath
     */
    private void compressIamgeFromMatrix(String imagePath) {
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
        Matrix matric = new Matrix();
        matric.setScale(0.5f,0.5f);
        originalBitmap = Bitmap.createBitmap(originalBitmap,0,0,originalBitmap.getWidth(),originalBitmap.getHeight(),matric,true);
        iv_compress_image.setImageBitmap(originalBitmap);
        tv_compress_bitmap.setText(originalBitmap.getByteCount()/1024  + "KB");

    }

    /**
     * 采样率压缩图片，把宽和高按照一定的比例压缩
     * @param imagePath
     */
    private void compressImageFromInSampleSize(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;//宽和高都按1/2压缩
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath,options);
        iv_compress_image.setImageBitmap(originalBitmap);
        tv_compress_bitmap.setText(originalBitmap.getByteCount()/1024  + "KB");
    }

    /**
     * RGB_565压缩图片，把安卓默认的AGB_8888（一个像素占4个字节）的画质改为RGB_565（一个像素占2个字节）画质显示
     * @param imagePath 本地图片路径
     */
    private void compressImageFromRGB_565(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath,options);
        iv_compress_image.setImageBitmap(originalBitmap);
        tv_compress_bitmap.setText(originalBitmap.getByteCount()/1024  + "KB");
    }
}
