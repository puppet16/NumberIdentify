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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat.RGB_565
import android.graphics.Matrix
import java.io.File
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp

/** Collection of image reading and manipulation utilities in the form of static functions. */
abstract class ImageUtils {
  companion object {



    /**
     * Convert a bitmap to a TensorImage for the recognition model with target size and
     * normalization
     *
     * @param bitmapIn
     * - the bitmap to convert
     * @param width
     * - target width of the converted TensorImage
     * @param height
     * - target height of the converted TensorImage
     * @param means
     * - means of the images
     * @param stds
     * - stds of the images
     */
    fun bitmapToTensorImageForRecognition(
      bitmapIn: Bitmap,
      width: Int,
      height: Int,
      mean: Float,
      std: Float
    ): TensorImage {
      val imageProcessor =
        ImageProcessor.Builder()
          .add(ResizeOp(height, width, ResizeOp.ResizeMethod.BILINEAR))
          .add(TransformToGrayscaleOp())
          .add(NormalizeOp(mean, std))
          .build()
      var tensorImage = TensorImage(DataType.FLOAT32)

      tensorImage.load(bitmapIn)
      tensorImage = imageProcessor.process(tensorImage)

      return tensorImage
    }

    /**
     * Convert a bitmap to a TensorImage for the detection model with target size and normalization
     *
     * @param bitmapIn
     * - the bitmap to convert
     * @param width
     * - target width of the converted TensorImage
     * @param height
     * - target height of the converted TensorImage
     * @param means
     * - means of the images
     * @param stds
     * - stds of the images
     */
    fun bitmapToTensorImageForDetection(
      bitmapIn: Bitmap,
      width: Int,
      height: Int,
      means: FloatArray,
      stds: FloatArray
    ): TensorImage {
      val imageProcessor =
        ImageProcessor.Builder()
          .add(ResizeOp(height, width, ResizeOp.ResizeMethod.BILINEAR))
          .add(NormalizeOp(means, stds))
          .build()
      var tensorImage = TensorImage(DataType.FLOAT32)

      tensorImage.load(bitmapIn)
      tensorImage = imageProcessor.process(tensorImage)

      return tensorImage
    }

    fun createEmptyBitmap(
      imageWidth: Int,
      imageHeigth: Int,
      color: Int = 0,
      imageConfig: Bitmap.Config = Bitmap.Config.RGB_565
    ): Bitmap {
      val ret = Bitmap.createBitmap(imageWidth, imageHeigth, imageConfig)
      if (color != 0) {
        ret.eraseColor(color)
      }
      return ret
    }
  }
}
