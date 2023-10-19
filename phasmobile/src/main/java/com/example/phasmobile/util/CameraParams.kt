package com.example.phasmobile.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.phasmobile.R
import org.opencv.core.Mat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


private const val CAMERA_MATRIX_ROWS = 3
private const val CAMERA_MATRIX_COLS = 3
private const val DISTORTION_COEFFICIENTS_SIZE = 5
private const val FILE_CODE = 3092
private const val FILE_TYPE = "text/plain"
private const val FILE_NAME = "camera-params.txt"

private const val TAG = "CameraFrag"
public object CameraParams {
    var isLoaded = false
        private set

    fun fileExists(activity: Activity): Boolean {
        return File(activity.externalCacheDir, FILE_NAME).exists()
    }

    fun selectFile(activity: Activity) {
        val alert = AlertDialog.Builder(activity)
        alert.setTitle("Warning")
        alert.setMessage(activity.getString(R.string.error_camera_params))
        alert.setPositiveButton(
            "OK"
        ) { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = FILE_TYPE
            activity.startActivityForResult(intent, FILE_CODE)
        }
        alert.show()
    }

    fun copyFile(context: Context, source: Uri?): Boolean {
        return try {
            val target = File(context.externalCacheDir, FILE_NAME)
            val `in` = context.contentResolver.openInputStream(source!!)
            val out: OutputStream = FileOutputStream(target)
            var size = `in`!!.available()
            val buffer = ByteArray(size)
            while (`in`.read(buffer).also { size = it } > 0) {
                out.write(buffer, 0, size)
            }
            `in`.close()
            out.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun tryLoad(context: Context, cameraMatrix: Mat, distCoeffs: Mat): Boolean {
        val file = File(context.externalCacheDir, FILE_NAME)
        return try {
            val inputStream: InputStream = FileInputStream(file)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val params = buffer.toString(Charsets.UTF_8).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            var index = 0
            val length = params.size
            val cameraMatrixArray = DoubleArray(CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS)
            for (i in 0 until CAMERA_MATRIX_ROWS) {
                for (j in 0 until CAMERA_MATRIX_COLS) {
                    val id = i * CAMERA_MATRIX_ROWS + j
                    if (index < length) {
                        cameraMatrixArray[id] = params[index].toDouble()
                        index++
                    }
                }
            }
            cameraMatrix.put(0, 0, *cameraMatrixArray)
            val distortionCoefficientsArray = DoubleArray(DISTORTION_COEFFICIENTS_SIZE)
            val shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS
            for (i in shift until DISTORTION_COEFFICIENTS_SIZE + shift) {
                if (index < length) {
                    distortionCoefficientsArray[i - shift] = params[index].toDouble()
                    index++
                }
            }
            distCoeffs.put(0, 0, *distortionCoefficientsArray)
            Toast.makeText(
                context,
                context.getString(R.string.success_camera_params),
                Toast.LENGTH_SHORT
            ).show()
            isLoaded = true
            isLoaded
        } catch (e: Exception) {
            false
        }
    }

    fun onActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        intent: Intent,
        cameraMatrix: Mat,
        distCoeffs: Mat
    ): Boolean {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            val uri = intent.data
            var success = true
            success = success and copyFile(context, uri)
            Log.d(TAG, "Copied file success: $success")
            success = success and tryLoad(context, cameraMatrix, distCoeffs)
            Log.d(TAG, "Load file success: $success")
            return success
        }
        return false
    }
}
