package com.example.testingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.testingapp.Adapter.ViewPagerAdapter;
import com.example.testingapp.Interface.EditImageFragmentListener;
import com.example.testingapp.Interface.FiltersListFragmentListener;
import com.example.testingapp.Utils.BitmapUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements FiltersListFragmentListener, EditImageFragmentListener {

    public static final String pictureName = "flash.jpg";
    public static final int permission_pick_image = 1000;

    ImageView img_preview;
    TabLayout tabLayout;
    ViewPager viewPager;
    CoordinatorLayout coordinatorLayout;

    Bitmap originalBitmap, filteredBitmap, finalBitmap;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float constrantFinal = 1.0f;

    //load native image filters lib
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Instagram Filter");


        //view
        img_preview = (ImageView) findViewById(R.id.image_preview);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);


        loadImage();
        setupViewPager(viewPager);


        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
//        this.viewPager = viewPager;
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, "FILTERS");
        adapter.addFragment(editImageFragment, "EDIT");

        viewPager.setAdapter(adapter);
    }


    private void loadImage() {

        originalBitmap = BitmapUtils.getBitmapFromAssets(this, pictureName, 300, 300);

        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        img_preview = (ImageView) findViewById(R.id.image_preview);

        img_preview.setImageBitmap(originalBitmap);

    }


    @Override
    public void onBrightnessChanged (int brightness) {

        brightnessFinal = brightness;
        Filter myFilter= new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onSaturationChanged(float saturation) {

        saturationFinal = saturation;
        Filter myFilter= new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onConstrantChanged(float constrant) {

        constrantFinal = constrant;
        Filter myFilter= new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(constrant));
        img_preview.setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888,true)));

    }

    @Override
    public void onEditStarted() {

    }

        @Override
        public void onEditCompleted () {

            Bitmap bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);

            Filter myFilter = new Filter();
            myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
            myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
            myFilter.addSubFilter(new ContrastSubFilter(constrantFinal));

            finalBitmap = myFilter.processFilter(bitmap);

        }

        @Override
        public void onFilterSelected (Filter filter){
            restControl();
            filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            img_preview.setImageBitmap(filter.processFilter(filteredBitmap));
            finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);


        }

        private void restControl () {
            if (editImageFragment != null)
                editImageFragment.restControls();
            brightnessFinal = 0;
            saturationFinal = 1.0f;
            constrantFinal = 1.0f;


        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            int id = item.getItemId();
            if (id == R.id.action_open) {
                openImageFromGallery();
                return true;

            }
            if (id == R.id.action_save) {
                saveImageFromGallery();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void saveImageFromGallery () {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                try {
                                    final String path = BitmapUtils.insertImage(getContentResolver(),
                                            finalBitmap,
                                            System.currentTimeMillis() + "_profile.jpg"
                                            , null);

                                    if (!TextUtils.isEmpty(path)) {
                                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                "Image saced to gallery",
                                                Snackbar.LENGTH_LONG)
                                                .setAction("OPEN", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        openImage(path);
                                                    }
                                                });
                                        snackbar.show();
                                    } else {
                                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                "Unable to save image",
                                                Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {

                                Toast.makeText(MainActivity.this, "permisson denied", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    });
        }

        private void openImage (String path){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(path), "image/*");
            startActivity(intent);
        }

        private void openImageFromGallery () {

            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {

                            if (report.areAllPermissionsGranted()) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, permission_pick_image);
                            } else {

                                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            token.continuePermissionRequest();
                        }
                    });

        }


        //add super. may be error
        @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK && requestCode == permission_pick_image) {
                Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

                originalBitmap.recycle();
                finalBitmap.recycle();
                filteredBitmap.recycle();


                originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                img_preview.setImageBitmap(originalBitmap);

                bitmap.recycle();

                // render selected img thumbnail
                filtersListFragment.displayThumbnail(originalBitmap);
            }
        }
    }

