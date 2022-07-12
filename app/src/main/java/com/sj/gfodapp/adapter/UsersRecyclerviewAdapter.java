package com.sj.gfodapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sj.gfodapp.R;
import com.sj.gfodapp.fragment.AddUsersFragment;
import com.sj.gfodapp.model.User;
import com.sj.gfodapp.utils.ListItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersRecyclerviewAdapter extends RecyclerView.Adapter<UsersRecyclerviewAdapter.UsersRecyclerviewHolder> {

    Context context;
    List<User> userList;
    List<User> filteredUserList;

    public UsersRecyclerviewAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.filteredUserList = userList;
    }

    @NonNull
    @Override
    public UsersRecyclerviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.users_recyclerview_item, parent, false);
        return new UsersRecyclerviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersRecyclerviewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txtFullName.setText(filteredUserList.get(position).getFullName());
       // holder.txtEmail.setText(filteredUserList.get(position).getEmail());
        holder.txtNIC.setText(filteredUserList.get(position).getNic());
        holder.txtStatus.setText(filteredUserList.get(position).getStatus());
        Picasso.get().load(filteredUserList.get(position).getAvatartUrl()).into(holder.userImage);
        ListItemAnimation.animateFadeIn(holder.itemView, position);

        // TODO: Item onClick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Item onlick
                AddUsersFragment.userListItemOnClick(
                        filteredUserList.get(position).getUserProfileId());
            }
        });

        holder.btnActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUsersFragment.rfid_tagListItemOnClick(
                        filteredUserList.get(position).getUserProfileId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredUserList.size();
    }

    public static final class UsersRecyclerviewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        MaterialButton btnActions;
        TextView txtFullName, txtNIC, txtEmail, txtStatus;

        public UsersRecyclerviewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.imageDisaster);
            btnActions = itemView.findViewById(R.id.btnActions);
            txtFullName = itemView.findViewById(R.id.tv_disaster);
            txtNIC = itemView.findViewById(R.id.tv_monthAndYear2);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String Key = charSequence.toString();
                if (Key.isEmpty()) {
                    filteredUserList = userList;
                } else {

                    List<User> lstFiltered = new ArrayList<>();
                    for (User row : userList) {
                        if (row.getNic().toLowerCase().contains(Key.toLowerCase()) || row.getFullName().toLowerCase().contains(Key.toLowerCase()) || row.getEmail().toLowerCase().contains(Key.toLowerCase())) {
                            lstFiltered.add(row);
                        }
                    }

                    filteredUserList = lstFiltered;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredUserList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredUserList = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };

    }


}

