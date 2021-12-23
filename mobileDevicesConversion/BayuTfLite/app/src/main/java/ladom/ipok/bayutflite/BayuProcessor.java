package ladom.ipok.bayutflite;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.dreamolight.jnpy.Npy;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BayuProcessor {
    private Context context;
    private InputStream stream;
    private Interpreter tflite;
    MappedByteBuffer tfliteModel;
    final String ASSOCIATED_AXIS_LABELS = "labels.txt";
    List<String> associatedAxisLabels = null;


    TensorImage  myTensorImage,  myDataOut;


    public  BayuProcessor(@NonNull Context context) {
        this.context = context;

    }

    public String convert(){
        try {
            stream = context.getAssets().open("00000001_000.png");
        }
        catch (IOException ex) {
            Logger.getLogger(BayuProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            associatedAxisLabels = FileUtil.loadLabels(context, ASSOCIATED_AXIS_LABELS);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }


        try{
            tfliteModel = FileUtil.loadMappedFile(context, "converted_model.tflite");
        } catch (IOException e){
            Log.e("tfliteSupport", "Error reading model", e);
        }
        String myRet = new String("");
        try {
            tflite = new Interpreter(tfliteModel);

            Npy npy = new Npy(stream);
            float[] npyData = npy.floatElements();

            TensorBuffer tensorBufferIn = TensorBuffer.createFixedSize(new int[]{1, 1, 1536}, DataType.FLOAT32);
            TensorBuffer tensorBufferOut = TensorBuffer.createFixedSize(new int[]{1, 14}, DataType.FLOAT32);

            tensorBufferIn.loadArray(npyData);
            tflite.run(tensorBufferIn.getBuffer(),tensorBufferOut.getBuffer());
            /*
            TensorProcessor probabilityProc =
                    new TensorProcessor.Builder().add(new DequantizeOp((float)0,(float) (1/255.0))).build();
            TensorBuffer dequantizedBuffer = probabilityProc.process(tensorBufferOut);
            */

            // Post-processor which dequantize the result
            TensorProcessor probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

            if (null != associatedAxisLabels) {
                // Map of labels and their corresponding probability
                TensorLabel labels = new TensorLabel(associatedAxisLabels,
                        probabilityProcessor.process(tensorBufferOut));

                // Create a map to access the result based on label
                Map<String, Float> floatMap = labels.getMapWithFloatValue();

                float max = Collections.max(floatMap.values());
                String maxDisease = new String("");

                for (Map.Entry<String, Float> entry : floatMap.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());
                    if (entry.getValue()==max) {
                        maxDisease = String.valueOf(entry.getKey());
                        //System.out.println("Nilai maksimal "+entry.getKey());
                    }
                }

                myRet = "Detected disease is: "+ maxDisease;


            }


            tflite.close();
        } catch (IOException ex) {
            Logger.getLogger(BayuProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myRet;
    }

    public String convertDebug(){
        String myRet = new String("");
        myRet = "Jozzz";
        return myRet;
    }
}
