package com.pajir.master;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.HistoryViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Integer> mHistoryId = new ArrayList<>();
    private ArrayList<String> mHistoryDuring = new ArrayList<>();
    private ArrayList<Integer> mHistorySum = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<Integer> history_id, ArrayList<String> during_time, ArrayList<Integer> sum_time){
        mHistoryId = history_id;
        mHistoryDuring = during_time;
        mHistorySum = sum_time;
        mContext = context;

    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.history_id.setText(String.format(Locale.CHINA, "%d", mHistoryId.get(position)));
        holder.during_time.setText(mHistoryDuring.get(position));
        holder.sum_time.setText(String.format(Locale.CHINA, " %3d 分钟", mHistorySum.get(position)));

        holder.hi_layout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG, "onClick: clicked on : " + mHistoryId.get(position));
                Toast.makeText(mContext, mHistoryDuring.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mHistoryId.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView history_id;
        TextView during_time;
        TextView sum_time;
        RelativeLayout hi_layout;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            history_id = itemView.findViewById(R.id.textview_history_id);
            during_time = itemView.findViewById(R.id.textview_history_during_time);
            sum_time = itemView.findViewById(R.id.textview_history_sum_time);
            hi_layout = itemView.findViewById(R.id.hi_layout);
        }
    }
}
