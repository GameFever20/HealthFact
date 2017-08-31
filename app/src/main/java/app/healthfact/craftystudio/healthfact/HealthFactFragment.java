package app.healthfact.craftystudio.healthfact;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.health.HealthStats;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import utils.HealthFact;

/**
 * Created by Aisha on 8/31/2017.
 */

public class HealthFactFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    HealthFact healthFact;

    static MainActivity mainActivity;
    static SharedPreferences prefs;


    public static HealthFactFragment newInstance(HealthFact healthFact, MainActivity context) {
        mainActivity = context;
        HealthFactFragment fragment = new HealthFactFragment();
        Bundle args = new Bundle();
        args.putSerializable("HealthFact", healthFact);
        fragment.setArguments(args);
        prefs = mainActivity.getSharedPreferences(
                "app.healthfact.craftystudio.healthfact", Context.MODE_PRIVATE);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.healthFact = (HealthFact) getArguments().getSerializable("HealthFact");
        }

        /*
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(story.getStoryTitle())
                .putContentId(story.getStoryID())
        );
*/

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_fact, container, false);

        TextView healthTitle = (TextView) view.findViewById(R.id.fragment_title_textview);
        healthTitle.setText(healthFact.getmHealthFactTitle());


        TextView healthFull = (TextView) view.findViewById(R.id.fragment_full_description_textview);
        healthFull.setText(healthFact.getmHealthFactFull());

        Button shareButton = (Button) view.findViewById(R.id.fragment_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareClick();
            }
        });

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

    private void onShareClick() {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://goo.gl/2uTbHK?healthFactID=" + healthFact.getmHealthFactID()))
                .setDynamicLinkDomain("ufn8w.app.goo.gl")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("app.healthfact.craftystudio.healthfact")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(healthFact.getmHealthFactTitle())
                                .setDescription(healthFact.getmHealthFactTag())
                                .setImageUrl(Uri.parse("https://firebasestorage.googleapis.com/v0/b/short-story-c4712.appspot.com/o/ssicon.png?alt=media&token=578b3bd8-6ce7-453b-8855-a44c0e16bd78"))
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

                    }
                });

    }


    private void openShareDialog(Uri shortUrl) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\nRead Health Fact");
        startActivity(Intent.createChooser(sharingIntent, "Share Health Fact via"));

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
