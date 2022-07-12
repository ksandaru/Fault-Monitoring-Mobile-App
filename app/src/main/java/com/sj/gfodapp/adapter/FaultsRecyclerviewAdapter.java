package com.sj.gfodapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sj.gfodapp.R;
import com.sj.gfodapp.fragment.AddFaultFragment;
import com.sj.gfodapp.model.Fault;
import com.sj.gfodapp.utils.ListItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class FaultsRecyclerviewAdapter extends RecyclerView.Adapter<FaultsRecyclerviewAdapter.UsersRecyclerviewHolder> {

    Context context;
    List<Fault> dataList;
    List<Fault> filteredDataList;

    public FaultsRecyclerviewAdapter(Context context, List<Fault> userList) {
        this.context = context;
        this.dataList = userList;
        this.filteredDataList = userList;
    }

    @NonNull
    @Override
    public UsersRecyclerviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.disasters_recyclerview_item, parent, false);
        return new UsersRecyclerviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersRecyclerviewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.tv_disaster.setText(filteredDataList.get(position).getFault());
        holder.tv_monthAndYear.setText(filteredDataList.get(position).getMonthOccured() + "-"+filteredDataList.get(position).getYearOccured());
        holder.tv_DivNameAndRegNo.setText(filteredDataList.get(position).getDivision().getRegNumber()+" - "+filteredDataList.get(position).getDivision().getName());
        Picasso.get().load(filteredDataList.get(position).getImageUrl()).into(holder.imageDisaster);

        ListItemAnimation.animateFadeIn(holder.itemView, position);

        // TODO: Item onClick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFaultFragment.listItemOnClick(filteredDataList.get(position).getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredDataList.size();
    }

    public static final class UsersRecyclerviewHolder extends RecyclerView.ViewHolder {

        TextView tv_disaster, tv_monthAndYear, tv_DivNameAndRegNo;
        ImageView imageDisaster;
//        CircleImageView imageDisaster;
        public UsersRecyclerviewHolder(@NonNull View itemView) {
            super(itemView);
            imageDisaster = itemView.findViewById(R.id.imageDisaster);
            tv_disaster = itemView.findViewById(R.id.tv_disaster);
            tv_monthAndYear = itemView.findViewById(R.id.tv_monthAndYear);
            tv_DivNameAndRegNo = itemView.findViewById(R.id.tv_DivNameAndRegNo);
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

                    List<Fault> lstFiltered = new ArrayList<>();
                    for (Fault row : dataList) {
                        if (row.getYearOccured().toLowerCase().contains(Key.toLowerCase())) {
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
                filteredDataList = (List<Fault>) filterResults.values;
                notifyDataSetChanged();
            }
        };

    }


}

