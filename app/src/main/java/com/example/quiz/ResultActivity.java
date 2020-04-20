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
import android.view.View;
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

    ResultGridAdapter adapter,filtered_adapter;

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
        adapter = new ResultGridAdapter(this,Common.answerSheetList);
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

        //Calculate result
        int percent = (Common.right_answer_count*100/Common.questionList.size());
        if (percent > 80)
        {
            txt_result.setText("EXCELLENT");
        }
        else if (percent > 70 && percent < 80)
        {
            txt_result.setText("GOOD");
        }
        else if (percent > 60 && percent < 70)
        {
            txt_result.setText("FAIR");
        }
        else if (percent > 50 && percent < 60)
        {
            txt_result.setText("POOR");
        }
        else if (percent > 40 && percent < 50)
        {
            txt_result.setText("BAD");
        }
        else
        {
            txt_result.setText("FAILING");
        }

        //Event filter
        btn_filter_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null)
                {
                    adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetList);
                    recycler_result.setAdapter(adapter);
                }
                else
                    recycler_result.setAdapter(adapter);
            }
        });

        btn_filter_no_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.answerSheetListFiltered.clear();
                for (int i=0;i<Common.answerSheetList.size();i++)
                {
                    if (Common.answerSheetList.get(i).getType() ==  Common.ANSWER_TYPE.NO_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recycler_result.setAdapter(adapter);
            }
        });

        btn_filter_wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.answerSheetListFiltered.clear();
                for (int i=0;i<Common.answerSheetList.size();i++)
                {
                    if (Common.answerSheetList.get(i).getType() ==  Common.ANSWER_TYPE.WRONG_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recycler_result.setAdapter(adapter);
            }
        });

        btn_filter_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.answerSheetListFiltered.clear();
                for (int i=0;i<Common.answerSheetList.size();i++)
                {
                    if (Common.answerSheetList.get(i).getType() ==  Common.ANSWER_TYPE.RIGHT_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recycler_result.setAdapter(adapter);
            }
        });
    }
}
