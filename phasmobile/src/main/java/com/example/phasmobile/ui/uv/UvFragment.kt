package com.example.phasmobile.ui.uv

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.phasmobile.MainActivity
import com.example.phasmobile.R
import com.example.phasmobile.ui.view.PortraitCameraView
import com.example.phasmobile.util.CameraParams
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.aruco.Aruco
import org.opencv.aruco.DetectorParameters
import org.opencv.aruco.Dictionary
import org.opencv.calib3d.Calib3d
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f
import org.opencv.core.Point
import org.opencv.core.Point3
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.LinkedList

private const val TAG = "CameraFrag"
private const val UV_RADIUS = 400
private const val SIZE = 0.04f;

class UvFragment : Fragment(), CvCameraViewListener2 {

    private lateinit var camera: PortraitCameraView

    private lateinit var params: DetectorParameters
    private lateinit var dictionary: Dictionary

    private var circleImg = Mat()
    private var handImg = Mat()
    private var outBuf = Mat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val res = context?.resources
        val handBmp = BitmapFactory.decodeResource(res, R.drawable.ghosty)
        Utils.bitmapToMat(handBmp, handImg)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_uv, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        else Toast.makeText(
            context,
            getString(R.string.error_native_lib),
            Toast.LENGTH_LONG
        ).show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layout = view.findViewById<LinearLayout>(R.id.llUv)

        camera = PortraitCameraView(context?.applicationContext, 0)
        camera.visibility = SurfaceView.VISIBLE
        camera.setCvCameraViewListener(this)
        camera.enableView()

        layout.addView(camera)

    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        params = DetectorParameters.create()
        dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50)

        // Create UV circle
        circleImg = Mat.zeros(height, width, CvType.CV_8UC4)

        val empty = circleImg.clone()

        val center = Point(width / 2.0, height / 2.0)
        val start = UV_RADIUS
        val end = UV_RADIUS - 50
        for (r in start downTo end) {
            val percent = ((r - end).toDouble() / (start - end).toDouble())
            val alpha = (1.0 - percent)

            val img = empty.clone();
            val fill = if (r == end) Core.FILLED else Core.BORDER_CONSTANT;
            Imgproc.circle(
                img,
                center,
                r,
                Scalar(95.0, 75.0, 139.0, 255.0),
                fill
            )

            Core.addWeighted(img, alpha, circleImg, 1.0, 0.0, circleImg)
        }
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        if (!CameraParams.isLoaded || inputFrame == null) {
            return Mat()
        }

        outBuf = inputFrame.rgba()
        drawMarkers()
        overlayUv()

        return outBuf
    }

    private fun overlayUv() {
        Core.add(outBuf, circleImg, outBuf)
    }

    // Making these class members for memory reasons
    val rgb = Mat()
    val gray = Mat()
    val rvecs = Mat()
    val tvecs = Mat()

    private fun drawMarkers() {
        if (outBuf.empty()) return

        val corners: List<Mat> = LinkedList()
        val ids = MatOfInt()

        Imgproc.cvtColor(outBuf, rgb, Imgproc.COLOR_RGBA2RGB)

        Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_RGB2GRAY)

        Aruco.detectMarkers(gray, dictionary, corners, ids, params)

        if (corners.isNotEmpty()) {
//            Aruco.drawDetectedMarkers(rgb, corners, ids);

            Aruco.estimatePoseSingleMarkers(
                corners,
                SIZE,
                MainActivity.CameraData.cameraMat,
                MainActivity.CameraData.distCoeffs,
                rvecs,
                tvecs
            )

            for (i in 0..<ids.toArray().size) {
                Log.d(TAG, "Drawing hand")
                drawHandprint(rvecs.row(i), tvecs.row(i))
            }
        }

//        Imgproc.cvtColor(rgb, outBuf, Imgproc.COLOR_RGB2RGBA);
    }

    // Making these class members for memory reasons
    val squareMat = MatOfPoint3f()
    val projectedPoints2f = MatOfPoint2f()
    val srcPoints = MatOfPoint2f()
    val projectedPoints = MatOfPoint()
    val warpImage = Mat()
    var poly_mask = Mat()
    var circle_mask = Mat()
    var mask = Mat()
    private fun drawHandprint(rvec: Mat, tvec: Mat) {
        val hs = SIZE / 2.0

        squareMat.fromList(
            listOf(
                Point3(-hs, -hs, 0.0),
                Point3(-hs, hs, 0.0),
                Point3(hs, hs, 0.0),
                Point3(hs, -hs, 0.0),
            )
        )

        Calib3d.projectPoints(
            squareMat,
            rvec,
            tvec,
            MainActivity.CameraData.cameraMat,
            MainActivity.CameraData.distCoeffs,
            projectedPoints2f
        )

        val w = handImg.width().toDouble()
        val h = handImg.height().toDouble()
        srcPoints.fromList(
            listOf(
                Point(0.0, 0.0),
                Point(w, 0.0),
                Point(w, h),
                Point(0.0, h),
            )
        )

        projectedPoints.fromList(projectedPoints2f.toList())

        val homography = Calib3d.findHomography(srcPoints, projectedPoints2f)
        Imgproc.warpPerspective(handImg, warpImage, homography, outBuf.size())


        poly_mask = Mat.zeros(outBuf.size(), CvType.CV_8U)
        Imgproc.fillConvexPoly(poly_mask, projectedPoints, Scalar(1.0))

        circle_mask = Mat.zeros(outBuf.size(), CvType.CV_8U)
        val center = Point(circle_mask.width() / 2.0, circle_mask.height() / 2.0)
        Imgproc.circle(circle_mask, center, UV_RADIUS, Scalar(1.0), Core.FILLED)

        Core.bitwise_and(poly_mask, circle_mask, mask)

        Log.d(TAG, "${projectedPoints.toList()}")
        Core.bitwise_and(warpImage, warpImage, outBuf, mask)

//        warpImage.release()
//        mask.release()
//        homography.release()
//        projectedPoints.release()
//        projectedPoints2f.release()
//        srcPoints.release()
//        squareMat.release()
    }


    private val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            val activity = activity as Activity
            Log.d(TAG, "activity $activity")
            if (status == SUCCESS) {
                MainActivity.CameraData.cameraMat = Mat.eye(3, 3, CvType.CV_64FC1)
                MainActivity.CameraData.distCoeffs = MatOfDouble(Mat.zeros(5, 1, CvType.CV_64FC1))
                if (CameraParams.fileExists(activity)) {
                    CameraParams.tryLoad(
                        activity,
                        MainActivity.CameraData.cameraMat,
                        MainActivity.CameraData.distCoeffs
                    )
                } else {
                    CameraParams.selectFile(activity)
                }
                camera.enableView()
            } else {
                super.onManagerConnected(status)
            }
        }
    }
}