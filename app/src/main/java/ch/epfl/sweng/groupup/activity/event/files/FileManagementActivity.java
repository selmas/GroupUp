package ch.epfl.sweng.groupup.activity.event.files;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.List;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.activity.toolbar.ToolbarActivity;
import ch.epfl.sweng.groupup.lib.Helper;
import ch.epfl.sweng.groupup.lib.Watcher;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.event.Event;

public class FileManagementActivity extends ToolbarActivity implements Watcher {

    private final int COLUMNS = 3;
    private final int ROWS = 4;
    private int columnWidth;
    private int rowHeight;
    private Event event;

    private List<Bitmap> images;

    int imagesAdded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_management);
        super.initializeToolbarActivity();

        Intent intent = getIntent();
        int eventIndex = intent.getIntExtra("EventIndex", -1);
        if (eventIndex >-1) {
            event = Account.shared.getEvents().get(eventIndex);
        }

        event.addWatcher(this);

        findViewById(R.id.add_files).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }});

        findViewById(R.id.update_from_database)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearImages();
                        images = event.getPictures();
                        for(Bitmap b : images){
                            addImageToGrid(b);
                        }
                    }
                });

        final GridLayout grid = findViewById(R.id.image_grid);
        final RelativeLayout container = findViewById(R.id.scroll_view_container);
        ViewTreeObserver vto = container.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                columnWidth = container.getMeasuredWidth() / COLUMNS;
                rowHeight = container.getMeasuredHeight() / ROWS;

            }
        });
        ViewGroup.LayoutParams params = grid.getLayoutParams();
        params.height = rowHeight;
        grid.setLayoutParams(params);

        ViewTreeObserver vto_grid = container.getViewTreeObserver();
        vto_grid.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                images = event.getPictures();
                for(Bitmap bitmap : images){
                    addImageToGrid(bitmap);
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        event.removeWatcher(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        event.removeWatcher(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        event.removeWatcher(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        event.addWatcher(this);

        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();

            if(targetUri == null){
                Helper.showToast(getApplicationContext(),
                        getString(R.string.file_management_toast_error_file_uri),
                        Toast.LENGTH_SHORT);
                return;
            }

            if(imagesAdded % COLUMNS == 0){
                ((GridLayout)findViewById(R.id.image_grid))
                        .setRowCount(imagesAdded / ROWS + 1);
                ViewGroup.LayoutParams params = findViewById(R.id.image_grid).getLayoutParams();
                params.height = Math.round(rowHeight * (imagesAdded / ROWS + 1));
                findViewById(R.id.image_grid)
                        .setLayoutParams(params);
            }

            Bitmap bitmap;
            try {

                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));

            } catch (FileNotFoundException e) {
                Helper.showToast(getApplicationContext(),
                        getString(R.string.file_management_toast_error_file_uri),
                        Toast.LENGTH_SHORT);
                return;
            }

            addImageToGrid(bitmap);
            event.addPicture(Account.shared.getUUID().getOrElse("Default ID"), bitmap);
        }
    }

    private void clearImages(){
        ((GridLayout)findViewById(R.id.image_grid))
                .removeAllViews();
        imagesAdded = 0;
    }

    private void addImageToGrid(Bitmap bitmap){
        ImageView image = new ImageView(this);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = columnWidth;
        layoutParams.height = rowHeight;
        image.setLayoutParams(layoutParams);

        image.setImageBitmap(trimBitmap(bitmap));

        ((GridLayout)findViewById(R.id.image_grid))
                .addView(image, imagesAdded++);
    }

    private Bitmap trimBitmap(Bitmap bitmap) {

        //Scaling bitmap
        Bitmap scaled;

        if(bitmap.getWidth() > bitmap.getHeight()) {
            int nh = (int) (bitmap.getWidth() * (1.0 * rowHeight / bitmap.getHeight()));
            scaled = Bitmap.createScaledBitmap(bitmap, nh, rowHeight, true);
        }else{
            int nh = (int) (bitmap.getHeight() * (1.0 * columnWidth / bitmap.getWidth()));
            scaled = Bitmap.createScaledBitmap(bitmap, columnWidth, nh, true);
        }

        int cutOnSide = (scaled.getWidth() - columnWidth) / 2;
        int cutOnTop = (scaled.getHeight() - rowHeight) / 2;

        return Bitmap.createBitmap(scaled, cutOnSide, cutOnTop,
                columnWidth, rowHeight);
    }

    @Override
    public void notifyWatcher() {
        images = event.getPictures();
        clearImages();
        for(Bitmap bitmap : images){
            addImageToGrid(bitmap);
        }
    }
}
