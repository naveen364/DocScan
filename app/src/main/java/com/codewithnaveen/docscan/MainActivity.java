package com.codewithnaveen.docscan;

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
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private Button cameraButton;
    private Button mediaButton;
    private Button add;
    private ImageView scannedImageView;
    private Bitmap bitmap = null;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();
    private Bitmap outBitmap;
    private int count =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        add = (Button) findViewById(R.id.add);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
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
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                outBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                bitmapArrayList.add(outBitmap);
                scannedImageView.setImageBitmap(outBitmap);
                if (scannedImageView!=null){
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
                            count = count+1;
                            Toast.makeText(MainActivity.this,"count = "+count, Toast.LENGTH_SHORT).show();
                            alert.show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createpdf(Bitmap bitmap, int count){
        File file = getOutputFile();
        int temp = 1;
      /*  int i=1;
        while(i !=count){
            bitmapArrayList.get(i);
            i=i+1;
        }
        
       */
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

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
                    canvas.drawPaint(paint);
                    canvas.drawBitmap(bitmap,0f,0f,null);
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

    private File getOutputFile(){
        File file = new File(Environment.getExternalStorageDirectory(),"docscan");
        boolean isFolderCreated = true;
        if(!file.exists()){
            isFolderCreated = file.mkdir();
        }
        if(isFolderCreated){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "PDF_"+timeStamp;
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
            createpdf(bitmap,count);
            Toast.makeText(this,"pdf saved",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
