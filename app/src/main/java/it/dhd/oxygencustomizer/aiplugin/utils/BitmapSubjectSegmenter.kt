package it.dhd.oxygencustomizer.aiplugin.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import it.dhd.oxygencustomizer.aiplugin.interfaces.SegmenterResultListener
import it.dhd.oxygencustomizer.aiplugin.remover.RemoveBg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BitmapSubjectSegmenter(mContext: Context) {

    private val mBgRemover: RemoveBg = RemoveBg(mContext)

    private suspend fun segmentSubject(
        inputBitmap: Bitmap,
        listener: SegmenterResultListener
    ) {
        mBgRemover.clearBackground(inputBitmap).collect { output ->
            listener.onSegmentationResult(output)
        }
    }

    fun segmentSubjectFromJava(inputBitmap: Bitmap, listener: SegmenterResultListener) {
        Log.d("BitmapSubjectSegmenter", "segmentSubjectFromJava started")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                segmentSubject(inputBitmap, listener)
            } catch (e: Exception) {
                listener.onSegmentationError(e)
            }
        }
    }
}
