package rohin.a121pdf.com.pdfreadertask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private GestureDetector gestureDetector;
    private static final int REQUEST_PICK_FILE = 1;
    private TextView filePath;
    private ImageView print;
    private Button Browse,next,previous;
    private File selectedFile;
    private int currentpage=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filePath = (TextView) findViewById(R.id.file_path);
        print = (ImageView)findViewById(R.id.imageView);
        next = (Button)findViewById(R.id.next);
        previous = (Button)findViewById(R.id.previous);
        Browse = (Button)findViewById(R.id.browse);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        Browse.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.browse:
                Intent intent = new Intent(this, FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);
                break;
           case R.id.next:
                currentpage++;
                pdfrender();
                break;
            case R.id.previous:
                currentpage--;
                pdfrender();
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
                        filePath.setText(selectedFile.getPath());
                        pdfrender();
                    }
                    break;
            }
        }
    }
    public void pdfrender(){
        try {
            int REQ_WIDTH = print.getWidth();
            int REQ_HEIGHT = print.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH,REQ_HEIGHT,Bitmap.Config.ARGB_4444);
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(selectedFile,ParcelFileDescriptor.MODE_READ_ONLY));

            if(currentpage < 0) {
                currentpage = 0;
            }
            else if(currentpage > renderer.getPageCount()){
                currentpage = renderer.getPageCount() - 1;
            }

            Toast.makeText(this,"Pages = "+ currentpage ,Toast.LENGTH_LONG).show();
            Matrix m = print.getImageMatrix();
            Rect  rect =  new Rect( 0, 0 ,REQ_WIDTH,REQ_HEIGHT);
            renderer.openPage(currentpage).render(bitmap,rect,m,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            print.setImageMatrix(m);
            print.setImageBitmap(bitmap);
            print.invalidate();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
