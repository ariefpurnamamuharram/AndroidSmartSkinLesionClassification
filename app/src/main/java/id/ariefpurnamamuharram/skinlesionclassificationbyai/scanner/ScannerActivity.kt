package id.ariefpurnamamuharram.skinlesionclassificationbyai.scanner

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.wonderkiln.camerakit.CameraKitImage
import id.ariefpurnamamuharram.skinlesionclassificationbyai.R
import id.ariefpurnamamuharram.skinlesionclassificationbyai.tensorflow.Classifier
import id.ariefpurnamamuharram.skinlesionclassificationbyai.tensorflow.TensorFlowImageClassifier
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class ScannerActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "ScannerActivity"
        private const val INPUT_WIDTH = 300
        private const val INPUT_HEIGHT = 300
        private const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128f
        private const val INPUT_NAME = "Mul"
        private const val OUTPUT_NAME = "final_result"
        private const val MODEL_FILE = ""
        private const val LABEL_FILE = ""
    }

    private var classifier: Classifier? = null
    private var initializeJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        initializeTensorClassifier()
        btnTakePicture.setOnClickListener { _ ->
            setVisibilityOnCaptured(false)
            camera.captureImage {
                onImageCaptured(it)
            }
        }
    }

    private fun onImageCaptured(it: CameraKitImage) {
        val bitmap = Bitmap.createScaledBitmap(it.bitmap, INPUT_WIDTH, INPUT_HEIGHT, false)
        showCapturedImage(bitmap)
        classifier?.let {
            try {
                showRecognizedResult(it.recognizeImage(bitmap))
            } catch (e: java.lang.RuntimeException) {
                Log.e(TAG, "Crashing due to classification.closed() before the recognizer finishes!")
            }
        }
    }

    private fun showRecognizedResult(results: MutableList<Classifier.Recognition>) {
        runOnUiThread {
            setVisibilityOnCaptured(true)
            if (results.isEmpty()) {
                textSkinLesion.text = getString(R.string.nothing_found)
                textLevelOfConfidence.text = getString(R.string.none)
            } else {
                val skinLesion = results[0].title
                val confidence = results[0].confidence
                textSkinLesion.text = skinLesion
                textLevelOfConfidence.text = confidence.toString()
            }
        }
    }

    private fun showCapturedImage(bitmap: Bitmap?) {
        runOnUiThread {
            imageCaptured.visibility = View.VISIBLE
            imageCaptured.setImageBitmap(bitmap)
        }
    }

    private fun setVisibilityOnCaptured(isDone: Boolean) {
        btnTakePicture.isEnabled = isDone
        if (isDone) {
            imageCaptured.visibility = View.VISIBLE
            textSkinLesion.visibility = View.VISIBLE
            textLevelOfConfidence.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        } else {
            imageCaptured.visibility = View.GONE
            textSkinLesion.visibility = View.GONE
            textLevelOfConfidence.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun initializeTensorClassifier() {
        initializeJob = launch {
            try {
                classifier = TensorFlowImageClassifier.create(
                    assets, MODEL_FILE, LABEL_FILE, INPUT_WIDTH, INPUT_HEIGHT,
                    IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME
                )
                runOnUiThread {
                    btnTakePicture.isEnabled = true
                }
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }

    private fun clearTensorClassifier() {
        initializeJob?.cancel()
        classifier?.close()
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        super.onPause()
        camera.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearTensorClassifier()
    }
}