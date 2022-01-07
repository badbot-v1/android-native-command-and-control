package com.faisalthaheem.bbremote.ui.home

import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.lifecycle.ViewModelProviders
import badbot.messages.Botstatus
import badbot.messages.Consts
import com.faisalthaheem.bbremote.R
import com.faisalthaheem.bbremote.ui.BadBotFragmentBase
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BadBotFragmentBase(), ServiceConnection {

    private lateinit var homeViewModel: HomeViewModel
    private val TAG = "HomeFragment"


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val _radio_group_op_mode = root.findViewById<RadioGroup>(R.id.radio_group_op_mode)
        _radio_group_op_mode.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                //val selectedButton : RadioButton = root.findViewById(checkedId)
                when(checkedId){
                    R.id.radio_auto -> {
                        _svc!!.setOpMode(Consts.OperationalMode.AUTONOMOUS)
                    }
                    R.id.radio_follow -> {
                        _svc!!.setOpMode(Consts.OperationalMode.FOLLOW_ME)
                    }
                    R.id.radio_teleop -> {
                        _svc!!.setOpMode(Consts.OperationalMode.TELEOP)
                    }
                }
            }
        )

        root.findViewById<Button>(R.id.btn_lidar_on).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setLidarState(true)
        })
        root.findViewById<Button>(R.id.btn_lidar_off).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setLidarState(false)
        })

        root.findViewById<Button>(R.id.btn_lidar_recording_on).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setLidarRecording(true)
        })
        root.findViewById<Button>(R.id.btn_lidar_recording_off).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setLidarRecording(false)
        })

        root.findViewById<Button>(R.id.btn_source_stream_on).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setAppSrcStatus(true)
        })
        root.findViewById<Button>(R.id.btn_source_stream_off).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setAppSrcStatus(false)
        })

        root.findViewById<Button>(R.id.btn_app_stream_on).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setMobileVideoStreamStatus(true)
        })
        root.findViewById<Button>(R.id.btn_app_stream_off).setOnClickListener(View.OnClickListener { v ->
            _svc!!.setMobileVideoStreamStatus(false)
        })

        root.findViewById<Button>(R.id.btn_telemetry_on).setOnClickListener(View.OnClickListener { v ->
            _svc!!.toggleTelemetry(true)
        })
        root.findViewById<Button>(R.id.btn_telemetry_off).setOnClickListener(View.OnClickListener { v ->
            _svc!!.toggleTelemetry(false)
            status_telemetry.setBackgroundColor(Color.RED)
        })

        return root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun processBotStatus(status: Botstatus.BotStatus)
    {

        tv_current_status.text = status.mode.toString()

        when(status.appSrcEnabled){
            true -> {
                tv_status_video_stream.setBackgroundColor(Color.GREEN)
            }
            false-> {
                tv_status_video_stream.setBackgroundColor(Color.RED)
            }
        }

        when(status.lidarEnabled){
            true -> {
                tv_status_lidar.setBackgroundColor(Color.GREEN)
            }
            false-> {
                tv_status_lidar.setBackgroundColor(Color.RED)
            }
        }

        when(status.lidarRecordingEnabled){
            true -> {
                tv_status_lidar_recording.setBackgroundColor(Color.GREEN)
            }
            false-> {
                tv_status_lidar_recording.setBackgroundColor(Color.RED)
            }
        }

        when(status.wsVideoEnabled){
            true -> {
                status_app_video_stream.setBackgroundColor(Color.GREEN)
            }
            false-> {
                status_app_video_stream.setBackgroundColor(Color.RED)
            }
        }

        when(status.telemetryEnabled){
            true -> {
                status_telemetry.setBackgroundColor(Color.GREEN)
            }
            false-> {
                status_telemetry.setBackgroundColor(Color.RED)
            }
        }

    }
}
