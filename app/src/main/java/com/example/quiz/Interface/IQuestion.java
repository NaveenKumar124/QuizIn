package com.example.quiz.Interface;

import com.example.quiz.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer();  //Get selected answer from user select
    void showCorrectAnswer();  //Bold correct Answer text
    void disableAnswer();  //Disable all CheckBox
    void resetQuestion();  //
}
