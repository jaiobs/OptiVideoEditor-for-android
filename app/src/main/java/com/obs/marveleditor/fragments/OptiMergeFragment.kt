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
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.obs.marveleditor.utils.OptiConstant
import com.obs.marveleditor.R
import com.obs.marveleditor.utils.OptiUtils
import com.obs.marveleditor.interfaces.OptiDialogueHelper
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.obs.marveleditor.OptiVideoEditor
import com.obs.marveleditor.interfaces.OptiFFMpegCallback
import com.obs.marveleditor.utils.OptiCommonMethods
import java.io.File
import java.io.FileWriter

class OptiMergeFragment : BottomSheetDialogFragment(), OptiDialogueHelper, OptiFFMpegCallback {

    private var tagName: String = OptiMergeFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var ivClose: AppCompatImageView
    private lateinit var ivDone: AppCompatImageView
    private lateinit var ivVideoOne: SimpleDraweeView
    private lateinit var ivVideoTwo: SimpleDraweeView
    private var videoFileOne: File? = null
    private var videoFileTwo: File? = null
    private var bmThumbnailOne: Bitmap? = null
    private var bmThumbnailTwo: Bitmap? = null
    private var helper: OptiBaseCreatorDialogFragment.CallBacks? = null
    private var mContext: Context? = null
    private var isMP4: Boolean? = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.opti_fragment_merge_dialog, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        ivVideoOne = rootView.findViewById(R.id.iv_video_one)
        ivVideoTwo = rootView.findViewById(R.id.iv_video_two)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {

            if (videoFileOne != null && videoFileTwo != null) {
                dismiss()

                val metaRetriever = MediaMetadataRetriever()
                metaRetriever.setDataSource(videoFileOne!!.absolutePath)
                val height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                Log.v(tagName, "height: $height, width: $width")

                //create text file for concat demuxer
                val txtFile = OptiUtils.createTextFile(context!!)
                Log.v(tagName, "txtFile: ${txtFile.absolutePath}")

                val writer = FileWriter(txtFile)
                writer.append("file '" + videoFileOne!!.absolutePath + "'")
                writer.append("\n")
                writer.append("file '" + videoFileTwo!!.absolutePath + "'")
                writer.flush()
                writer.close()

                val extensionOne = OptiCommonMethods.getFileExtension(videoFileOne!!.absolutePath)
                val extensionTwo = OptiCommonMethods.getFileExtension(videoFileTwo!!.absolutePath)
                Log.v(tagName, "extensionOne: $extensionOne, extensionTwo: $extensionTwo")

                isMP4 = extensionOne == OptiConstant.VIDEO_FORMAT && extensionTwo == OptiConstant.VIDEO_FORMAT

                //output file is generated and send to video processing
                val outputFile = OptiUtils.createVideoFile(context!!)
                Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

                OptiVideoEditor.with(context!!)
                    .setType(OptiConstant.MERGE_VIDEO)
                    .setFile(videoFileOne!!)
                    .setFileTwo(videoFileTwo!!)
                    .setTextFile(txtFile, isMP4!!)
                    .setOutputPath(outputFile.path)
                    .setFileWidthHeight(width!!, height!!)
                    .setCallback(this)
                    .main()

                helper?.showLoading(true)
            } else {
                OptiUtils.showGlideToast(activity!!, getString(R.string.error_merge))
            }
        }

        ivVideoOne.setOnClickListener {
            checkPermission(OptiConstant.VIDEO_MERGE_1, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ivVideoTwo.setOnClickListener {
            checkPermission(OptiConstant.VIDEO_MERGE_2, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun setMode(mode: Int) {

    }

    override fun setFilePathFromSource(file: File) {

    }

    override fun setHelper(helper: OptiBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    override fun setDuration(duration: Long) {

    }

    fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            OptiConstant.VIDEO_MERGE_1 -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if (ActivityCompat.checkSelfPermission(activity as Activity, permission) == PackageManager.PERMISSION_GRANTED) {
                            //call the gallery intent
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.setType("video/*")
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, OptiConstant.VIDEO_MERGE_1)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            OptiConstant.VIDEO_MERGE_2 -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if (ActivityCompat.checkSelfPermission(activity as Activity, permission) == PackageManager.PERMISSION_GRANTED) {
                            //call the gallery intent
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.setType("video/*")
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
                            startActivityForResult(i, OptiConstant.VIDEO_MERGE_2)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }
        }
    }

    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) return

        when (requestCode) {
            OptiConstant.VIDEO_MERGE_1 -> {
                data?.let {
                    setFilePath(resultCode, it, OptiConstant.VIDEO_MERGE_1)
                }
            }

            OptiConstant.VIDEO_MERGE_2 -> {
                data?.let {
                    setFilePath(resultCode, it, OptiConstant.VIDEO_MERGE_2)
                }
            }
        }
    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {

        if (resultCode == Activity.RESULT_OK) {
            try {
                val selectedImage = data.data
                //  Log.e("selectedImage==>", "" + selectedImage)
                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = context!!.contentResolver
                    .query(selectedImage!!, filePathColumn, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor
                        .getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if (mode == OptiConstant.VIDEO_MERGE_1) {
                        videoFileOne = File(filePath)
                        Log.v(tagName, "videoFileOne: " + videoFileOne!!.absolutePath)

                        //get thumbnail of selected video
                        bmThumbnailOne = ThumbnailUtils.createVideoThumbnail(
                            videoFileOne!!.absolutePath,
                            MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
                        )

                        ivVideoOne.setImageBitmap(bmThumbnailOne)
                    } else if (mode == OptiConstant.VIDEO_MERGE_2) {
                        videoFileTwo = File(filePath)
                        Log.v(tagName, "videoFileTwo: " + videoFileTwo!!.absolutePath)

                        //get thumbnail of selected video
                        bmThumbnailTwo = ThumbnailUtils.createVideoThumbnail(
                            videoFileTwo!!.absolutePath,
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
                        )

                        ivVideoTwo.setImageBitmap(bmThumbnailTwo)
                    }
                }
            } catch (e: Exception) {
                Log.e(tagName, "Exception: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        fun newInstance() = OptiMergeFragment()
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_LONG).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}
