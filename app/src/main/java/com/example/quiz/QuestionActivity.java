package com.example.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quiz.Adapter.AnswerSheetAdapter;
import com.example.quiz.Adapter.QuestionFragmentAdapter;
import com.example.quiz.Common.Common;
import com.example.quiz.DBHelper.DBHelper;
import com.example.quiz.Model.CurrentQuestion;
import com.example.quiz.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

     int time_play = Common.TOTAL_TIME;
     boolean isAnswerModeView = false;


     TextView txt_right_answer,txt_timer,txt_wrong_answer;

     RecyclerView answer_sheet_view;
     AnswerSheetAdapter answerSheetAdapter;

     ViewPager viewPager;
     TabLayout tabLayout;

    @Override
    protected void onDestroy() {
        if (Common.countDownTimer != null)
            Common.countDownTimer.cancel();
        super.onDestroy();
    }

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        //First, we need to take question from database
        takeQuestion();


        if (Common.questionList.size() > 0) {

            //Show Text View right answer and Text view Timer
            txt_right_answer = (TextView) findViewById(R.id.txt_question_right);
            txt_timer = (TextView) findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d", Common.right_answer_count, Common.questionList.size())));

            countTimer();



            getAnswerSheetItems();

            //View
            answer_sheet_view = (RecyclerView) findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);
            //answer_sheet_view.setLayoutManager(new GridLayoutManager(this));
            if (Common.questionList.size() > 5) //If question List has size > 5, we will seperate 2 rows
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size() / 2));
            answerSheetAdapter = new AnswerSheetAdapter(this, Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);

            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

            genFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,
                    Common.fragmentsList);
            viewPager.setAdapter(questionFragmentAdapter);

            tabLayout.setupWithViewPager(viewPager);

            //Event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDETERMINED = 2;

                int currentScrollDirection = 2;

                private void setScrollingDirection(float positionOffset)
                {
                    if ((1-positionOffset) >= 0.5)
                        this.currentScrollDirection = SCROLLING_RIGHT;
                    else if ((1-positionOffset) <= 0.5)
                        this.currentScrollDirection = SCROLLING_LEFT;
                }

                private boolean isScrollDirectionUndetermined()
                {
                    return currentScrollDirection == SCROLLING_UNDETERMINED;
                }

                private boolean isScrollingRight()
                {
                    return currentScrollDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft()
                {
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int i, float v, int i1) {

                    if (isScrollDirectionUndetermined())
                        setScrollingDirection(v);

                }

                @Override
                public void onPageSelected(int i) {

                    QuestionFragment questionFragment;
                    int position = 0;
                    if (i>0)
                    {
                        if (isScrollingRight())
                        {
                            //If user scroll to right, get previous fragment to calculate result
                            questionFragment = Common.fragmentsList.get(i-1);
                            position = i-1;
                        }
                        else if (isScrollingLeft())
                        {
                            //If user scroll to left, get next fragment to calculate result
                            questionFragment = Common.fragmentsList.get(i+1);
                            position = i+1;
                        }
                        else{
                            questionFragment = Common.fragmentsList.get(position);
                        }
                    }
                    else{
                        questionFragment = Common.fragmentsList.get(0);
                        position = 0;
                    }

                    //To show correct answer, just call function here
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(position,question_state); //set question answer for answer sheet
                    answerSheetAdapter.notifyDataSetChanged(); //Change color in answer sheet

                    countCorrectAnswer();

                    txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                    .append("/")
                    .append(String.format("%d",Common.questionList.size())).toString());

                    //txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER)
                    {
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }

                }

                @Override
                public void onPageScrollStateChanged(int i) {

                    if (i == ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollDirection = SCROLLING_UNDETERMINED;


                }
            });

        }


    }

    private void finishGame() {

        int position = viewPager.getCurrentItem();
        QuestionFragment questionFragment = Common.fragmentsList.get(position);
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
        Common.answerSheetList.set(position,question_state); //set question answer for answer sheet
        answerSheetAdapter.notifyDataSetChanged(); //Change color in answer sheet


        countCorrectAnswer();

        txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                .append("/")
                .append(String.format("%d",Common.questionList.size())).toString());

        //txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

        if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER)
        {
            questionFragment.showCorrectAnswer();
            questionFragment.disableAnswer();
        }

        //We will navigate to new Result Activity here

    }

    private void countCorrectAnswer() {

        //Reset variable
        Common.right_answer_count = Common.wrong_answer_count = 0;
        for (CurrentQuestion item:Common.answerSheetList)
            if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++;
            else if (item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;

    }

    private void genFragmentList() {

        for (int i = 0 ; i < Common.questionList.size() ; i++)
        {
            Bundle bundle = new Bundle();
            bundle.putInt("index",i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentsList.add(fragment);
        }

    }

    private void getAnswerSheetItems() {
    }

    private void countTimer() {

            if (Common.countDownTimer == null)
            {
                Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d: %02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play -= 1000;
                    }

                    @Override
                    public void onFinish() {

                        //Finish Game


                    }
                }.start();
            }
            else
            {
                Common.countDownTimer.cancel();
                Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d: %02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play -= 1000;
                    }

                    @Override
                    public void onFinish() {

                        //Finish Game


                    }
                }.start();
            }

    }

    private void takeQuestion() {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());
        if (Common.questionList.size() == 0)
        {
            //If no question
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Oops!")
                    .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    .setDescription("We do not have any questons in this"+ Common.selectedCategory.getName()+"Category")
                    .setPositiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
        else
        {

            if (Common.answerSheetList.size() > 0)
                Common.answerSheetList.clear();
            //Get answer sheet item from question
            //30 question = 30 answer sheet item
            //1question = 1 answer sheet item

            //for(Question question:Common.questionList)
            for (int i=0; i<Common.questionList.size(); i++)
            {
                //Because we need to take index of question in list, so we will use i
                Common.answerSheetList.add(new CurrentQuestion(i,Common.ANSWER_TYPE.NO_ANSWER)); //By default, all answers are no answer
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout) item.getActionView();
//        txt_wrong_answer = (TextView) constraintLayout.findViewById(R.id.txt_wrong_answer);
//        txt_wrong_answer.setText(String.valueOf(0));


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_finish_game){

            if (!isAnswerModeView)
            {
                new MaterialStyledDialog.Builder(this)
                        .setTitle("Finish ?")
                        .setIcon(R.drawable.ic_mood_black_24dp)
                        .setDescription("Do you really want to Finish?")
                        .setNegativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finishGame();
                            }
                        }).show();
            }

        return true;
    }

        return super.onOptionsItemSelected(item);
    }



    //    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

   // @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        //Handle navigation view tem clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera);
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
