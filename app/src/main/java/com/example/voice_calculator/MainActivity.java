package com.example.voice_calculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //IDs of all numeric buttons
    private int[] numericButtons= {R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree, R.id.btnFour,
    R.id.btnFive, R.id.btnSix, R.id.btnSeven, R.id.btnEight, R.id.btnNine};

    //IDs of all operators
    private int[] operatorsButtons={R.id.btnAdd,R.id.btnSubtract,R.id.btnMultiply,R.id.btnDivide};

    private TextView txtScreen;
    private boolean lastNumeric;
    private boolean stateError;
    private boolean lastDot;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSpeak= findViewById(R.id.btnSpeak);
        txtScreen= findViewById(R.id.txtScreen);

        //find and set onclickListener to numeric buttons
        setNumericOnClickListener();

        //find and set onclickListener to operators, equal button and decimal point button
        setOperatorOnClickListener();
    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener=new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //just append/set text of clicked button
                Button button= (Button) v;
                if(stateError){
                    //if current state is error, replace the error message
                    txtScreen.setText(button.getText());
                    stateError=false;
                }else {
                    txtScreen.append(button.getText());
                }
                //set the FLAG
                lastNumeric=true;
            }
        };
        //assign the listener to all the numeric button
        for(int id:numericButtons){
            findViewById(id).setOnClickListener(listener);
        }
    }
    private void setOperatorOnClickListener() {
        View.OnClickListener listener=new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //if the current state is error, do not append the operator
                //if last input is number only,append the operator
                if(lastNumeric && !stateError){
                    Button button=(Button) view;
                    txtScreen.append(button.getText());
                    lastNumeric=false;
                    lastDot= false; //reset the DOT flag
                }
            }
        };

        //assign the listener to all the operator buttons
        for (int id:operatorsButtons){
            findViewById(id).setOnClickListener(listener);
        }
        //decimal point
        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastNumeric && !stateError && !lastDot)
                    txtScreen.append(".");
                    lastNumeric=false;
                    lastDot=false;
            }
        });
        //clear button
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");
                lastNumeric=false;
                stateError=false;
                lastDot=false;
            }
        });

        //Equal button
        findViewById(R.id.btnEqual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqual();
            }
        });

        //speak button
        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stateError){
                    txtScreen.setText("Try Again");
                    stateError=false;
                }else{
                    promptSpeechInput();
                }
                lastNumeric=true;
            }
        });
    }
    private void onEqual() {
        if(lastNumeric && !stateError){
            String txt=txtScreen.getText().toString();

            //create and expression
            try{
                Expression expression=null;
                try{
                    expression=new ExpressionBuilder(txt).build();
                    double result=expression.evaluate();
                    txtScreen.setText(Double.toString(result));
                }catch (Exception e){
                    txtScreen.setText("Error");
                }
            }catch(ArithmeticException ex){
                txtScreen.setText("Error");
                stateError=true;
                lastNumeric=false;
            }
        }
    }
    private void promptSpeechInput() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        }catch(ActivityNotFoundException a){
                Toast.makeText(getApplicationContext(),getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }

    //receiving speech input
    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
    super.onActivityResult(requestCode,resultCode,data);
    switch(requestCode){
        case REQ_CODE_SPEECH_INPUT: {
            if(resultCode == RESULT_OK && null !=data){
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String change=result.get(0);
                change=change.replace("x","*");
                change=change.replace("X","*");
                change=change.replace("add","+");
                change=change.replace("sub","-");
                change=change.replace("to","2");
                change=change.replace("plus","+");
                change=change.replace("minus","-");
                change=change.replace("times","*");
                change=change.replace("into","*");
                change=change.replace("in2","*");
                change=change.replace("multiply by","*");
                change=change.replace("divide by","/");
                change=change.replace("divide","/");
                change=change.replace("equal","=");
                change=change.replace("equals","=");
                change=change.replace("equals to","=");
                change=change.replace("is equal to","=");

                if(change.contains("=")){
                    change=change.replace("=","");
                    txtScreen.setText(change);
                    onEqual();
                }else {
                    txtScreen.setText(change);
                }
            }
            break;
        }
    }
    }
}