package id.ihsan.bakingapp;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import id.ihsan.bakingapp.models.Recipe;
import id.ihsan.bakingapp.models.Step;

import static id.ihsan.bakingapp.RecipeActivity.SELECTED_INDEX;
import static id.ihsan.bakingapp.RecipeActivity.SELECTED_RECIPES;
import static id.ihsan.bakingapp.RecipeActivity.SELECTED_STEPS;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.04.09
 */
public class RecipeStepDetailFragment extends Fragment {

    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private BandwidthMeter bandwidthMeter;
    private ArrayList<Step> steps = new ArrayList<>();
    private int selectedIndex;
    private Handler mainHandler;
    ArrayList<Recipe> recipe;
    String recipeName;

    private long position;
    private ListItemClickListener itemClickListener;

    public interface ListItemClickListener {
        void onListItemClick(List<Step> allSteps, int Index, String recipeName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView;
        mainHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter();

        itemClickListener = (RecipeDetailActivity) getActivity();

        recipe = new ArrayList<>();

        if (savedInstanceState != null) {
            steps = savedInstanceState.getParcelableArrayList(SELECTED_STEPS);
            selectedIndex = savedInstanceState.getInt(SELECTED_INDEX);
            recipeName = savedInstanceState.getString("Title");
            position = savedInstanceState.getLong("position");

        } else {
            steps = getArguments().getParcelableArrayList(SELECTED_STEPS);
            if (steps != null) {
                steps = getArguments().getParcelableArrayList(SELECTED_STEPS);
                selectedIndex = getArguments().getInt(SELECTED_INDEX);
                recipeName = getArguments().getString("Title");
            } else {
                recipe = getArguments().getParcelableArrayList(SELECTED_RECIPES);
                //casting List to ArrayList
                steps = (ArrayList<Step>) recipe.get(0).getSteps();
                selectedIndex = 0;
            }
        }

        View rootView = inflater.inflate(R.layout.recipe_step_detail_fragment_body_part, container, false);
        textView = (TextView) rootView.findViewById(R.id.recipe_step_detail_text);
        textView.setText(steps.get(selectedIndex).getDescription());
        textView.setVisibility(View.VISIBLE);

        ImageView thumbImage = (ImageView) rootView.findViewById(R.id.thumbImage);
        simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.playerView);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        String videoURL = steps.get(selectedIndex).getVideoURL();

        if (rootView.findViewWithTag("sw600dp-port-recipe_step_detail") != null) {
            recipeName = ((RecipeDetailActivity) getActivity()).recipeName;
            ((RecipeDetailActivity) getActivity()).getSupportActionBar().setTitle(recipeName);
        }

        if (!videoURL.isEmpty()) {
            if (rootView.findViewWithTag("sw600dp-land-recipe_step_detail") != null) {
                getActivity().findViewById(R.id.fragment_container2).setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            } else if (isInLandscapeMode(getContext())) {
                textView.setVisibility(View.GONE);
            }
            thumbImage.setVisibility(View.GONE);
        } else {
            simpleExoPlayerView.setVisibility(View.GONE);
            player = null;

            String imageUrl = steps.get(selectedIndex).getThumbnailURL();
            Uri builtUri = Uri.parse(imageUrl).buildUpon().build();
            Glide.with(getActivity()).load(imageUrl).crossFade().into(thumbImage);
        }

        Button mPrevStep = (Button) rootView.findViewById(R.id.previousStep);
        Button mNextstep = (Button) rootView.findViewById(R.id.nextStep);

        mPrevStep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (steps.get(selectedIndex).getId() > 0) {
                    if (player != null) {
                        player.stop();
                    }
                    itemClickListener.onListItemClick(steps, steps.get(selectedIndex).getId() - 1, recipeName);
                } else {
                    Toast.makeText(getActivity(), "You already are in the First step of the recipe", Toast.LENGTH_SHORT).show();

                }
            }
        });

        mNextstep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                int lastIndex = steps.size() - 1;
                if (steps.get(selectedIndex).getId() < steps.get(lastIndex).getId()) {
                    if (player != null) {
                        player.stop();
                    }
                    itemClickListener.onListItemClick(steps, steps.get(selectedIndex).getId() + 1, recipeName);
                } else {
                    Toast.makeText(getContext(), "You already are in the Last step of the recipe", Toast.LENGTH_SHORT).show();

                }
            }
        });


        return rootView;
    }

    private void initializePlayer(Uri mediaUri) {
        if (player == null) {
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();

            player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            simpleExoPlayerView.setPlayer(player);

            String userAgent = Util.getUserAgent(getContext(), "Baking App");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
            if (position != C.TIME_UNSET) player.seekTo(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle currentState) {
        super.onSaveInstanceState(currentState);
        currentState.putParcelableArrayList(SELECTED_STEPS, steps);
        currentState.putInt(SELECTED_INDEX, selectedIndex);
        currentState.putString("Title", recipeName);
        currentState.putLong("position", position);
    }

    public boolean isInLandscapeMode(Context context) {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void initializeExoplayer() {
        String videoURL = steps.get(selectedIndex).getVideoURL();
        if (!videoURL.isEmpty()) {
            initializePlayer(Uri.parse(steps.get(selectedIndex).getVideoURL()));
        }
    }

    public void releaseExoplayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializeExoplayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            initializeExoplayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releaseExoplayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            position = player.getCurrentPosition();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releaseExoplayer();
        }
    }
}
