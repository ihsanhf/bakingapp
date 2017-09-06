package id.ihsan.bakingapp;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.ihsan.bakingapp.adapters.RecipeDetailAdapter;
import id.ihsan.bakingapp.models.Ingredient;
import id.ihsan.bakingapp.models.Recipe;
import id.ihsan.bakingapp.widgets.UpdateBakingService;

import static id.ihsan.bakingapp.RecipeActivity.SELECTED_RECIPES;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.04.09
 */
public class RecipeDetailFragment extends Fragment {

    private List<Recipe> recipe;
    private String recipeName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RecyclerView recyclerView;
        TextView textView;

        recipe = new ArrayList<>();

        if (savedInstanceState != null) {
            recipe = savedInstanceState.getParcelableArrayList(SELECTED_RECIPES);

        } else {
            recipe = getArguments().getParcelableArrayList(SELECTED_RECIPES);
        }

        List<Ingredient> ingredients = recipe.get(0).getIngredients();
        recipeName = recipe.get(0).getName();

        View rootView = inflater.inflate(R.layout.recipe_detail_fragment_body_part, container, false);
        textView = (TextView) rootView.findViewById(R.id.recipe_detail_text);

        ArrayList<String> recipeIngredientsForWidgets = new ArrayList<>();

        int index = 0;
        for (Ingredient a : ingredients) {
            textView.append("- " + a.getIngredient() + "\n");
            textView.append("  Quantity: " + a.getQuantity() + "\n");
            textView.append("  Measure: " + a.getMeasure() + "\n\n");

            recipeIngredientsForWidgets.add(a.getIngredient() + "\n" +
                    "Quantity: " + a.getQuantity() + "\n" +
                    "Measure: " + a.getMeasure() + "\n");
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recipe_detail_recycler);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        RecipeDetailAdapter mRecipeDetailAdapter = new RecipeDetailAdapter((RecipeDetailAdapter.ListItemClickListener) getActivity());
        recyclerView.setAdapter(mRecipeDetailAdapter);
        mRecipeDetailAdapter.setMasterRecipeData(recipe, getContext());

        //update widget
        UpdateBakingService.startBakingService(getContext(), recipeIngredientsForWidgets);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle currentState) {
        super.onSaveInstanceState(currentState);
        currentState.putParcelableArrayList(SELECTED_RECIPES, (ArrayList<? extends Parcelable>) recipe);
        currentState.putString("Title", recipeName);
    }
}


