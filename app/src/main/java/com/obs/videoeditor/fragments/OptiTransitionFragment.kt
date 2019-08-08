/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.videoeditor.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.obs.videoeditor.R
import com.obs.videoeditor.adapter.OptiTransitionAdapter
import com.obs.videoeditor.interfaces.OptiFilterListener
import com.obs.videoeditor.utils.OptiUtils
import java.io.File
import java.util.*

class OptiTransitionFragment : BottomSheetDialogFragment(), OptiFilterListener {

    private var tagName: String = OptiTransitionFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvTransition: RecyclerView
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var helper: OptiBaseCreatorDialogFragment.CallBacks? = null
    private var transitionList: ArrayList<String> = ArrayList()
    private lateinit var optiTransitionAdapter: OptiTransitionAdapter
    private var selectedTransition: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.opti_fragment_transition, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTransition = rootView.findViewById(R.id.rvTransition)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            optiTransitionAdapter.setTransition()

            if (selectedTransition != null) {
                dismiss()

                when (selectedTransition) {
                    "Fade in/out" -> {
                        applyTransitionAction()
                    }
                }
            }
        }

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvTransition.layoutManager = linearLayoutManager

        transitionList.add("Fade in/out")

        optiTransitionAdapter = OptiTransitionAdapter(transitionList, activity!!.applicationContext, this)
        rvTransition.adapter = optiTransitionAdapter
        optiTransitionAdapter.notifyDataSetChanged()
    }

    private fun applyTransitionAction() {
        //output file is generated and send to video processing
        val outputFile = OptiUtils.createVideoFile(context!!)
        Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

        val cmd = arrayOf("-y", "-i", videoFile!!.absolutePath, "-acodec", "copy", "-vf", "fade=t=in:st=0:d=5", outputFile.absolutePath)

        try {
            FFmpeg.getInstance(context).execute(cmd, object : ExecuteBinaryResponseHandler() {
                override fun onStart() {
                    Log.v(tagName, "onStart()")
                    helper?.showLoading(true)
                }

                override fun onProgress(message: String?) {
                    Log.v(tagName, "onProgress()")
                }

                override fun onSuccess(message: String?) {
                    Log.v(tagName, "onSuccess()")
                    helper?.showLoading(false)
                    helper?.onFileProcessed(outputFile)
                }

                override fun onFailure(message: String?) {
                    Log.v(tagName, "onFailure() $message")
                    helper?.showLoading(false)
                }

                override fun onFinish() {
                    Log.v(tagName, "onFinish()")
                    helper?.showLoading(false)
                }
            })
        } catch (e: Exception) {
            Log.v(tagName, "Exception ${e.localizedMessage}")
        } catch (e2: FFmpegCommandAlreadyRunningException) {
            Log.v(tagName, "FFmpegCommandAlreadyRunningException ${e2.localizedMessage}")
        }
    }

    override fun selectedFilter(filter: String) { //here transition
        selectedTransition = filter
    }

    fun setHelper(helper: OptiBaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }
}