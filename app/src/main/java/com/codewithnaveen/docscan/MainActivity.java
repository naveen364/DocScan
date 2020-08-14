package com.codewithnaveen.docscan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private Button cameraButton;
    private Button mediaButton;
    private Button add;
    private Bitmap bitmap = null;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();
    private ImageAdapter imageAdapter;
    private Bitmap outBitmap;
    private int count =1;
    private int deg = 90;
    private RecyclerView recyclerView;
    private Parcelable mBundleRecyclerViewState;
    private String mtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        askpermission();
        visible();
    }

    private void init() {
        add = (Button) findViewById(R.id.add);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        imageAdapter = new ImageAdapter(this,bitmapArrayList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void visible(){
        if(bitmapArrayList.size()<1){
            add.setVisibility(View.GONE);
            cameraButton.setVisibility(View.VISIBLE);
            mediaButton.setVisibility(View.VISIBLE);

        }else{
            add.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.GONE);
            mediaButton.setVisibility(View.GONE);
        }
    }

    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

      //  public ScanButtonClickListener() {
       // }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            /*
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(uri,projection,null,null,null);
            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String picture_Path = cursor.getString(column_index_data);
            */
            img_bitmap(uri);
        }
    }

    public void img_bitmap(Uri uri){
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            getContentResolver().delete(uri, null, null);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888,false);
            outBitmap = Bitmap.createBitmap(bitmap1,0,0,bitmap1.getWidth(),bitmap1.getHeight(),matrix,true);
            Bitmap scalebitmap = Bitmap.createScaledBitmap(outBitmap,595,842,true);
            bitmapArrayList.add(scalebitmap);
            imageAdapter.notifyDataSetChanged();
            if (bitmapArrayList!=null){
                add.setVisibility(View.VISIBLE);
                cameraButton.setVisibility(View.GONE);
                mediaButton.setVisibility(View.GONE);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Choose from where you want image");
                        builder.setTitle("Image Choose");
                        builder.setCancelable(false);
                        builder.setPositiveButton("camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startScan(ScanConstants.OPEN_CAMERA);
                                deg = 90;
                                Toast.makeText(MainActivity.this,"positive button", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this,"Cancel button", Toast.LENGTH_SHORT).show();
                                dialogInterface.cancel();
                            }
                        });

                        builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startScan(ScanConstants.OPEN_MEDIA);

                                Toast.makeText(MainActivity.this,"Neutral button", Toast.LENGTH_SHORT).show();
                            }
                        });

                        AlertDialog alert =builder.create();
                        count = bitmapArrayList.size();
                        Toast.makeText(MainActivity.this,"count = "+count, Toast.LENGTH_SHORT).show();
                        alert.show();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBundleRecyclerViewState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        visible();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(mBundleRecyclerViewState);
        visible();
    }


    public void askpermission(){
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe();
    }

    public void createpdf(int count){
        File file = getOutputFile(mtext);
        int temp = 1;
        Canvas canvas;
        if(file != null) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                PdfDocument pdfDocument = new PdfDocument();
                for(int i = 0; i<=count-1; i++){
                    bitmap = bitmapArrayList.get(i);
                    PdfDocument.PageInfo mypageinfo = new PdfDocument.PageInfo.Builder(598,842,temp+i).create();
                    PdfDocument.Page mypage = pdfDocument.startPage(mypageinfo);
                    canvas = mypage.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    DisplayMetrics mertrix = getApplicationContext().getResources().getDisplayMetrics();
                    int w = mertrix.widthPixels;
                    int h = mertrix.heightPixels;

                    Rect frameToDraw = new Rect(0,0,w,h);
                    RectF whereToDraw = new RectF(0,0,w,h);
                    canvas.drawBitmap(bitmap,frameToDraw,whereToDraw,paint);
                    pdfDocument.finishPage(mypage);
                    bitmap.recycle();
                }
                pdfDocument.writeTo(fileOutputStream);
                pdfDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputFile(String mtext){
        File file = new File(Environment.getExternalStorageDirectory(),"docscan");
        boolean isFolderCreated = true;
        String imageFileName;
        if(!file.exists()){
            isFolderCreated = file.mkdir();
        }
        if(isFolderCreated){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            if(mtext!= null){
                imageFileName = mtext;
            }else {
                imageFileName = "PDF_" + timeStamp;
            }
            return new File(file,imageFileName+".pdf");
        }
        else{
            Toast.makeText(this,"Folder is not created ",Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(bitmapArrayList.size()>0) {
                createpdf(bitmapArrayList.size());
                bitmapArrayList.clear();
                Toast.makeText(this, "pdf saved"+bitmapArrayList, Toast.LENGTH_SHORT).show();
                imageAdapter.notifyDataSetChanged();
                visible();
            }
            else{
                Toast.makeText(this, "please Select Image", Toast.LENGTH_SHORT).show();
            }
            return true;

        }

        if(id == R.id.action_Rename){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Rename Pdf");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            builder1.setView(input);
            builder1.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mtext = input.getText().toString();
                }
            });
            builder1.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder1.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
