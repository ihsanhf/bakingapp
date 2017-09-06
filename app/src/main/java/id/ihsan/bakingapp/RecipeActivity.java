package id.ihsan.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.core.deps.guava.base.Strings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import id.ihsan.bakingapp.adapters.RecipeAdapter;
import id.ihsan.bakingapp.helpers.SimpleIdlingResource;
import id.ihsan.bakingapp.helpers.SpacesItemDecoration;
import id.ihsan.bakingapp.models.Recipe;
import id.ihsan.bakingapp.networks.RestClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.04.09
 */
public class RecipeActivity extends AppCompatActivity implements RecipeAdapter.ListItemClickListener {

    public static final String ALL_RECIPES = "All_Recipes";
    public static final String SELECTED_RECIPES = "Selected_Recipes";
    public static final String SELECTED_STEPS = "Selected_Steps";
    public static final String SELECTED_INDEX = "Selected_Index";

    @Nullable
    private SimpleIdlingResource mIdlingResource;

    /**
     * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    private Toolbar toolbar;
    private RecyclerView listRecipe;

    private RecipeAdapter recipesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        findViews();
        initViews();
        setupToolbar();

        mIdlingResource = (SimpleIdlingResource) getIdlingResource();
        mIdlingResource.setIdleState(false);
        retrieveRecipes();
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listRecipe = (RecyclerView) findViewById(R.id.recipe_recycler);
    }

    private void initViews() {
        int column = 4;
        if (listRecipe.getTag() != null && listRecipe.getTag().equals("phone-port")) {
            column = 1;
        }
        recipesAdapter = new RecipeAdapter(this, RecipeActivity.this);
        listRecipe.setAdapter(recipesAdapter);

        GridLayoutManager mLayoutManager = new GridLayoutManager(RecipeActivity.this, column);
        listRecipe.setLayoutManager(mLayoutManager);
        listRecipe.addItemDecoration(new SpacesItemDecoration(column, 16, true));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Baking");
        }
    }

    private void retrieveRecipes() {
        RestClient.ApiService apiService = RestClient.getClient();
        Observable<List<Recipe>> call = apiService.getRecipe();

        call.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Recipe>>() {
                    @Override
                    public void onCompleted() {
                        if (mIdlingResource != null) {
                            mIdlingResource.setIdleState(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        String errorMessage = RestClient.getErrorFail(RecipeActivity.this, e);
                        if (!Strings.isNullOrEmpty(errorMessage)) {
                            Toast.makeText(RecipeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(List<Recipe> response) {
                        Bundle recipesBundle = new Bundle();
                        recipesBundle.putParcelableArrayList(ALL_RECIPES, (ArrayList<? extends Parcelable>) response);

                        recipesAdapter.setRecipeData(response);
                    }
                });
    }

    @Override
    public void onListItemClick(Recipe selectedItemIndex) {

        Bundle selectedRecipeBundle = new Bundle();
        ArrayList<Recipe> selectedRecipe = new ArrayList<>();
        selectedRecipe.add(selectedItemIndex);
        selectedRecipeBundle.putParcelableArrayList(SELECTED_RECIPES, selectedRecipe);

        final Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtras(selectedRecipeBundle);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
