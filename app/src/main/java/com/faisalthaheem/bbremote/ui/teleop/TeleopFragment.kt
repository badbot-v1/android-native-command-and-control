package com.faisalthaheem.bbremote.ui.teleop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.faisalthaheem.bbremote.R
import com.faisalthaheem.bbremote.ui.BadBotFragmentBase
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlin.math.cos


class TeleopFragment : BadBotFragmentBase() {

    private lateinit var dashboardViewModel: DashboardViewModel
    val TAG = "TeleopFragment";

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_teleop, container, false)

        val joystick = root.findViewById(R.id.js) as JoystickView
        joystick.setOnMoveListener { angle, strength ->

            val vX = (cos(Math.toRadians(angle.toDouble())) * strength).toInt();
            var vY = strength

            if(angle > 180 && angle < 360){
                vY *= -1
            }

            Log.i(TAG, String.format("angle [%d] strength [%d] vX [%d] vY [%d]", angle, strength, vX, vY));
            _svc!!.txTeleop(vX, vY);

        }

        return root
    }
}
