package com.codewithnaveen.docscan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
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


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private ImageButton cameraButton;
    private ImageButton mediaButton;
    private Button add;
    private Bitmap bitmap = null;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();
    private ImageAdapter imageAdapter;
    private int count =1;
    private RecyclerView recyclerView;
    private Parcelable mBundleRecyclerViewState;
    private String mtext;
    private String save;
    private MenuItem menushare;
    private MenuItem menupdf,createnew;
    private LinearLayout linearLayout;
    private static final String LIST_STATE_KEY = "recycle_layout";
    private boolean isToggled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        askpermission();
        visible();
    }

    private void init() {
        add = findViewById(R.id.add);
        linearLayout = findViewById(R.id.lay);
        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        menupdf = findViewById(R.id.action_settings);
        menushare = findViewById(R.id.action_share);
        recyclerView = findViewById(R.id.recycleview);
        imageAdapter = new ImageAdapter(this,bitmapArrayList);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public void visible(){
        if(imageAdapter.getItemCount()<1){
            add.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            mediaButton.setVisibility(View.VISIBLE);

        }else{
            add.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);
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
            matrix.setRotate(0);
            Bitmap outBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmapArrayList.add(outBitmap);
            imageAdapter.notifyDataSetChanged();
            if (bitmapArrayList!=null){
                add.setVisibility(View.VISIBLE);
                cameraButton.setVisibility(View.GONE);
                mediaButton.setVisibility(View.GONE);
                menushare.setVisible(true);
                menupdf.setVisible(true);
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
                        count = bitmapArrayList.size();
                        if(count == 0){
                            visible();
                        }
                        Toast.makeText(MainActivity.this,"count = "+count, Toast.LENGTH_SHORT).show();
                        alert.show();
                    }
                });
            }
            else {
                menupdf.setVisible(false);
                menushare.setVisible(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    try {
                        bitmap = bitmapArrayList.get(i);
                        Toast.makeText(this,"width is "+bitmap.getWidth(),Toast.LENGTH_SHORT).show();
                    }catch(IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                 //   DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
                    int w =595;//metrics.widthPixels;
                    int h =842;// metrics.heightPixels;
                    PdfDocument.PageInfo mypageinfo = new PdfDocument.PageInfo.Builder(w,h,temp+i).create();
                    PdfDocument.Page mypage = pdfDocument.startPage(mypageinfo);
                    canvas = mypage.getCanvas();
                    float scalew = w/(float)bitmap.getWidth();
                    float scaley = h/(float)bitmap.getHeight();
                    float xTrans = 0.0f;
                    float yTrans = (h - bitmap.getHeight()*scalew)/20.0f;
                    Matrix transform = new Matrix();
                    transform.postTranslate(xTrans,yTrans);
                    transform.preScale(scalew,scaley);
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                   // bitmap = Bitmap.createScaledBitmap(bitmap,w,h,false);
                   // Rect rect = new Rect(0,0,w,h);
                   // RectF rectf = new RectF(0,0,w,h);
                    canvas.drawBitmap(bitmap,transform,paint);
                    pdfDocument.finishPage(mypage);
                }
                pdfDocument.writeTo(fileOutputStream);
                pdfDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public ImageAdapter getImageAdapter() {
        return imageAdapter;
    }

    private void openpdf(String save){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String path =Environment.getExternalStorageDirectory().getAbsolutePath()+"/docscan";
        File file1 = new File(path,save+".pdf");
        Uri uri1 = FileProvider.getUriForFile(MainActivity.this,"com.codewithnaveen.docscan.provider",file1);
        intent.setDataAndType(uri1,"application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void shareFile(String save){
        Intent intent = new Intent(Intent.ACTION_SEND);
        String path =Environment.getExternalStorageDirectory().getAbsolutePath()+"/docscan";
        File file1 = new File(path,save+".pdf");
        Uri uri1 = FileProvider.getUriForFile(MainActivity.this,"com.codewithnaveen.docscan.provider",file1);
        intent.setDataAndType(uri1,"application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM,uri1);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent,"share"));
    }


    private File getOutputFile(String mtext){
        File file = new File(Environment.getExternalStorageDirectory(),"docscan");
        Toast.makeText(this,"file"+file,Toast.LENGTH_SHORT).show();
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
            save = imageFileName;
            return new File(file,imageFileName+".pdf");
        }
        else{
            Toast.makeText(this,"Folder is not created ",Toast.LENGTH_SHORT).show();
            return null;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menupdf = menu.findItem(R.id.action_settings);
        menushare = menu.findItem(R.id.action_Rename);
        createnew = menu.findItem(R.id.createnew);
        if(getImageAdapter() == null || getImageAdapter().getItemCount() == 0){
            menupdf.setVisible(false);
            menushare.setVisible(false);
        }else {
            menupdf.setVisible(true);
            menushare.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if(bitmapArrayList.size()>0) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Your Image is Scanned And Ready");
                builder.setTitle("Open or Share");
                builder.setCancelable(true);
                builder.setPositiveButton(" ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createpdf(bitmapArrayList.size());
                        openpdf(save);
                        Toast.makeText(MainActivity.this, "Open Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNeutralButton(" ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Dialog closed", Toast.LENGTH_SHORT).show();
                        dialogInterface.cancel();
                    }
                });

                builder.setNegativeButton(" ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createpdf(bitmapArrayList.size());
                        shareFile(save);
                    }
                });


                AlertDialog alert = builder.create();
                alert.show();
                Button open = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                open.setBackgroundResource(R.drawable.ic_open);
                Button shared = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                shared.setBackgroundResource(R.drawable.ic_share);
                Button close = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
                close.setBackgroundResource(R.drawable.ic_close);
            }
            else{
                Toast.makeText(this, "please Select Image", Toast.LENGTH_SHORT).show();
            }
            return true;

        }

        if(id == R.id.createnew){
            if(bitmapArrayList.size()>0) {
                bitmapArrayList.clear();
                menupdf.setVisible(false);
                menushare.setVisible(false);
                imageAdapter.notifyDataSetChanged();
                visible();
            }
            return true;
        }

        if(id == R.id.action_Rename){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Rename Pdf");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            input.setPivotX(5);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
