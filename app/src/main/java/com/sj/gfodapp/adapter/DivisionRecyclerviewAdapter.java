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

import com.sj.gfodapp.R;
import com.sj.gfodapp.fragment.AddDivisionFragment;
import com.sj.gfodapp.model.Operator_Division;
import com.sj.gfodapp.utils.ListItemAnimation;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
public class DivisionRecyclerviewAdapter extends RecyclerView.Adapter<DivisionRecyclerviewAdapter.UsersRecyclerviewHolder> {

    Context context;
    List<Operator_Division> dataList;
    List<Operator_Division> filteredDataList;

    public DivisionRecyclerviewAdapter(Context context, List<Operator_Division> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.filteredDataList = dataList;
    }

    @NonNull
    @Override
    public UsersRecyclerviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.divisions_recyclerview_item, parent, false);
        return new UsersRecyclerviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersRecyclerviewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_regNumber.setText("ID: "+filteredDataList.get(position).getRegNumber());
        holder.txt_Name.setText("Name: "+filteredDataList.get(position).getName());
        holder.txt_Province_Discrict.setText("District: " + filteredDataList.get(position).getDistrict());
     //   Picasso.get().load(filteredDataList.get(position).getAvatartUrl()).into(holder.userImage);

        ListItemAnimation.animateFadeIn(holder.itemView, position);

        // TODO: Item onClick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Item onlick
                AddDivisionFragment.divisionsListItemOnClick(
                        new Operator_Division(
                                filteredDataList.get(position).getId(),
                                filteredDataList.get(position).getRegNumber(),
                                filteredDataList.get(position).getName(),
                                filteredDataList.get(position).getDistrict(),
                                filteredDataList.get(position).getLongitude(),
                                filteredDataList.get(position).getLatitude()));
            }
        });

        holder.tv_actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDivisionFragment.divisionsListItemOnClick(
                        new Operator_Division(
                                filteredDataList.get(position).getId(),
                                filteredDataList.get(position).getRegNumber(),
                                filteredDataList.get(position).getName(),
                                filteredDataList.get(position).getDistrict(),
                                filteredDataList.get(position).getLongitude(),
                                filteredDataList.get(position).getLatitude()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredDataList.size();
    }

    public static final class UsersRecyclerviewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView txt_regNumber, txt_Name, txt_Province_Discrict, tv_actions;

        public UsersRecyclerviewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.imageDisaster);
            txt_regNumber = itemView.findViewById(R.id.tv_disaster);
            txt_Name = itemView.findViewById(R.id.tv_monthAndYear);
            txt_Province_Discrict = itemView.findViewById(R.id.txt_Province_Discrict);
            tv_actions = itemView.findViewById(R.id.tv_actions);
        }
    }

    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String Key = charSequence.toString();
                if (Key.isEmpty()) {
                    filteredDataList = dataList;
                } else {

                    List<Operator_Division> lstFiltered = new ArrayList<>();
                    for (Operator_Division row : dataList) {
                        if (row.getRegNumber().toLowerCase().contains(Key.toLowerCase()) || row.getName().toLowerCase().contains(Key.toLowerCase()) ||  row.getDistrict().toLowerCase().contains(Key.toLowerCase())) {
                            lstFiltered.add(row);
                        }
                    }
                    filteredDataList = lstFiltered;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredDataList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredDataList = (List<Operator_Division>) filterResults.values;
                notifyDataSetChanged();
            }
        };

    }


}


