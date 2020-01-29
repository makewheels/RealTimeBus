package com.eg.realtimebus.activity.timetable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.eg.realtimebus.R;
import com.eg.realtimebus.util.Constants;
import com.eg.realtimebus.util.HttpUtil;

import java.util.List;

public class TimeTableActivity extends AppCompatActivity {
    private RecyclerView rv_timeTable;
    private final static int WHAT_TIME_TABLE = 1;

    private String busName;
    private String direction;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //显示发车时刻列表
                case WHAT_TIME_TABLE:
                    ClientTimeTable clientTimeTable = JSON.parseObject(
                            (String) msg.obj, ClientTimeTable.class);
                    //如果出现了错误
                    if (clientTimeTable.getMsg().equals("ok") == false) {

                    }
                    rv_timeTable.setAdapter(new MyAdapter(
                            TimeTableActivity.this, clientTimeTable.getTimeList()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        rv_timeTable = findViewById(R.id.rv_timeTable);
        //垂直线性布局
        rv_timeTable.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //分割线
        rv_timeTable.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        Intent intent = getIntent();
        busName = intent.getStringExtra("busName");
        direction = intent.getStringExtra("direction");
        showTimeTable();
    }

    /**
     * 搞定时刻表
     */
    private void showTimeTable() {
        new Thread() {
            @Override
            public void run() {
                String json = HttpUtil.get(Constants.BASE_URL + "/bus/getTimeTable?busName="
                        + busName + "&direction=" + direction);
                Message message = Message.obtain();
                message.what = WHAT_TIME_TABLE;
                message.obj = json;
                handler.sendMessage(message);
            }
        }.start();
    }

    public class MyAdapter extends RecyclerView.Adapter<TimeTableActivity.MyAdapter.MyViewHolder> {
        private Context context;
        private List<String> timeTableList;

        public MyAdapter(Context context, List<String> timeTableList) {
            this.context = context;
            this.timeTableList = timeTableList;
        }

        @NonNull
        @Override
        public TimeTableActivity.MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(View.inflate(context, R.layout.item_common_list, null));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.tv_text.setText(timeTableList.get(position));
        }

        @Override
        public int getItemCount() {
            return timeTableList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_text;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.tv_text = itemView.findViewById(R.id.tv_text);
            }
        }
    }
}
