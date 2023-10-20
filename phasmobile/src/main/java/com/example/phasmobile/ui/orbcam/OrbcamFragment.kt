package com.example.phasmobile.ui.orbcam

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.phasmobile.R
import com.example.phasmobile.ui.view.PortraitCameraView
import com.example.phasmobile.util.MainViewModel
import kotlinx.coroutines.launch
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.time.Duration
import java.time.Instant
import kotlin.math.sign
import kotlin.random.Random


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TAG = "Camera"

/**
 * A simple [Fragment] subclass.
 * Use the [OrbcamFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val MAX_X = 1080;
private const val MAX_Y = 1920;
const val SPEED = 3
class OrbcamFragment : Fragment(), CvCameraViewListener2 {

    var orbImg = Mat()
    val outBuf = Mat()
    val orb = Orb()
    var orbsVisible = false
    val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val res = context?.resources
        val orbBmp = BitmapFactory.decodeResource(res, R.drawable.gorb);
        Utils.bitmapToMat(orbBmp, orbImg)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orbcam, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layout = view.findViewById<LinearLayout>(R.id.llOrbcam)



        val camera = PortraitCameraView(context?.applicationContext, 0)
        camera.visibility = SurfaceView.VISIBLE
        camera.setCvCameraViewListener(this)
        camera.enableView()

        layout.addView(camera)

        Log.i(TAG, "Added $camera to $layout")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OrbcamFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            OrbcamFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(TAG, "Camera view started")
    }

    override fun onCameraViewStopped() {
        Log.i(TAG, "Camera view stopped")
    }

    fun floatOrbs() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        if (inputFrame == null) {
            return outBuf
        }

        inputFrame.rgba().copyTo(outBuf);

        if (viewModel.canSeeOrbs()) {
            overlayOrbs();
        }

        Imgproc.cvtColor(outBuf, outBuf, Imgproc.COLOR_RGBA2GRAY)

        return outBuf
    }

    private fun overlayOrbs() {
        val alpha = 0.5;
        orb.update()

        if (orb.isPaused()) return

        val srcRect = Rect(Point(0.0,0.0), orbImg.size())
        val destRect = Rect(Point(orb.x.toDouble(), orb.y.toDouble()), srcRect.size())

        if (destRect.x + destRect.width < outBuf.width()
            && destRect.y + destRect.height < outBuf.height()) {
            val outRoi = outBuf.submat(destRect)
            Core.add(outRoi, orbImg, outRoi)
//                Core.addWeighted(outRoi, alpha, orbImg, 1-alpha,0.2, outRoi)
            outRoi.release()
        }
    }
}

class Orb() {
    var x = Random.nextInt(0, MAX_X);
    var y = Random.nextInt(0, MAX_Y);
    var dx: Int
    var dy: Int

    var pauseTime: Instant? = null;

    init {
        if(Random.nextBoolean()) {
            dx = 1
        }
        else {
            dx = -1
        }

        if (Random.nextBoolean()) {
            dy = 1
        }
        else {
            dy = -1
        }
    }

    fun update() {
        if (pauseTime != null) {
            val duration = Duration.between(pauseTime, Instant.now()).seconds
            if (duration > 4) {
                pauseTime = null
                x = Random.nextInt(0, MAX_X);
                y = Random.nextInt(0, MAX_Y);

            }
            return
        }

        val xPrime = x + dx * SPEED
        if (xPrime > MAX_X || xPrime < 0) {
            pauseTime = Instant.now()
        }
        else {
            x = xPrime
        }

        val yPrime = y + dy * SPEED
        if (yPrime > MAX_Y || yPrime < 0) {
            pauseTime = Instant.now()
        }
        else {
            y = yPrime
        }

//        Log.d(TAG, "Location update: $x, $y")
    }

    fun isPaused(): Boolean {
        return pauseTime != null;
    }
}
