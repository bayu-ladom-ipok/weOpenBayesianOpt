package ladom.ipok.bayutflite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


   /* Interpreter tflite;
    final String ASSOCIATED_AXIS_LABELS = "labels.txt";
    List<String> associatedAxisLabels = null;*/
    BayuProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bp = new BayuProcessor(this);
        setContentView(R.layout.activity_main);
    }

    public void doClick(View view) {
        // Do something in response to button
        String tampil = "Oke Boss ...";
        TextView tampilText = (TextView) findViewById(R.id.textView);
        String myRet = bp.convert();
        tampil = myRet;
        tampilText.setText(tampil);
        System.out.println(myRet);
    }



}