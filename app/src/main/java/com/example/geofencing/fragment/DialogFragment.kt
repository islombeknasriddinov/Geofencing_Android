package com.example.geofencing.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.geofencing.R
import com.example.geofencing.model.MyMarker
import com.google.android.gms.maps.model.LatLng

class DialogFragment : DialogFragment() {

    var saveClick: ((MyMarker) -> Unit)? = null
    var latLng: LatLng? = null


    lateinit var etEnter: EditText
    lateinit var etDwell: EditText
    lateinit var etExit: EditText
    lateinit var etRadius: EditText
    lateinit var tvSave: TextView
    lateinit var tvCancel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.customDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEnter = view.findViewById(R.id.et_enter)
        etDwell = view.findViewById(R.id.et_dwell)
        etExit = view.findViewById(R.id.et_exit)
        etRadius = view.findViewById(R.id.et_radius)
        tvSave = view.findViewById(R.id.tv_save)
        tvCancel = view.findViewById(R.id.tv_cancel)

        tvSave.setOnClickListener {
            if (checkIsNotEmpty()) {
                saveClick?.invoke(
                    MyMarker(
                        "${System.currentTimeMillis()}",
                        etEnter.text.toString(),
                        etDwell.text.toString(),
                        etExit.text.toString(),
                        latLng,
                        etRadius.text.toString().toFloat()
                    )
                )
            }

            dismiss()
        }

        tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun checkIsNotEmpty(): Boolean {
        return etEnter.text.isNotEmpty() && etDwell.text.isNotEmpty() && etExit.text.isNotEmpty() && etRadius.text.isNotEmpty()
    }

    fun setCurrentLatLng(newLatLng: LatLng) {
        latLng = newLatLng
    }

    override fun onDestroyView() {
        etEnter.text?.clear()
        etDwell.text?.clear()
        etExit.text?.clear()
        etRadius.text?.clear()
        super.onDestroyView()
    }

}