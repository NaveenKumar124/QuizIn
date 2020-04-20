package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.quiz.Adapter.ResultGridAdapter;
import com.example.quiz.Common.Common;
import com.example.quiz.Common.SpaceDecoration;

import java.util.concurrent.TimeUnit;


public class ResultActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView txt_timer, txt_result, txt_right_answer;
    Button btn_filter_total, btn_filter_right, btn_filter_wrong, btn_filter_no_answer;
    RecyclerView recycler_result;

    BroadcastReceiver backToQuestion = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().toString().equals(Common.KEY_BACK_FROM_RESULT))
            {
                int question = intent.getIntExtra(Common.KEY_BACK_FROM_RESULT,-1);
                goBackActivityWithQuestion(question);
            }
        }
    };

    private void goBackActivityWithQuestion(int question) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Common.KEY_BACK_FROM_RESULT,question);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(backToQuestion,new IntentFilter(Common.KEY_BACK_FROM_RESULT));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("RESULT");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_result = (TextView) findViewById(R.id.txt_result);
        txt_right_answer = (TextView) findViewById(R.id.txt_right_answer);
        txt_timer = (TextView) findViewById(R.id.txt_timer);

        btn_filter_no_answer = (Button) findViewById(R.id.btn_filter_no_answer);
        btn_filter_right = (Button) findViewById(R.id.btn_filter_right_answer);
        btn_filter_wrong = (Button) findViewById(R.id.btn_filter_wrong_answer);
        btn_filter_total = (Button) findViewById(R.id.btn_filter_total);

        recycler_result = (RecyclerView) findViewById(R.id.recycler_result);
        recycler_result.setHasFixedSize(true);
        recycler_result.setLayoutManager(new GridLayoutManager(this,3));

        //Set Adapter
        ResultGridAdapter adapter = new ResultGridAdapter(this,Common.answerSheetList);
        recycler_result.addItemDecoration(new SpaceDecoration(4));
        recycler_result.setAdapter(adapter);

        txt_timer.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Common.timer),
                TimeUnit.MILLISECONDS.toSeconds(Common.timer)-
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Common.timer))));

        txt_right_answer.setText(new StringBuilder("").append(Common.right_answer_count).append("/")
        .append(Common.questionList.size()));

        btn_filter_total.setText(new StringBuilder("").append(Common.questionList.size()));
        btn_filter_right.setText(new StringBuilder("").append(Common.right_answer_count));
        btn_filter_wrong.setText(new StringBuilder("").append(Common.wrong_answer_count));
        btn_filter_no_answer.setText(new StringBuilder("").append(Common.no_answer_count));
    }
}
