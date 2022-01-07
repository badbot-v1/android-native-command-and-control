package com.faisalthaheem.bbremote.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.faisalthaheem.bbremote.R
import com.faisalthaheem.bbremote.services.CommunicationService
import kotlinx.android.synthetic.main.fragment_home.*

class SettingsFragment : Fragment(), ServiceConnection {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private val TAG = "SettingsFragment"
    var _svc: CommunicationService? = null;
    var _radio_group_host:RadioGroup? = null;


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        //https://stackoverflow.com/questions/51672231/kotlin-button-onclicklistener-event-inside-a-fragment
        val buttonConnect = root.findViewById<Button>(R.id.settings_button_connect)
        buttonConnect.setOnClickListener{view ->
             onClick(view)
        }

        _radio_group_host = root.findViewById<RadioGroup>(R.id.settings_radio_group_host)

        return root
    }

    override fun onResume() {
        super.onResume()

        val commService = Intent(activity!!.applicationContext, CommunicationService::class.java)
        activity!!.bindService(commService, this, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does *not* remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to [.onServiceConnected] when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     * connection has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName?) {
        _svc = null

        Log.i(TAG,"Service disconnected")
    }

    /**
     * Called when a connection to the Service has been established, with
     * the [android.os.IBinder] of the communication channel to the
     * Service.
     *
     *
     * **Note:** If the system has started to bind your
     * client app to a service, it's possible that your app will never receive
     * this callback. Your app won't receive a callback if there's an issue with
     * the service, such as the service crashing while being created.
     *
     * @param name The concrete component name of the service that has
     * been connected.
     *
     * @param service The IBinder of the Service's communication channel,
     * which you can now make calls on.
     */
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val b = service as CommunicationService.CommunicationServiceBinder;
        _svc = b.getService()

        Log.i(TAG,"Service connected")
    }

    fun onClick(v:View)
    {
        Log.i(TAG, "Button clicked ")
        when(v.id)
        {
            R.id.settings_button_connect -> {
                Log.i(TAG, "Connect button was pressed")
                _svc!!.sayHello()

                when(_radio_group_host!!.checkedRadioButtonId)
                {
                    R.id.settings_radio_button_localhost -> {
                        _svc!!.connectToHost("192.168.230.10")
                    }

                    R.id.settings_radio_button_nanogl -> {
                        _svc!!.connectToHost("192.168.20.100")
                    }
                }
            }
        }
    }
}
