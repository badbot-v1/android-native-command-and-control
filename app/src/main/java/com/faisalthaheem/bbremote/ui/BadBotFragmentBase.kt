package com.faisalthaheem.bbremote.ui

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import badbot.messages.Botstatus
import com.faisalthaheem.bbremote.R
import com.faisalthaheem.bbremote.services.CommunicationService


open class BadBotFragmentBase : Fragment(), ServiceConnection {

    private val TAG = "BadBotFragmentBase"
    protected var _svc: CommunicationService? = null;

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)


        return root
    }

    override fun onResume() {
        super.onResume()

        val commService = Intent(activity!!.applicationContext, CommunicationService::class.java)
        activity!!.bindService(commService, this, Context.BIND_AUTO_CREATE)

        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(mMessageReceiver,
                IntentFilter("Bot-Status"));

    }

    // Handling the received Intents for the "my-integer" event
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent
            val bundle = intent.extras

            val msg = badbot.messages.Botstatus.BotStatus.parseFrom(
                bundle!!.getByteArray("status")
            )

            processBotStatus(msg);
        }
    }

     open fun processBotStatus(status: Botstatus.BotStatus)
     {

     }

    override fun onPause() {

        _svc!!.toggleTelemetry(false);
        LocalBroadcastManager.getInstance(context!!)
            .unregisterReceiver(mMessageReceiver);

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
        _svc!!.toggleTelemetry(true);

    }
}
