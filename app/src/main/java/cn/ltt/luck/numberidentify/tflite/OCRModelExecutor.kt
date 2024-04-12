/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package cn.ltt.luck.numberidentify.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import cn.ltt.luck.numberidentify.widget.LogUtil
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Random
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils.bitmapToMat
import org.opencv.android.Utils.matToBitmap
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfRotatedRect
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Size
import org.opencv.dnn.Dnn.NMSBoxesRotated
import org.opencv.imgproc.Imgproc.boxPoints
import org.opencv.imgproc.Imgproc.getPerspectiveTransform
import org.opencv.imgproc.Imgproc.warpPerspective
import org.opencv.utils.Converters.vector_RotatedRect_to_Mat
import org.opencv.utils.Converters.vector_float_to_Mat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate

/**
 * 该类用于运行OCR模型。OCR过程分为两个阶段：
 * 1）使用EAST模型进行文本检测（https://tfhub.dev/sayakpaul/lite-model/east-text-detector/fp16/1）
 * 2）使用Keras OCR模型进行文本识别（https://tfhub.dev/tulasiram58827/lite-model/keras-ocr/float16/2）
 *
 * NMSBoxesRotated 函数的各个参数含义如下：
 *
 * boxes: 输入参数，一个 MatOfRotatedRect 类型的矩阵，表示检测到的旋转框（Rotated Rectangles）。
 * scores: 输入参数，一个 MatOfFloat 类型的矩阵，表示对应于每个旋转框的置信度分数。
 * scoreThreshold: 输入参数，浮点数类型，表示用于过滤低置信度旋转框的阈值。如果旋转框的置信度分数低于该阈值，则会被抑制。
 * nmsThreshold: 输入参数，浮点数类型，表示用于非极大值抑制的阈值。该阈值决定了两个旋转框之间的最小重叠度。如果两个旋转框的重叠度超过该阈值，则会进行抑制。
 * indices: 输出参数，一个 MatOfInt 类型的矩阵，表示经过非极大值抑制后保留的旋转框的索引。
 * 综合起来，NMSBoxesRotated 函数的作用是根据旋转框的置信度分数和重叠度阈值，对检测到的旋转框进行非极大值抑制，保留具有高置信度且不重叠的旋转框，并返回这些旋转框的索引。
 */
class OCRModelExecutor(context: Context, private var useGPU: Boolean = false) : AutoCloseable {
    private var gpuDelegate: GpuDelegate? = null

    private val recognitionResult: ByteBuffer
    private val detectionInterpreter: Interpreter
    private val recognitionInterpreter: Interpreter

    private var ratioHeight = 0.toFloat()
    private var ratioWidth = 0.toFloat()
    private var indicesMat: MatOfInt
    private var boundingBoxesMat: MatOfRotatedRect
    private var ocrResults: HashMap<String, Int>

    // 初始化代码块，加载模型和设置Interpreter
    init {
        // 加载OpenCV库并进行初始化
        try {
            if (!OpenCVLoader.initDebug()) throw Exception("Unable to load OpenCV")
            else Log.d(TAG, "OpenCV loaded")
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Log.d(TAG, exceptionLog)
        }
        // 加载文本检测模型和文本识别模型的Interpreter
        detectionInterpreter = getInterpreter(context, textDetectionModel, useGPU)
        // 识别模型需要Flex，因此无论用户选择如何，都禁用GPU委托
        recognitionInterpreter = getInterpreter(context, textRecognitionModel, false)

        recognitionResult = ByteBuffer.allocateDirect(recognitionModelOutputSize * 8)
        recognitionResult.order(ByteOrder.nativeOrder())
        indicesMat = MatOfInt()
        boundingBoxesMat = MatOfRotatedRect()
        ocrResults = HashMap<String, Int>()
    }

