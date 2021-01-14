package com.example.blind_wallet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import Classes.Speech;
import ClassificationModel.AssetsUtils;
import ClassificationModel.configs.GtsrbQuantConfig;
import ClassificationModel.configs.ModelConfig;
import ClassificationModel.ClassificationResult;


public class ClassificationActivity extends AppCompatActivity {


    private CameraView cameraView;


    private Interpreter interpreter;
    private List<String> labels;
    private ModelConfig modelConfig;
    private static final int MAX_CLASSIFICATION_RESULTS = 2;
    private static final float CLASSIFICATION_THRESHOLD = 0.2f;
    Button button;
    FirebaseFirestore fStore;
    String userId;
    FirebaseAuth fAuth;
    String username;

    String text;
    ArrayList<String> strings ;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Intent intent = new  Intent(this,MainActivity.class);
            Bundle b = new Bundle();
            b.putStringArrayList("returnedText",strings);
            intent.putExtras(b);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return false; //I have tried here true also
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId=fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    username =documentSnapshot.getString("fName");
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
        strings = new ArrayList<>();
        Bundle b = getIntent().getExtras();
        strings = b.getStringArrayList("TEXTVIEW_KEY");
        cameraView = (CameraView)findViewById(R.id.cameraView);
        button = (Button)findViewById(R.id.capture);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }
            @Override
            public void onError(CameraKitError cameraKitError) {
            }
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap1 = cameraKitImage.getBitmap();
                Bitmap bitmap =Bitmap.createScaledBitmap(bitmap1 , cameraView.getWidth() , cameraView.getHeight(),false);
                cameraView.stop();
                classifiy(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

    }
    private void classifiy(Bitmap bitmap) {
        modelConfig = new GtsrbQuantConfig();
        try {
            ByteBuffer model = AssetsUtils.loadFile(this,"model.tflite");
            this.interpreter = new Interpreter(model);
            this.labels = AssetsUtils.loadLines(this, modelConfig.getLabelsFilename());
            Bitmap toClassify = ThumbnailUtils.extractThumbnail(
                    bitmap, modelConfig.getInputWidth(), modelConfig.getInputHeight()
            );
            ByteBuffer byteBufferToClassify = bitmapToModelsMatchingByteBuffer(toClassify);
            if (modelConfig.isQuantized()) {
                runInferenceOnQuantizedModel(byteBufferToClassify);
            } else {
                runInferenceOnFloatModel(byteBufferToClassify);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }
    private void runInferenceOnQuantizedModel(ByteBuffer byteBufferToClassify) {
        byte[][] result = new byte[1][labels.size()];
        interpreter.run(byteBufferToClassify, result);
        float[][] resultFloats = new float[1][labels.size()];
        byte[] bytes = result[0];
        for (int i = 0; i < bytes.length; i++) {
            float resultF = (bytes[i] & 0xff) / 255.f;
            resultFloats[0][i] = resultF;
        }
        getSortedResult(resultFloats);
        //Toast.makeText(this, "hello : "+String.valueOf(resultFloats[0][0]), Toast.LENGTH_SHORT).show();
    }

    private void runInferenceOnFloatModel(ByteBuffer byteBufferToClassify) {
        float[][] result = new float[1][labels.size()];
        interpreter.run(byteBufferToClassify, result);
        getSortedResult(result);
        //Toast.makeText(this, "hello "+result.toString(), Toast.LENGTH_SHORT).show();
    }
    private void getSortedResult(float[][] resultsArray) {
        PriorityQueue<ClassificationResult> sortedResults = new PriorityQueue<>(
                MAX_CLASSIFICATION_RESULTS,
                (lhs, rhs) -> Float.compare(rhs.confidence, lhs.confidence)
        );
        float max = 0.0f;
        String mylabel= "";
        for (int i = 0; i < labels.size(); i++) {
            float confidence = resultsArray[0][i];
            if(max < confidence)
            {
                max = confidence;
                mylabel = labels.get(i);
            }
        }
        text = "<font color=#2874A6>BlindWallet@</font><font color = #B7950B>"+username+"</font><font color = #5F6A6A> >> </font><font color=#F0F3F4>Image Classification</font>";
        strings.add(text);
        text = "<font color= #ffa500 > Money Image is </font> <font color=#BA4A00>" + mylabel+" Pound</font>";
        strings.add(text);
        Toast.makeText(this,"Label : "+mylabel, Toast.LENGTH_SHORT).show();
        Speech speech = new Speech();
        speech.Texttospeech(this,mylabel);
    }
    private ByteBuffer bitmapToModelsMatchingByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(modelConfig.getInputSize());
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[modelConfig.getInputWidth() * modelConfig.getInputHeight()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < modelConfig.getInputWidth(); ++i) {
            for (int j = 0; j < modelConfig.getInputHeight(); ++j) {
                int pixelVal = intValues[pixel++];
                if (modelConfig.isQuantized()) {
                    for (byte channelVal : pixelToChannelValuesQuant(pixelVal)) {
                        byteBuffer.put(channelVal);
                    }
                } else {
                    for (float channelVal : pixelToChannelValues(pixelVal)) {
                        byteBuffer.putFloat(channelVal);
                    }
                }
            }
        }
        return byteBuffer;
    }
    private float[] pixelToChannelValues(int pixel) {
        if (modelConfig.getChannelsCount() == 1) {
            float[] singleChannelVal = new float[1];
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            singleChannelVal[0] = (rChannel + gChannel + bChannel) / 3 / modelConfig.getStd();
            return singleChannelVal;
        } else if (modelConfig.getChannelsCount() == 3) {
            float[] rgbVals = new float[3];
            rgbVals[0] = ((((pixel >> 16) & 0xFF) - modelConfig.getMean()) / modelConfig.getStd());
            rgbVals[1] = ((((pixel >> 8) & 0xFF) - modelConfig.getMean()) / modelConfig.getStd());
            rgbVals[2] = ((((pixel) & 0xFF) - modelConfig.getMean()) / modelConfig.getStd());
            return rgbVals;
        } else {
            throw new RuntimeException("Only 1 or 3 channels supported at the moment.");
        }
    }

    private byte[] pixelToChannelValuesQuant(int pixel) {
        byte[] rgbVals = new byte[3];
        rgbVals[0] = (byte) ((pixel >> 16) & 0xFF);
        rgbVals[1] = (byte) ((pixel >> 8) & 0xFF);
        rgbVals[2] = (byte) ((pixel) & 0xFF);
        return rgbVals;
    }
    public interface ClassificationListener {
        void onClassifiedFrame(List<ClassificationResult> classificationResults);
    }


}
