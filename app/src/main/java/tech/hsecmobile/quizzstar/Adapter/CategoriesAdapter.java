package tech.hsecmobile.quizzstar.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.Model.Category;
import tech.hsecmobile.quizzstar.R;

import java.util.ArrayList;

public class CategoriesAdapter extends  RecyclerView.Adapter<CategoriesAdapter.CategoriesHolder> {
    private Context context;
    private ArrayList<Category> categories;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class CategoriesHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView catImageUrl;
        public CategoriesHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            catName = itemView.findViewById(R.id.category_name);
            catImageUrl = itemView.findViewById(R.id.category_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
        public void setDetails(Category category) {
            catName.setText(category.getTitle());
            String urlImg = context.getResources().getString(R.string.domain_name);
            String allUrl = urlImg+"/assets/uploads/categories/"+category.getImageUrl();
            Picasso.get().load(allUrl).fit().centerInside().into(catImageUrl);
        }
    }
    public CategoriesAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
    }
    @NonNull
    @Override
    public CategoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_category, parent, false);
        return new CategoriesHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesHolder holder, int position) {
        Category category = categories.get(position);
        holder.setDetails(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}



