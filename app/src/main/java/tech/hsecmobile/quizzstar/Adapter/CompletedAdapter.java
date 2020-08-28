package tech.hsecmobile.quizzstar.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tech.hsecmobile.quizzstar.Model.Completed;
import tech.hsecmobile.quizzstar.R;

import java.util.ArrayList;

public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.CompletedHolder> {

    private Context context;
    private ArrayList<Completed> completeds;


    public class CompletedHolder extends RecyclerView.ViewHolder {
        private TextView categoryName, categoryLevel, totalPoints, earnedPoints, wastedPoints,percentage;

        public CompletedHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = (TextView) itemView.findViewById(R.id.statistics_category);
            categoryLevel = (TextView) itemView.findViewById(R.id.statistics_level);
            totalPoints = (TextView) itemView.findViewById(R.id.statistics_total_points);
            earnedPoints = (TextView) itemView.findViewById(R.id.statistics_earned_points);
            wastedPoints = (TextView) itemView.findViewById(R.id.statistics_wasted_points);
            percentage = (TextView) itemView.findViewById(R.id.statistics_percentage);
        }
        public void setDetails(Completed completed) {
            categoryName.setText(completed.getCategoryName() + " (" + completed.getCategoryLevel() + ")");
            // categoryLevel.setText(completed.getCategoryLevel());
            totalPoints.setText(String.valueOf(completed.getTotalPoints()));
            earnedPoints.setText(String.valueOf(completed.getEarnedPoints()) + " points");
            wastedPoints.setText(String.valueOf(completed.getWastedPoints()) + " points");
            percentage.setText(String.valueOf(completed.getPercentage()) + " %");
        }
    }

    public CompletedAdapter(Context context, ArrayList<Completed> completeds) {
        this.context = context;
        this.completeds = completeds;
    }

    @NonNull
    @Override
    public CompletedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_completed, parent, false);
        return new CompletedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedHolder holder, int position) {
        Completed completed = completeds.get(position);
        holder.setDetails(completed);
    }

    @Override
    public int getItemCount() {
        return completeds.size();
    }


}



