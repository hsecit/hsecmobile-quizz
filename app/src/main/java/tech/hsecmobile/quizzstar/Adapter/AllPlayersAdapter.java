package tech.hsecmobile.quizzstar.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.Model.Player;
import tech.hsecmobile.quizzstar.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllPlayersAdapter extends  RecyclerView.Adapter<AllPlayersAdapter.FirstPlayerHolder> {

    private Context context;
    private List<Player> players;


    public class FirstPlayerHolder extends RecyclerView.ViewHolder {
        private TextView playerName, playerPoints, playerPosition;
        private CircleImageView playerImage;

        public FirstPlayerHolder(@NonNull View itemView) {
            super(itemView);
            playerName = (TextView) itemView.findViewById(R.id.all_player_name);
            playerPoints = (TextView) itemView.findViewById(R.id.all_player_points);
            playerImage = (CircleImageView) itemView.findViewById(R.id.all_player_image);
            playerPosition = (TextView) itemView.findViewById(R.id.all_player_rank);
        }
        public void setDetails(Player player) {
            playerName.setText(player.getName());
            playerPoints.setText(String.valueOf(player.getPoints()));
            playerPosition.setText(String.valueOf(getAdapterPosition()+4)+ "/");
            Picasso.get().load(player.getImage_url()).fit().centerInside().into(playerImage);
        }
    }

    public AllPlayersAdapter(Context context, List<Player> players) {
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public FirstPlayerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_all_player_layout, parent, false);
        return new FirstPlayerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FirstPlayerHolder holder, int position) {
        Player player = players.get(position);
        holder.setDetails(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }
}