    fun execute(data: Bitmap): ModelExecutionResult {
        try {
            // 计算高度和宽度比例
            ratioHeight = data.height.toFloat() / detectionImageHeight
            ratioWidth = data.width.toFloat() / detectionImageWidth
            ocrResults.clear()
            // 第一阶段：检测文本

            detectTexts(data)
            // 第二阶段：识别文本

            val bitmapWithBoundingBoxes = recognizeTexts(data, boundingBoxesMat, indicesMat)

            return ModelExecutionResult(bitmapWithBoundingBoxes, "OCR result", ocrResults)
        } catch (e: Exception) {
            val exceptionLog = "something went wrong: ${e.message}"
            Log.d(TAG, exceptionLog)

            val emptyBitmap = ImageUtils.createEmptyBitmap(displayImageSize, displayImageSize)
            return ModelExecutionResult(emptyBitmap, exceptionLog, HashMap<String, Int>())
        }
    }

    /**
     * 文本检测阶段
     * @param data Bitmap
     */
    private fun detectTexts(data: Bitmap) {
        LogUtil.i(TAG, "detectTexts 开始：")

        // 转换图像为TensorImage以进行检测
        val detectionTensorImage =
            ImageUtils.bitmapToTensorImageForDetection(
                data,
                detectionImageWidth,
                detectionImageHeight,
                detectionImageMeans,
                detectionImageStds
            )
        // 准备输入和输出

        val detectionInputs = arrayOf(detectionTensorImage.buffer.rewind())
        val detectionOutputs: HashMap<Int, Any> = HashMap<Int, Any>()

        val detectionScores =
            Array(1) { Array(detectionOutputNumRows) { Array(detectionOutputNumCols) { FloatArray(1) } } }
        val detectionGeometries =
            Array(1) { Array(detectionOutputNumRows) { Array(detectionOutputNumCols) { FloatArray(5) } } }
        detectionOutputs.put(0, detectionScores)
        detectionOutputs.put(1, detectionGeometries)
        // 运行检测Interpreter

        detectionInterpreter.runForMultipleInputsOutputs(detectionInputs, detectionOutputs)

        val transposeddetectionScores =
            Array(1) { Array(1) { Array(detectionOutputNumRows) { FloatArray(detectionOutputNumCols) } } }
        val transposedDetectionGeometries =
            Array(1) { Array(5) { Array(detectionOutputNumRows) { FloatArray(detectionOutputNumCols) } } }

        // 转置检测输出张量
        for (i in 0 until transposeddetectionScores[0][0].size) {
            for (j in 0 until transposeddetectionScores[0][0][0].size) {
                for (k in 0 until 1) {
                    transposeddetectionScores[0][k][i][j] = detectionScores[0][i][j][k]
                }
                for (k in 0 until 5) {
                    transposedDetectionGeometries[0][k][i][j] = detectionGeometries[0][i][j][k]
                }
            }
        }
        // 非极大值抑制算法，用于过滤重叠的边界框
        val detectedRotatedRects = ArrayList<RotatedRect>()
        val detectedConfidences = ArrayList<Float>()

        for (y in 0 until transposeddetectionScores[0][0].size) {
            val detectionScoreData = transposeddetectionScores[0][0][y]
            val detectionGeometryX0Data = transposedDetectionGeometries[0][0][y]
            val detectionGeometryX1Data = transposedDetectionGeometries[0][1][y]
            val detectionGeometryX2Data = transposedDetectionGeometries[0][2][y]
            val detectionGeometryX3Data = transposedDetectionGeometries[0][3][y]
            val detectionRotationAngleData = transposedDetectionGeometries[0][4][y]

            for (x in 0 until transposeddetectionScores[0][0][0].size) {
                if (detectionScoreData[x] < 0) {
                    continue
                }

                // 计算旋转的边界框和限制（主要基于 OpenCV 示例）:
                // https://github.com/opencv/opencv/blob/master/samples/dnn/text_detection.py
                val offsetX = x * 4.0
                val offsetY = y * 4.0

                val h = detectionGeometryX0Data[x] + detectionGeometryX2Data[x]
                val w = detectionGeometryX1Data[x] + detectionGeometryX3Data[x]

                val angle = detectionRotationAngleData[x]
                val cos = Math.cos(angle.toDouble())
                val sin = Math.sin(angle.toDouble())

                val offset =
                    Point(
                        offsetX + cos * detectionGeometryX1Data[x] + sin * detectionGeometryX2Data[x],
                        offsetY - sin * detectionGeometryX1Data[x] + cos * detectionGeometryX2Data[x]
                    )
                val p1 = Point(-sin * h + offset.x, -cos * h + offset.y)
                val p3 = Point(-cos * w + offset.x, sin * w + offset.y)
                val center = Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y))

                val textDetection =
                    RotatedRect(
                        center,
                        Size(w.toDouble(), h.toDouble()),
                        (-1 * angle * 180.0 / Math.PI)
                    )
                detectedRotatedRects.add(textDetection)
                detectedConfidences.add(detectionScoreData[x])
            }
        }

        val detectedConfidencesMat = MatOfFloat(vector_float_to_Mat(detectedConfidences))

        boundingBoxesMat = MatOfRotatedRect(vector_RotatedRect_to_Mat(detectedRotatedRects))
        NMSBoxesRotated(
            boundingBoxesMat,
            detectedConfidencesMat,
            detectionConfidenceThreshold.toFloat(),
            detectionNMSThreshold.toFloat(),
            indicesMat
        )

        LogUtil.i(TAG, "detectTexts 结束：indicesMat=$indicesMat")
    }

    private fun recognizeTexts(
        data: Bitmap,
        boundingBoxesMat: MatOfRotatedRect,
        indicesMat: MatOfInt
    ): Bitmap {
        LogUtil.i(TAG, "recognizeTexts 开始")
        // 创建一个新的Bitmap用于在其上绘制文本框
        val bitmapWithBoundingBoxes = data.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapWithBoundingBoxes)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.toFloat()
        paint.setColor(Color.GREEN)
        // 对于每个检测到的文本边界框
        for (i in indicesMat.toArray()) {
            // 获取边界框
            val boundingBox = boundingBoxesMat.toArray()[i]
            // 创建目标顶点数组
            val targetVertices = ArrayList<Point>()
            targetVertices.add(Point(0.toDouble(), (recognitionImageHeight - 1).toDouble()))
            targetVertices.add(Point(0.toDouble(), 0.toDouble()))
            targetVertices.add(Point((recognitionImageWidth - 1).toDouble(), 0.toDouble()))
            targetVertices.add(
                Point(
                    (recognitionImageWidth - 1).toDouble(),
                    (recognitionImageHeight - 1).toDouble()
                )
            )
            // 获取边界框的四个顶点
            val srcVertices = ArrayList<Point>()

            val boundingBoxPointsMat = Mat()
            boxPoints(boundingBox, boundingBoxPointsMat)
            for (j in 0 until 4) {
                // 将边界框顶点转换为目标图像坐标系中的坐标
                srcVertices.add(
                    Point(
                        boundingBoxPointsMat.get(j, 0)[0] * ratioWidth,
                        boundingBoxPointsMat.get(j, 1)[0] * ratioHeight
                    )
                )
                // 绘制边界框线段
                if (j != 0) {
                    canvas.drawLine(
                        (boundingBoxPointsMat.get(j, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j, 1)[0] * ratioHeight).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 0)[0] * ratioWidth).toFloat(),
                        (boundingBoxPointsMat.get(j - 1, 1)[0] * ratioHeight).toFloat(),
                        paint
                    )
                }
            }
            // 绘制边界框的最后一条线段
            canvas.drawLine(
                (boundingBoxPointsMat.get(0, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(0, 1)[0] * ratioHeight).toFloat(),
                (boundingBoxPointsMat.get(3, 0)[0] * ratioWidth).toFloat(),
                (boundingBoxPointsMat.get(3, 1)[0] * ratioHeight).toFloat(),
                paint
            )
            // 计算透视变换矩阵
            val srcVerticesMat =
                MatOfPoint2f(srcVertices[0], srcVertices[1], srcVertices[2], srcVertices[3])
            val targetVerticesMat =
                MatOfPoint2f(
                    targetVertices[0],
                    targetVertices[1],
                    targetVertices[2],
                    targetVertices[3]
                )
            val rotationMatrix = getPerspectiveTransform(srcVerticesMat, targetVerticesMat)
            val recognitionBitmapMat = Mat()
            val srcBitmapMat = Mat()
            // 将Bitmap转换为Mat

            bitmapToMat(data, srcBitmapMat)
            // 进行透视变换

            warpPerspective(
                srcBitmapMat,
                recognitionBitmapMat,
                rotationMatrix,
                Size(recognitionImageWidth.toDouble(), recognitionImageHeight.toDouble())
            )
            // 创建用于识别的空白Bitmap
            val recognitionBitmap =
                ImageUtils.createEmptyBitmap(
                    recognitionImageWidth,
                    recognitionImageHeight,
                    0,
                    Bitmap.Config.ARGB_8888
                )
            // 将Mat转换为Bitmap
            matToBitmap(recognitionBitmapMat, recognitionBitmap)
            // 将识别用的Bitmap转换为TensorImage

            val recognitionTensorImage =
                ImageUtils.bitmapToTensorImageForRecognition(
                    recognitionBitmap,
                    recognitionImageWidth,
                    recognitionImageHeight,
                    recognitionImageMean,
                    recognitionImageStd
                )
            // 重置识别结果ByteBuffer

            recognitionResult.rewind()
            // 运行识别Interpreter

            recognitionInterpreter.run(recognitionTensorImage.buffer, recognitionResult)

            var recognizedText = ""
            // 解析识别结果并记录到OCR结果中
            for (k in 0 until recognitionModelOutputSize) {
                var alphabetIndex = recognitionResult.getInt(k * 8)
                if (alphabetIndex in alphabets.indices)
                    recognizedText += alphabets[alphabetIndex]
            }
            Log.d("Recognition result:", recognizedText)
            if (recognizedText != "") {
                ocrResults.put(recognizedText, getRandomColor())
            }
        }

        LogUtil.i(TAG, "recognizeTexts 结束")
        return bitmapWithBoundingBoxes
    }

    /**
     * 加载模型文件
     * https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/java/demo/app/src/main/java/com/example/android/tflitecamerademo/ImageClassifier.java
     * @param context Context
     * @param modelFile String
     * @return MappedByteBuffer
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }

    @Throws(IOException::class)
    private fun getInterpreter(
        context: Context,
        modelName: String,
        useGpu: Boolean = false
    ): Interpreter {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(numberThreads)

        gpuDelegate = null
        if (useGpu) {
            gpuDelegate = GpuDelegate()
            tfliteOptions.addDelegate(gpuDelegate)
        }

        return Interpreter(loadModelFile(context, modelName), tfliteOptions)
    }

    override fun close() {
        detectionInterpreter.close()
        recognitionInterpreter.close()
        if (gpuDelegate != null) {
            gpuDelegate!!.close()
        }
    }

    private fun getRandomColor(): Int {
        val random = Random()
        return Color.argb(
            (128),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt(),
            (255 * random.nextFloat()).toInt()
        )
    }

    companion object {
        public const val TAG = "TfLiteOCRDemo"
        private const val textDetectionModel = "text_detection.tflite"
        private const val textRecognitionModel = "text_recognition.tflite"
        private const val numberThreads = 4
        private const val alphabets = "0123456789abcdefghijklmnopqrstuvwxyz"
        private const val displayImageSize = 257
        const val detectionImageHeight = 320
        const val detectionImageWidth = 320
        private val detectionImageMeans =
            floatArrayOf(103.94.toFloat(), 116.78.toFloat(), 123.68.toFloat())
        private val detectionImageStds = floatArrayOf(1.toFloat(), 1.toFloat(), 1.toFloat())
        private val detectionOutputNumRows = 80
        private val detectionOutputNumCols = 80
        private val detectionConfidenceThreshold = 0.5
        private val detectionNMSThreshold = 0.4
        private const val recognitionImageHeight = 31
        private const val recognitionImageWidth = 200
        private const val recognitionImageMean = 0.toFloat()
        private const val recognitionImageStd = 255.toFloat()
        private const val recognitionModelOutputSize = 48
    }
}
