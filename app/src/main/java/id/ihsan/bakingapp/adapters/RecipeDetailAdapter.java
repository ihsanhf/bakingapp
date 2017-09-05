package id.ihsan.bakingapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import id.ihsan.bakingapp.R;
import id.ihsan.bakingapp.models.Recipe;
import id.ihsan.bakingapp.models.Step;

/**
 * @author Ihsan Helmi Faisal <ihsan.helmi@ovo.id>
 * @since 2017.04.09
 */
public class RecipeDetailAdapter extends RecyclerView.Adapter<RecipeDetailAdapter.RecyclerViewHolder> {

    private String recipeName;
    private List<Step> steps;

    private ListItemClickListener lOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(List<Step> stepsOut, int clickedItemIndex, String recipeName);
    }

    public RecipeDetailAdapter(ListItemClickListener listener) {
        lOnClickListener = listener;
    }

    public void setMasterRecipeData(List<Recipe> recipesIn, Context context) {
        //steps = recipesIn;
        steps = recipesIn.get(0).getSteps();
        recipeName = recipesIn.get(0).getName();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_detail_cardview_items, viewGroup, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.textRecyclerView.setText(steps.get(position).getId() + ". " + steps.get(position).getShortDescription());
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textRecyclerView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            textRecyclerView = (TextView) itemView.findViewById(R.id.shortDescription);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            lOnClickListener.onListItemClick(steps, clickedPosition, recipeName);
        }
    }
}
