package app.healthfact.craftystudio.healthfact;

import android.animation.ObjectAnimator;
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
import android.support.v4.content.ContextCompat;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;

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

    ProgressBar progBar;
    private com.facebook.ads.InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.splash_main);

        //progressbar
        progBar = (ProgressBar) findViewById(R.id.progressBar3);
        fireBaseHandler = new FireBaseHandler();

        openDynamicLink();

    }

    public void initializeActivity() {

        progBar.clearAnimation();
        progBar.setVisibility(View.GONE);

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
        AppRater appRater = new AppRater();
        appRater.app_launched(MainActivity.this);


        FirebaseMessaging.getInstance().subscribeToTopic("subscribed");


       // MobileAds.initialize(this, "ca-app-pub-8455191357100024~7684748984");
       // initializeInterstitialAds();
        initializeFacebookads();
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

                            Answers.getInstance().logCustom(new CustomEvent("Via Dynamic link")
                                    .putCustomAttribute("Fact id", healthFactID));


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

                                    Answers.getInstance().logCustom(new CustomEvent("Via Push notification")
                                            .putCustomAttribute("Fact id", healthFactID));
                                    //   Toast.makeText(this, "Story id is = "+storyID, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {

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
                        downloadHealthFactList();
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

        if (id == R.id.nav_rate) {
            // Handle the camera action
            onRateUs();
        } else if (id == R.id.nav_suggestion) {
            giveSuggestion();



        } else if (id == R.id.nav_share) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");

            //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=app.nutrition.craftystudio.fit.active" +
                            "\n\n Read Health Fact Daily \n Download it now \n ");
            startActivity(Intent.createChooser(sharingIntent, "Share Fit and Active via"));


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initializeFacebookads() {
        interstitialAd = new com.facebook.ads.InterstitialAd(this, "1466375360115237_1466377923448314");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {

                interstitialAd.loadAd();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
             //   Toast.makeText(MainActivity.this, "Error msg " + adError.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        interstitialAd.loadAd();
    }

    private void showFacebookAds() {

    }

    private void giveSuggestion() {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"acraftystudio@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion For " + getResources().getString(R.string.app_name));
        emailIntent.setType("text/plain");

        startActivity(Intent.createChooser(emailIntent, "Send mail From..."));

    }

    private void onRateUs() {
        try {
            String link = "https://play.google.com/store/apps/details?id=" + "app.nutrition.craftystudio.fit.active";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {

        }
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
        mInterstitialAd.setAdUnitId("ca-app-pub-8455191357100024/8869838145");
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

                Answers.getInstance().logCustom(new CustomEvent("Ad failed to load").putCustomAttribute("Failed index", errorCode));

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
         /*   if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }*/

         if (interstitialAd.isAdLoaded()){
             interstitialAd.show();
         }else {
             interstitialAd.loadAd();
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
