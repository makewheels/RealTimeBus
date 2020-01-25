package com.eg.realtimebus.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eg.realtimebus.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SearchView sv_search;
    private RecyclerView rv_busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv_search = findViewById(R.id.sv_search);
        rv_busList = findViewById(R.id.rv_busList);

        initBusList();
    }

    private void initBusList() {
        List<String> commonBusList = new ArrayList<>();
        commonBusList.add("50");
        commonBusList.add("225");
        commonBusList.add("810");
        commonBusList.add("11");
        commonBusList.add("22");
        rv_busList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_busList.setAdapter(new BusListAdapter(this, commonBusList));
    }

    public class BusListAdapter extends RecyclerView.Adapter<BusListAdapter.MyViewHolder> {
        private Context context;
        private List<String> commonBusList;

        public BusListAdapter(Context context, List<String> commonBusList) {
            this.context = context;
            this.commonBusList = commonBusList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(View.inflate(context, R.layout.item_commonbus, null));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            if (holder.tv_bus == null)
                Log.e("tag", "awegawg4e");
            holder.tv_bus.setText(commonBusList.get(position));
        }

        @Override
        public int getItemCount() {
            return commonBusList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView tv_bus;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.tv_bus = findViewById(R.id.tv_bus);
            }
        }
    }
}
