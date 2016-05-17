package rohin.a121pdf.com.pdfreadertask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ScaleGestureDetector SGD ;
    private static final int REQUEST_PICK_FILE = 1;
    private TextView filePath;
    private ImageView print;
    private Button Browse, Next, Prev;
    private File selectedFile;
    String fileextension;
    private int currentpage=0;
    private float scale = 1f;
    Matrix m = new Matrix();
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    int pageCount;
    int index = 0;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filePath = (TextView) findViewById(R.id.file_path);
        print = (ImageView)findViewById(R.id.imageView);
        Browse = (Button)findViewById(R.id.browse);
        Next = (Button)findViewById(R.id.next);
        Prev = (Button)findViewById(R.id.previous);
        SGD = new ScaleGestureDetector(this,new ScaleListener());
        Browse.setOnClickListener(this);
        Next.setOnClickListener(this);
        Prev.setOnClickListener(this);
       Next.setVisibility(View.GONE);
       Prev.setVisibility(View.GONE);
       print.setVisibility(View.GONE);

       print.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
           @Override
           public void onSwipeLeft() {
               // Whatever
               if (currentpage+1 < pageCount) {
                   Toast.makeText(MainActivity.this,"You Are In LastPage", Toast.LENGTH_SHORT).show();

               }else {
                   showPage(mCurrentPage.getIndex() + 1);
               }
           }

           @Override
           public void onSwipeRight() {

               if (mCurrentPage.getIndex() != 0 ) {
                   showPage(mCurrentPage.getIndex() - 1);
               }else {
                   Toast.makeText(MainActivity.this,"You Are In FirstPage", Toast.LENGTH_SHORT).show();
               }
           }
       });

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.browse:

                Intent intent = new Intent(this, FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);

                break;
           case R.id.next:
               showPage(mCurrentPage.getIndex() + 1);

                break;
            case R.id.previous:

                showPage(mCurrentPage.getIndex() - 1);
                break;
            default:
                Log.e("Task","Task");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {

            switch(requestCode) {
                case REQUEST_PICK_FILE:
                    if(data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        fileextension=selectedFile.getAbsolutePath();
                        filePath.setText(fileextension);
                        String fileext=fileextension.substring(fileextension.lastIndexOf(".")+1);

                        if (fileext.equalsIgnoreCase("pdf")) {
                            try {
                                openRenderer(getApplicationContext());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            showPage(index);
                        }else {
                            Toast.makeText(MainActivity.this,"Plz Select PDF File", Toast.LENGTH_SHORT).show();
                            Next.setVisibility(View.GONE);
                            Prev.setVisibility(View.GONE);
                            print.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
        }
    }

     //Open Renderer
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        // This is the PdfRenderer we use to render the PDF.
        mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(selectedFile.getAbsoluteFile(),ParcelFileDescriptor.MODE_READ_ONLY));
    }

    //Close Renderer
    private void closeRenderer() throws IOException {


            if (null != mCurrentPage) {
                mCurrentPage.close();
            }
            if (mPdfRenderer != null)
                mPdfRenderer.close();
        }

    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        int pageCount=mPdfRenderer.getPageCount();
        mCurrentPage = mPdfRenderer.openPage(index);
        Prev.setEnabled(0 != mCurrentPage.getIndex());
        Next.setEnabled(currentpage +1 < pageCount );
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        print.setVisibility(View.VISIBLE);
        print.setImageBitmap(bitmap);
        Next.setVisibility(View.VISIBLE);
        Prev.setVisibility(View.VISIBLE);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            m.setScale(scale, scale);
            print.setImageMatrix(m);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }

    /* public void pdfrender(){
        try {
            int REQ_WIDTH = print.getWidth();
            int REQ_HEIGHT = print.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH,REQ_HEIGHT,Bitmap.Config.ARGB_4444);
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(selectedFile.getAbsoluteFile(),ParcelFileDescriptor.MODE_READ_ONLY));

            if(currentpage < 0) {
                currentpage = 0;
            }
            else if(currentpage > renderer.getPageCount()){
                currentpage = renderer.getPageCount() - 1;
            }

            int pageCount = renderer.getPageCount();
            Prev.setEnabled(0!=currentpage);
            Next.setEnabled(currentpage +1 < pageCount );

            m = print.getImageMatrix();
            Rect  rect =  new Rect( 0, 0 ,REQ_WIDTH,REQ_HEIGHT);
            renderer.openPage(currentpage).render(bitmap,rect,m,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            print.setImageMatrix(m);
            print.setImageBitmap(bitmap);
            print.invalidate();
            Next.setVisibility(View.VISIBLE);
            Prev.setVisibility(View.VISIBLE);

        }catch (Exception e){
            e.printStackTrace();
        }

    }*/
}
