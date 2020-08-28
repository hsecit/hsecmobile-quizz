package tech.hsecmobile.quizzstar.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.Model.Referral;
import tech.hsecmobile.quizzstar.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReferralAdapter extends RecyclerView.Adapter<ReferralAdapter.ReferralHolder>{

    private Context context;
    private ArrayList<Referral> referrals;

    public class ReferralHolder extends RecyclerView.ViewHolder {
        private TextView referName, referEmail, referDate;
        private CircleImageView referImage;

        public ReferralHolder(@NonNull View itemView) {
            super(itemView);
            referName = (TextView) itemView.findViewById(R.id.refer_name);
            referEmail = (TextView) itemView.findViewById(R.id.refer_email);
            referDate = (TextView) itemView.findViewById(R.id.refer_date);
            referImage = (CircleImageView) itemView.findViewById(R.id.single_image);
        }
        public void setDetails(Referral referral) {
            referName.setText(referral.getName());
            referEmail.setText(referral.getEmail());
            referDate.setText(referral.getDate());
            Picasso.get().load(referral.getImageUrl()).fit().centerInside().into(referImage);
        }
    }

    public ReferralAdapter(Context context, ArrayList<Referral> referrals) {
        this.context = context;
        this.referrals = referrals;
    }

    @NonNull
    @Override
    public ReferralAdapter.ReferralHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_refer, parent, false);
        return new ReferralHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralAdapter.ReferralHolder holder, int position) {
        Referral referral = referrals.get(position);
        holder.setDetails(referral);
    }

    @Override
    public int getItemCount() {
        return referrals.size();
    }
}
