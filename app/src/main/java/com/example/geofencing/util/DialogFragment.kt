package com.example.geofencing.util


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.geofencing.R
import com.example.geofencing.model.Message

class DialogFragment : DialogFragment() {

    var saveClick: ((Message) -> Unit)? = null

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

        val etEnter: EditText = view.findViewById(R.id.et_enter)
        val etDwell: EditText = view.findViewById(R.id.et_dwell)
        val etExit: EditText = view.findViewById(R.id.et_exit)
        val etRadius: EditText = view.findViewById(R.id.et_radius)
        val tvSave: TextView = view.findViewById(R.id.tv_save)
        val tvCancel: TextView = view.findViewById(R.id.tv_cancel)

        tvSave.setOnClickListener {
            if (etEnter.text.isNotEmpty() &&
                etDwell.text.isNotEmpty() &&
                etExit.text.isNotEmpty() &&
                etRadius.text.isNotEmpty()
            ) {
                saveClick?.invoke(
                    Message(
                        etEnter.text.toString(),
                        etDwell.text.toString(),
                        etExit.text.toString(),
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

}