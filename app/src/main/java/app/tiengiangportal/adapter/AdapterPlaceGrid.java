/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import app.tiengiangportal.R;
import app.tiengiangportal.data.Constant;
import app.tiengiangportal.model.Place;
import app.tiengiangportal.utils.Tools;

public class AdapterPlaceGrid extends RecyclerView.Adapter<AdapterPlaceGrid.ViewHolder> implements Filterable {

    private Context ctx;

    private List<Place> original_items = new ArrayList<>();
    private List<Place> filtered_items = new ArrayList<>();

    private ItemFilter mFilter = new ItemFilter();
    private ImageLoader imgloader = ImageLoader.getInstance();

    private OnItemClickListener onItemClickListener;

    private int lastPosition = -1;
    private boolean clicked = false;


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public ImageView image;
        public TextView distance;
        public LinearLayout lyt_distance;
        public MaterialRippleLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.title);
            image = (ImageView) v.findViewById(R.id.image);
            distance = (TextView) v.findViewById(R.id.distance);
            lyt_distance = (LinearLayout) v.findViewById(R.id.lyt_distance);
            lyt_parent = (MaterialRippleLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public Filter getFilter() {
        return mFilter;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterPlaceGrid(Context ctx, List<Place> items) {
        this.ctx = ctx;
        original_items = items;
        filtered_items = items;
        if (!imgloader.isInited()) Tools.initImageLoader(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Place p = filtered_items.get(position);
        holder.name.setText(p.name);
        imgloader.displayImage(Constant.getURLimgPlace(p.image), holder.image, Tools.getGridOption());

        if(p.distance == -1){
            holder.lyt_distance.setVisibility(View.GONE);
        } else {
            holder.lyt_distance.setVisibility(View.VISIBLE);
            holder.distance.setText(Tools.getFormatedDistance(p.distance));
        }

        // Here you apply the animation when the view is bound
        setAnimation(holder.lyt_parent, position);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!clicked && onItemClickListener != null) {
                    clicked = true;
                    onItemClickListener.onItemClick(v, p);
                }
            }
        });
        clicked = false;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filtered_items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String query = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<Place> list = original_items;
            final List<Place> result_list = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                String str_title = list.get(i).name;
                if (str_title.toLowerCase().contains(query)) {
                    result_list.add(list.get(i));
                }
            }
            results.values = result_list;
            results.count = result_list.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered_items = (List<Place>) results.values;
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Place viewModel);
    }
}