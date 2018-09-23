package app.healthfact.craftystudio.healthfact;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.health.HealthStats;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.w3c.dom.Text;

import utils.FireBaseHandler;
import utils.HealthFact;

/**
 * Created by Aisha on 8/31/2017.
 */

public class HealthFactFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    HealthFact healthFact;

    static Context mContext;
    ProgressDialog progressDialog;

    public static HealthFactFragment newInstance(HealthFact healthFact, MainActivity context) {
        mContext = context;
        HealthFactFragment fragment = new HealthFactFragment();
        Bundle args = new Bundle();
        args.putSerializable("HealthFact", healthFact);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.healthFact = (HealthFact) getArguments().getSerializable("HealthFact");
        }

        try {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName(healthFact.getmHealthFactTitle())
                    .putContentId(healthFact.getmHealthFactID())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (healthFact.getObjectType() == 1) {
            View view = inflater.inflate(R.layout.nativead_card_layout, container, false);

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.nativead_container_linearLayout);
            NativeExpressAdView nativeExpressAdView = healthFact.getNativeExpressAdView();

            if (nativeExpressAdView.getParent() != null) {
                ((ViewGroup) nativeExpressAdView.getParent()).removeView(nativeExpressAdView);
            }

            linearLayout.removeAllViews();
            linearLayout.addView(nativeExpressAdView);

            return view;
        }


        View view = inflater.inflate(R.layout.fragment_health_fact, container, false);

        TextView healthTitle = (TextView) view.findViewById(R.id.fragment_title_textview);
        //formatting text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            healthTitle.setText(Html.fromHtml(healthFact.getmHealthFactTitle(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            healthTitle.setText(Html.fromHtml(healthFact.getmHealthFactTitle()));
        }

        TextView healthFull = (TextView) view.findViewById(R.id.fragment_full_description_textview);
        //formatting text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            healthFull.setText(Html.fromHtml(healthFact.getmHealthFactFull(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            healthFull.setText(Html.fromHtml(healthFact.getmHealthFactFull()));
        }


        TextView healthTimeUpload = (TextView) view.findViewById(R.id.fragment_fact_time_textview);
        healthTimeUpload.setText(healthFact.getmHealthFactDate());


        //show image


        try {
            ImageView factimageView1 = (ImageView) view.findViewById(R.id.fragment_fact_image_imageview);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("healthfactImage/" + healthFact.getmHealthFactID() + "/" + "main");

            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade(100)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(factimageView1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Button shareButton = (Button) view.findViewById(R.id.fragment_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                onShareClick();
            }
        });

        final LinearLayout likeButton = (LinearLayout) view.findViewById(R.id.fragment_Like_Linearlayout);

        final TextView likeTextview = (TextView) view.findViewById(R.id.fragment_like_textview);
        likeTextview.setText(healthFact.getmHealthFactLikes() + " Likes");

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                healthFact.setmHealthFactLikes(healthFact.getmHealthFactLikes() + 1);
                likeTextview.setText(healthFact.getmHealthFactLikes() + " Likes");
                onLikeCLick(healthFact.getmHealthFactLikes());


            }
        });


        NativeExpressAdView nativeExpressAdView = healthFact.getNativeExpressAdView();
        if (nativeExpressAdView != null) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.fragment_adcontainer_linearLayout);
            linearLayout.removeAllViews();

            if (nativeExpressAdView.getParent() != null) {
                ((ViewGroup) nativeExpressAdView.getParent()).removeView(nativeExpressAdView);
            }

            linearLayout.addView(nativeExpressAdView);
        }


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onLikeCLick(int likes) {

        FireBaseHandler fireBaseHandler = new FireBaseHandler();

        fireBaseHandler.uploadHealthFactLikes(healthFact.getmHealthFactID(), likes, new FireBaseHandler.OnLikelistener() {
            @Override
            public void onLikeUpload(boolean isSuccessful) {
                if (isSuccessful) {

                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Content Id " + healthFact.getmHealthFactID())
                                .putCustomAttribute("Likes", healthFact.getmHealthFactTitle()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Toast.makeText(mContext, "Thank You! for liking the Health Tips", Toast.LENGTH_SHORT).show();
                } else {
                    // storyLikesText.setText(story.getStoryLikes()+"");

                }
            }
        });
    }

    private void onShareClick() {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://goo.gl/Z1if7V?healthFactID=" + healthFact.getmHealthFactID()))
                .setDynamicLinkDomain("ufn8w.app.goo.gl")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("app.nutrition.craftystudio.fit.active")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(healthFact.getmHealthFactTitle())
                                .setDescription(healthFact.getmHealthFactTag())
                                .setImageUrl(Uri.parse(healthFact.getmHealthImageAddress()))
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("share")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();

                            openShareDialog(shortLink);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideDialog();

                    }
                });

    }


    private void openShareDialog(Uri shortUrl) {

        try {
            Answers.getInstance().logCustom(new CustomEvent("Share link created").putCustomAttribute("Content Id", healthFact.getmHealthFactID())
                    .putCustomAttribute("Shares", healthFact.getmHealthFactTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        hideDialog();

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\nRead Health Tips from Fit and Active");
        startActivity(Intent.createChooser(sharingIntent, "Share Health Tip via"));

    }

    public void showDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait..Creating link");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    public void hideDialog() {
        try {
            progressDialog.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
