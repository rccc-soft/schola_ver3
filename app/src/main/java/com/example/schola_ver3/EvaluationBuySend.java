package com.example.schola_ver3;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.  ;

//EvaluationSellから
public class EvaluationBuySend extends AppCompatActivity implements View.OnClickListener{
    private Button confirmButton;
    private Button cancelButton;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buyer_evaluation_confirmation0);

        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(this);
        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirmButton) {
            // 結果をセットして終了
            setResult(RESULT_OK);
            finish();
        } else if (v.getId() == R.id.cancelButton) {
            // 結果をセットして終了
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
