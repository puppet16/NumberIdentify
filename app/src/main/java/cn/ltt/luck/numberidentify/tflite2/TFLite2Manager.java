package cn.ltt.luck.numberidentify.tflite2;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import cn.ltt.luck.numberidentify.singlemodel.DistinguishResult;
import cn.ltt.luck.numberidentify.widget.LogUtil;
import cn.ltt.luck.numberidentify.widget.TimeUtil;

/**
 *  模型下载地址：https://www.kaggle.com/models/tensorflow/mobilenet-v2/tfLite/1-0-224
 */
public class TFLite2Manager {
    private static final String LOG_TAG = TFLite2Manager.class.getSimpleName();

    private static final String MODEL_NAME = "2.tflite";//"mnist.tflite";



    final float IMAGE_MEAN = 127.5f;
    final float IMAGE_STD = 127.5f;
    public static final int IMAGE_SIZE_X = 224;
    public static final int IMAGE_SIZE_Y = 224;
    final int DIM_BATCH_SIZE = 1;
    final int DIM_PIXEL_SIZE = 3;
    final int NUM_BYTES_PER_CHANNEL = 4;
    final int NUM_CLASS = 1001;


    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter mInterpreter;
    private final ByteBuffer mImageData;
    private final float[][] mResult = new float[1][NUM_CLASS];

    public TFLite2Manager(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);
        mImageData = ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * IMAGE_SIZE_X
                                * IMAGE_SIZE_Y
                                * DIM_PIXEL_SIZE
                                * NUM_BYTES_PER_CHANNEL);
    }

    public TF2DistinguishResult classify(Bitmap bitmap) {
        LogUtil.d("identify 开始时间：" + TimeUtil.Companion.getNowTime());

        convertBitmapToByteBuffer(bitmap);
        long startTime = SystemClock.uptimeMillis();
        mInterpreter.run(mImageData, mResult);
        long endTime = SystemClock.uptimeMillis();
        long timeCost = endTime - startTime;
        LogUtil.d("classify(): result = " + Arrays.toString(mResult[0])
                + ", timeCost = " + timeCost);
        LogUtil.d("identify 结束时间：" + TimeUtil.Companion.getNowTime());
        return new TF2DistinguishResult(mResult[0], timeCost);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (mImageData == null) {
            return;
        }
        mImageData.rewind();

        int[] intValues = new int[IMAGE_SIZE_X * IMAGE_SIZE_Y];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE_X; ++i) {
            for (int j = 0; j < IMAGE_SIZE_Y; ++j) {
                int pixelValue = intValues[pixel++];
                mImageData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                mImageData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                mImageData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }
    }

}
