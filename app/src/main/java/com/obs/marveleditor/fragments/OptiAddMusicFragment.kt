/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import com.obs.marveleditor.utils.OptiConstant
import com.obs.marveleditor.R
import com.obs.marveleditor.interfaces.OptiFFMpegCallback
import com.obs.marveleditor.utils.VideoUtils.buildMediaSource
import com.obs.marveleditor.utils.VideoUtils.secToTime
import com.obs.marveleditor.utils.VideoFrom
import com.obs.marveleditor.interfaces.OptiDialogueHelper
import com.github.guilhe.views.SeekBarRangedView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.obs.marveleditor.OptiVideoEditor
import com.obs.marveleditor.utils.OptiCommonMethods
import com.obs.marveleditor.utils.OptiUtils
import java.io.File
import kotlin.math.roundToLong

class OptiAddMusicFragment : OptiBaseCreatorDialogFragment(), OptiDialogueHelper, OptiFFMpegCallback {

    private var tagName: String = OptiAddMusicFragment::class.java.simpleName
    private var audioFile: File? = null
    private var videoFile: File? = null
    private var playWhenReady: Boolean? = false
    private var exoPlayer: SimpleExoPlayer? = null
    private var sbrvVideoTrim: SeekBarRangedView? = null
    private var acbCrop: AppCompatButton? = null
    private var actvStartTime: AppCompatTextView? = null
    private var actvEndTime: AppCompatTextView? = null
    private var ePlayer: PlayerView? = null
    private var acivClose: AppCompatImageView? = null
    private var helper: CallBacks? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playPause: Boolean = false
    private var durationSet: Boolean = false
    private var flLoadingView: FrameLayout? = null
    private var pbLoading: ProgressBar? = null
    private var tvSelectedAudio: AppCompatTextView? = null
    private var ivRadio: AppCompatImageView? = null
    private var masterAudioFile: File? = null
    private var tvSubTitle: AppCompatTextView? = null
    private var nextAction: Int = 1
    private var seekToValue: Long = 0L
    private var minSeekValue: Float = 0F
    private var maxSeekValue: Float = 0F
    private var mContext: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val inflate = inflater.inflate(R.layout.opti_add_music_includer, container, false)
        initView(inflate)
        return inflate
    }

    private fun initView(inflate: View?) {

        flLoadingView = inflate?.findViewById(R.id.flLoadingView)
        acbCrop = inflate?.findViewById(R.id.acbCrop)
        pbLoading = inflate?.findViewById(R.id.pbLoading)
        acivClose = inflate?.findViewById(R.id.acivClose)
        ePlayer = inflate?.findViewById(R.id.ePlayer)
        sbrvVideoTrim = inflate?.findViewById(R.id.sbrvVideoTrim)
        actvStartTime = inflate?.findViewById(R.id.actvStartTime)
        actvEndTime = inflate?.findViewById(R.id.actvEndTime)
        tvSelectedAudio = inflate?.findViewById(R.id.tv_selected_audio)
        ivRadio = inflate?.findViewById(R.id.ivRadio)
        tvSubTitle = inflate?.findViewById(R.id.tvSubTitle)

        flLoadingView?.visibility = View.GONE

        mContext = context

        acivClose?.setOnClickListener {
            dialog?.dismiss()
            helper?.onDidNothing()
        }

        ivRadio?.setOnClickListener {
            checkPermission(OptiConstant.AUDIO_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        tvSelectedAudio?.setOnClickListener {
            checkPermission(OptiConstant.AUDIO_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val callback = object : SeekBarRangedView.SeekBarRangedChangeCallback {

            override fun onChanged(minValue: Float, maxValue: Float) {
                exoPlayer?.seekTo(minValue.toLong())
            }

            override fun onChanging(minValue: Float, maxValue: Float) {
                minSeekValue = minValue
                maxSeekValue = maxValue
                actvStartTime?.text = secToTime(minValue.toLong())
                actvEndTime?.text = secToTime(maxValue.toLong())
            }
        }

        sbrvVideoTrim?.actionCallback = callback

        setControls(false)
    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer?.currentPosition!!
            currentWindow = exoPlayer?.currentWindowIndex!!
            playWhenReady = exoPlayer?.playWhenReady
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    private fun initializePlayer() {
        exoPlayer = activity?.let {
            ExoPlayerFactory.newSimpleInstance(
                it, DefaultRenderersFactory(activity!!),
                DefaultTrackSelector(), DefaultLoadControl()
            )
        }

        ePlayer?.player = exoPlayer

        exoPlayer?.playWhenReady = false

        exoPlayer?.addListener(playerListener)

        buildMediaSource(Uri.fromFile(masterAudioFile), VideoFrom.LOCAL)?.let {
            exoPlayer?.prepare(
                it
            )
        }

        exoPlayer?.seekTo(0)

        exoPlayer?.seekTo(currentWindow, playbackPosition)

        acbCrop?.setOnClickListener {

            stopRunningProcess()

            if (!isRunning()) {
                val trimDuration = maxSeekValue - minSeekValue
                Log.v(tagName, "seekToValue: $seekToValue")
                Log.v(tagName, "maxValue: $maxSeekValue")
                Log.v(tagName, "minValue: $minSeekValue")
                Log.v(tagName, "trimDuration: $trimDuration")
                val convertedSeekValue = OptiCommonMethods.convertDuration(seekToValue)
                Log.v(tagName, "convertedSeekValue: $convertedSeekValue")
                /*val trimDurationLong = OptiCommonMethods.convertDurationInSec(trimDuration.roundToLong())
                Log.v(tagName, "trimDurationLong: $trimDurationLong")*/

                if (trimDuration.roundToLong() >= seekToValue) {
                    Toast.makeText(activity, "Please trim audio under $convertedSeekValue.", Toast.LENGTH_SHORT).show()
                } else {
                    //output file is generated and send to video processing
                    val outputFile = OptiUtils.createAudioFile(context!!)
                    Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

                    nextAction = 1

                    OptiVideoEditor.with(context!!)
                        .setType(OptiConstant.AUDIO_TRIM)
                        .setAudioFile(masterAudioFile!!)
                        .setOutputPath(outputFile.absolutePath)
                        .setStartTime(actvStartTime?.text.toString())
                        .setEndTime(actvEndTime?.text.toString())
                        .setCallback(this)
                        .main()
                }
            } else {
                showInProgressToast()
            }
        }
    }

    private val playerListener = object : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            //no implementation
        }

        override fun onSeekProcessed() {
            //no implementation
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            //no implementation
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            //no implementation
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            pbLoading?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        override fun onPositionDiscontinuity(reason: Int) {
            //no implementation
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            //no implementation
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            //no implementation
        }

        /*override fun onTimelineChanged(timeline: Timeline, reason: Int) {

        }*/

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            if (!playWhenReady && playbackState == Player.STATE_READY) {

                if (playWhenReady) {
                    playPause = true
                    activity?.let {
                    }
                } else {
                    playPause = true
                    activity?.let {
                    }
                }

                if (!durationSet) {
                    durationSet = true
                    val duration = exoPlayer?.duration
                    sbrvVideoTrim?.setMaxValue(duration?.toFloat()!!)
                    actvStartTime?.text = secToTime(0)
                    actvEndTime?.text = duration?.let { secToTime(it) }
                    //set min & max seek value for audio trimming
                    minSeekValue = 0F
                    maxSeekValue = duration?.toFloat() ?: 0F
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun setMode(mode: Int) {
        //no implementation
    }

    override fun setHelper(helper: CallBacks) {
        this.helper = helper
    }

    override fun setDuration(duration: Long) {
        this.seekToValue = duration
    }

    override fun dismiss() {
        //no implementation
    }

    override fun permissionsBlocked() {
        //no implementation
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    companion object {
        fun newInstance() = OptiAddMusicFragment()
    }

    fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            OptiConstant.AUDIO_GALLERY -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        //Toast.makeText(this@EditProfileActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //call the gallery intent
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, OptiConstant.AUDIO_GALLERY)

                        } else {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
                            intent.data = uri
                            startActivityForResult(intent, 300)
                        }
                    }
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) return

        when (requestCode) {
            OptiConstant.AUDIO_GALLERY -> {
                data?.let {
                    setFilePath(resultCode, it, OptiConstant.AUDIO_GALLERY)
                }
            }
        }
    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val selectedImage = data.data

                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if (mode == OptiConstant.AUDIO_GALLERY) {
                        masterAudioFile = File(filePath)
                        masterAudioFile?.let { file ->
                            tvSelectedAudio!!.text = masterAudioFile!!.name.toString()
                            //setFilePathFromSource(file)
                            setControls(true)

                            if (Util.SDK_INT <= 23 || exoPlayer == null) {
                                initializePlayer()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.v(tagName, "Exception: " + e.localizedMessage)
            }
        }
    }

    private fun setControls(isShow: Boolean) {
        if (isShow) {
            tvSubTitle!!.visibility = View.VISIBLE
            sbrvVideoTrim!!.visibility = View.VISIBLE
            actvStartTime!!.visibility = View.VISIBLE
            actvEndTime!!.visibility = View.VISIBLE
            acbCrop!!.visibility = View.VISIBLE
        } else {
            tvSubTitle!!.visibility = View.INVISIBLE
            sbrvVideoTrim!!.visibility = View.INVISIBLE
            actvStartTime!!.visibility = View.INVISIBLE
            actvEndTime!!.visibility = View.INVISIBLE
            acbCrop!!.visibility = View.INVISIBLE
        }
    }

    override fun setFilePathFromSource(file: File) {
        videoFile = file
        Log.d("getMimeType = ", "" + getMimeType(videoFile!!.absolutePath))
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
        if (nextAction == 1) {
            flLoadingView?.visibility = View.VISIBLE
            acbCrop?.visibility = View.GONE
        }
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        if (nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
            audioFile = convertedFile
        } else {
            helper?.showLoading(false)
            helper?.onFileProcessed(convertedFile)
        }
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_LONG).show()
        if (nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
        } else {
            helper?.showLoading(false)
        }
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        if (nextAction == 1) {
            flLoadingView?.visibility = View.GONE
            acbCrop?.visibility = View.VISIBLE
            releasePlayer()
            muxVideoPlayer()
        } else {
            helper?.showLoading(false)
            dialog?.dismiss()
        }
    }

    private fun muxVideoPlayer() {

        stopRunningProcess()

        if (!isRunning()) {
            //output file is generated and send to video processing
            val outputFile = OptiUtils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            nextAction = 2

            OptiVideoEditor.with(context!!)
                .setType(OptiConstant.VIDEO_AUDIO_MERGE)
                .setFile(videoFile!!)
                .setAudioFile(audioFile!!)
                .setOutputPath(outputFile.path)
                .setCallback(this)
                .main()

            helper?.showLoading(true)
        } else {
            showInProgressToast()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        releasePlayer()
    }
}