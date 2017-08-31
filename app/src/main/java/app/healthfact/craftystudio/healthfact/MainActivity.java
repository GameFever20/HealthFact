package app.healthfact.craftystudio.healthfact;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.health.HealthStats;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import utils.AppRater;
import utils.FireBaseHandler;
import utils.HealthFact;
import utils.ZoomOutPageTransformer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    ArrayList<HealthFact> mHelthFactList = new ArrayList<>();

    FireBaseHandler fireBaseHandler;

    int adsCount = 0;
    private InterstitialAd mInterstitialAd;

    private boolean pendingInterstitialAd;
    private Handler handler;
    private Runnable runnable;

    boolean isSplashScreen = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);


        //Fabric.with(this, new Crashlytics());

        fireBaseHandler = new FireBaseHandler();
        openDynamicLink();

    }

    public void initializeActivity() {

        setContentView(R.layout.activity_main);
        isSplashScreen = false;


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                HealthFact healthFact = new HealthFact();
                healthFact.setmHealthFactTitle("Water");
                healthFact.setmHealthFactFull("drink water daily");
                fireBaseHandler.uploadHealthFact(healthFact, new FireBaseHandler.OnHealthFactlistener() {
                    @Override
                    public void OnHealthFactlistener(HealthFact healthFact, boolean isSuccessful) {

                    }

                    @Override
                    public void OnHealthFactListlistener(ArrayList<HealthFact> healthFacts, boolean isSuccessful) {

                    }

                    @Override
                    public void onHealthFactUpload(boolean isSuccessful) {

                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mPager = (ViewPager) findViewById(R.id.mainActivity_viewpager);


        initializeViewPager();


        //calling rate now dialog
        // AppRater appRater = new AppRater();
        //appRater.app_launched(MainActivity.this);


        FirebaseMessaging.getInstance().subscribeToTopic("subscribed");


        MobileAds.initialize(this, "ca-app-pub-8455191357100024~7684748984");
        initializeInterstitialAds();

    }

    private void openDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("DeepLink", "onSuccess: " + deepLink);

                            String healthFactID = deepLink.getQueryParameter("healthFactID");
                            //Toast.makeText(MainActivity.this, "Story id " + shortStoryID, Toast.LENGTH_SHORT).show();

                            //download story
                            downloadHealthFact(healthFactID);

                            // downloadNewsArticle(newsArticleID);

                        } else {
                            Log.d("DeepLink", "onSuccess: ");

                            //download story list


                            try {
                                Intent intent = getIntent();
                                String healthFactID = intent.getStringExtra("healthFactID");
                                if (healthFactID == null) {
                                    downloadHealthFactList();
                                } else {
                                    //download story
                                    downloadHealthFact(healthFactID);

                                    //   Toast.makeText(this, "Story id is = "+storyID, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                downloadHealthFactList();
                                e.printStackTrace();
                            }


                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DeepLink", "getDynamicLink:onFailure", e);
                    }
                });
    }


    @Override
    public void onBackPressed() {
        if (!isSplashScreen) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void giveSuggestion() {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"acraftystudio@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion From Short Story User");
        emailIntent.setType("text/plain");

        startActivity(Intent.createChooser(emailIntent, "Send mail From..."));

    }

    private void rateUs() {

        MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=app.craftystudio.vocabulary.dailyeditorial&hl=en")));

    }

    public void downloadHealthFact(String healthFactUID) {

        fireBaseHandler.downloadHealthFact(healthFactUID, new FireBaseHandler.OnHealthFactlistener() {
            @Override
            public void OnHealthFactlistener(HealthFact healthFact, boolean isSuccessful) {


                if (isSplashScreen) {
                    initializeActivity();
                }

                if (isSuccessful) {
                    mHelthFactList.add(healthFact);
                    mPagerAdapter.notifyDataSetChanged();
                } else {
                    openConnectionFailureDialog();
                }
                downloadHealthFactList();
            }

            @Override
            public void OnHealthFactListlistener(ArrayList<HealthFact> healthFacts, boolean isSuccessful) {

            }

            @Override
            public void onHealthFactUpload(boolean isSuccessful) {

            }
        });

    }

    public void downloadMoreHealthFactList() {

        fireBaseHandler.downloadHealthFactList(5, mHelthFactList.get(mHelthFactList.size() - 1).getmHealthFactID(), new FireBaseHandler.OnHealthFactlistener() {
            @Override
            public void OnHealthFactlistener(HealthFact healthFact, boolean isSuccessful) {

            }

            @Override
            public void OnHealthFactListlistener(ArrayList<HealthFact> healthFacts, boolean isSuccessful) {

                if (isSuccessful) {

                    for (HealthFact healthFact : healthFacts) {
                        MainActivity.this.mHelthFactList.add(healthFact);
                    }

                    mPagerAdapter.notifyDataSetChanged();

                } else {
                     openConnectionFailureDialog();
                }


            }

            @Override
            public void onHealthFactUpload(boolean isSuccessful) {

            }
        });

    }

    public void downloadHealthFactList() {

        fireBaseHandler.downloadHealthFactList(5, new FireBaseHandler.OnHealthFactlistener() {
            @Override
            public void OnHealthFactlistener(HealthFact healthFact, boolean isSuccessful) {

            }

            @Override
            public void OnHealthFactListlistener(ArrayList<HealthFact> healthFactArrayList, boolean isSuccessful) {

                if (isSplashScreen) {
                    initializeActivity();
                }
                if (isSuccessful) {

                    for (HealthFact healthFact : healthFactArrayList) {
                        MainActivity.this.mHelthFactList.add(healthFact);
                    }

                    mPagerAdapter.notifyDataSetChanged();

                } else {
                    openConnectionFailureDialog();
                }
            }

            @Override
            public void onHealthFactUpload(boolean isSuccessful) {

            }
        });

    }

    public void openConnectionFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Connection Failure!");
        builder.setMessage("Something went wrong!")
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                        recreate();

                        // FIRE ZE MISSILES!
                    }
                });

        // Create the AlertDialog object and return it
        builder.create();
        builder.show();


    }

    private void initializeViewPager() {

        // Instantiate a ViewPager and a PagerAdapter.

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //change to zoom
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                checkInterstitialAds();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void initializeInterstitialAds() {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAdTimer(45000);


        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");

                // Answers.getInstance().logCustom(new CustomEvent("Ad failed to load").putCustomAttribute("Failed index",errorCode));

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.i("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.i("Ads", "onAdClosed");
                adsCount = 0;
                interstitialAdTimer(45000);

                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }


        });

    }

    public void interstitialAdTimer(long waitTill) {
        pendingInterstitialAd = false;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                pendingInterstitialAd = true;
            }
        };


        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, waitTill);


    }

    private void checkInterstitialAds() {

        if (adsCount > 2 && pendingInterstitialAd) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            adsCount++;
            //getting more stories
            if (position == mHelthFactList.size() - 2) {
                downloadMoreHealthFactList();
            }

            return HealthFactFragment.newInstance(mHelthFactList.get(position), MainActivity.this);
        }

        @Override
        public int getCount() {
            return mHelthFactList.size();
        }
    }


}
