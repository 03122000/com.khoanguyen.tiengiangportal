/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import app.tiengiangportal.adapter.AdapterImageList;
import app.tiengiangportal.data.AppConfig;
import app.tiengiangportal.data.Constant;
import app.tiengiangportal.data.DatabaseHandler;
import app.tiengiangportal.data.SharedPref;
import app.tiengiangportal.data.ThisApplication;
import app.tiengiangportal.loader.ApiSinglePlaceLoader;
import app.tiengiangportal.model.Images;
import app.tiengiangportal.model.Place;
import app.tiengiangportal.utils.Callback;
import app.tiengiangportal.utils.Tools;

public class ActivityPlaceDetail extends AppCompatActivity {

    private static final String EXTRA_OBJ = "app.thecity.EXTRA_OBJ";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View transitionImage, Place p) {
        Intent intent = new Intent(activity, ActivityPlaceDetail.class);
        intent.putExtra(EXTRA_OBJ, p);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, EXTRA_OBJ);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    private Place place = null;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private FloatingActionButton fab;
    private View parent_view = null;
    private GoogleMap googleMap;
    private DatabaseHandler db;

    private boolean onProcess = false;
    private ApiSinglePlaceLoader task_loader = null;
    private View lyt_progress;
    private View lyt_no_internet;
    private View lyt_distance;
    private float distance = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        parent_view = findViewById(android.R.id.content);

        if(!imgloader.isInited()) Tools.initImageLoader(this);

        db = new DatabaseHandler(this);
        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_OBJ);

        place = (Place) getIntent().getSerializableExtra(EXTRA_OBJ);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        lyt_no_internet = findViewById(R.id.lyt_no_internet);
        lyt_progress = findViewById(R.id.lyt_progress);
        lyt_distance = findViewById(R.id.lyt_distance);
        imgloader.displayImage(Constant.getURLimgPlace(place.image), (ImageView) findViewById(R.id.image));
        distance = place.distance;

        prepareAds();
        fabToggle();
        setupToolbar(place.name);
        initMap();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (db.isFavoritesExist(place.place_id)) {
                    db.deleteFavorites(place.place_id);
                    Snackbar.make(parent_view, place.name +" "+ getString(R.string.remove_favorite), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", place.name);
                } else {
                    db.addFavorites(place.place_id);
                    Snackbar.make(parent_view, place.name +" "+ getString(R.string.add_favorite), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "ADD", place.name);
                }
                fabToggle();
            }
        });
        lyt_no_internet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlaceData();
            }
        });

        // for system bar in lollipop
        Tools.systemBarLolipop(this);

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View place : "+place.name);
    }


    private void displayData(Place p) {
        ((TextView) findViewById(R.id.address)).setText(p.address);
        ((TextView) findViewById(R.id.phone)).setText(p.phone.equals("-") ? getString(R.string.no_phone_number) : p.phone);
        ((TextView) findViewById(R.id.website)).setText(p.website.equals("-") ? getString(R.string.no_website) : p.website);
        ((TextView) findViewById(R.id.description)).setText(p.description);

        if(distance == -1){
            lyt_distance.setVisibility(View.GONE);
        }else{
            lyt_distance.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.distance)).setText(Tools.getFormatedDistance(distance));
        }

        setImageGallery(db.getListImageByPlaceId(p.place_id));
    }

    // this method name same with android:onClick="clickLayout" at layout xml
    public void clickLayout(View view){
        switch (view.getId()){
            case R.id.lyt_address:
                if(!place.isDraft()) {
                    Uri uri = Uri.parse("http://maps.google.com/maps?q=loc:" + place.lat + "," + place.lng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                break;
            case R.id.lyt_phone:
                if(!place.isDraft() && !place.phone.equals("-")){
                    Tools.dialNumber(this, place.phone);
                }else{
                    Snackbar.make(parent_view, R.string.fail_dial_number, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.lyt_website:
                if(!place.isDraft() && !place.website.equals("-")){
                    Tools.directUrl(this, place.website);
                }else{
                    Snackbar.make(parent_view, R.string.fail_open_website, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setImageGallery(List<Images> images) {
        // add optional image into list
        List<Images> new_images = new ArrayList<>();
        final ArrayList<String> new_images_str = new ArrayList<>();
        new_images.add(new Images(place.place_id, place.image));
        new_images.addAll(images);
        for(Images img : new_images){
            new_images_str.add(img.name);
        }

        RecyclerView galleryRecycler = (RecyclerView) findViewById(R.id.galleryRecycler);
        galleryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        AdapterImageList adapter = new AdapterImageList(new_images);
        galleryRecycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterImageList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, String viewModel, int pos) {
                Intent i = new Intent(ActivityPlaceDetail.this, ActivityFullScreenImage.class);
                i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str);
                startActivity(i);
            }
        });
    }

    private void fabToggle() {
        if (db.isFavoritesExist(place.place_id)) {
            fab.setImageResource(R.drawable.ic_nav_favorites);
        } else {
            fab.setImageResource(R.drawable.ic_nav_favorites_outline);
        }
    }

    private void setupToolbar(String name) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        ((TextView) findViewById(R.id.toolbar_title)).setText(name);

        final CollapsingToolbarLayout collapsing_toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setContentScrimColor(new SharedPref(this).getThemeColorInt());
        ((AppBarLayout) findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (collapsing_toolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsing_toolbar)) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }else if(id == R.id.action_share){
            if(!place.isDraft()) {
                Snackbar.make(parent_view, "Share", Snackbar.LENGTH_SHORT).show();
                Tools.methodShare(ActivityPlaceDetail.this, place);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap(){
        if (googleMap == null) {
            MapFragment mapFragment1 =(MapFragment) getFragmentManager().findFragmentById(R.id.mapPlaces);
            mapFragment1.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap gMap) {
                    googleMap = gMap;
                    if (googleMap == null) {
                        Snackbar.make(parent_view, "Sorry! unable to create maps", Snackbar.LENGTH_SHORT).show();
                    }else {
                        // config map
                        googleMap = Tools.configStaticMap(ActivityPlaceDetail.this, googleMap, place);
                    }
                }
            });
        }

        ((Button) findViewById(R.id.bt_navigate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"OPEN", Toast.LENGTH_LONG).show();
                Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + place.lat + "," + place.lng));
                startActivity(navigation);
            }
        });
        ((Button) findViewById(R.id.bt_view)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
        ((LinearLayout) findViewById(R.id.map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
    }

    private void openPlaceInMap(){
        Intent intent = new Intent(ActivityPlaceDetail.this, ActivityMaps.class);
        intent.putExtra(ActivityMaps.EXTRA_OBJ, place);
        startActivity(intent);
    }

    private void prepareAds(){
        if(AppConfig.ENABLE_ADSENSE && Tools.cekConnection(getApplicationContext())){
            AdView mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }else{
            ((RelativeLayout) findViewById(R.id.banner_layout)).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        if(!imgloader.isInited()) Tools.initImageLoader(getApplicationContext());
        loadPlaceData();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(task_loader!=null)task_loader.cancel(true);
        super.onDestroy();
    }

    // places detail load with lazy scheme
    private void loadPlaceData(){
        place = db.getPlace(place.place_id);
        lyt_no_internet.setVisibility(View.GONE);
        if(place.isDraft()){
            if(Tools.cekConnection(this)){
                requestDetailsPlace(place.place_id);
            }else{
                lyt_no_internet.setVisibility(View.VISIBLE);
            }
        }else{
            displayData(place);
        }
    }

    private void requestDetailsPlace(int place_id){
        if(!onProcess) {
            onProcess = true;
            showProgressbar(true);
            task_loader = new ApiSinglePlaceLoader(place_id, new Callback<Place>() {
                @Override
                public void onSuccess(Place result) {
                    showProgressbar(false);
                    onProcess = false;
                    Place p = db.updatePlace(result);
                    if (p != null) place = p;
                    displayData(place);
                }

                @Override
                public void onError(String result) {
                    showProgressbar(false);
                    onProcess = false;
                    Snackbar.make(parent_view, "Failed load place data", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loadPlaceData();
                        }
                    }).show();
                }
            });
            task_loader.execute("");
        } else {
            Snackbar.make(parent_view, "Task still running", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showProgressbar(boolean show){
        lyt_progress.setVisibility( show ? View.VISIBLE : View.GONE );
    }
}
