package com.eg.realtimebus.activity.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
        setContentView(R.layout.activity_enter);

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
        rv_busList.setAdapter(new BusListAdapter(commonBusList));
    }

    public class BusListAdapter extends RecyclerView.Adapter<BusListAdapter.ViewHolder> {
        private List<String> commonBusList;
        private RecyclerView.ViewHolder holder;
        private int position;

        public BusListAdapter(List<String> commonBusList) {
            this.commonBusList = commonBusList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            this.holder = holder;
            this.position = position;
        }

        @Override
        public int getItemCount() {
            return commonBusList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
