package com.faisalthaheem.bbremote

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.faisalthaheem.bbremote.services.CommunicationService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity() : AppCompatActivity(), ServiceConnection {

    private val TAG = "MainActivity"

    var _svc:CommunicationService? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_teleop, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



    }

    override fun onResume() {
        super.onResume()

        val commService = Intent(applicationContext, CommunicationService::class.java)
        bindService(commService, this, Context.BIND_AUTO_CREATE)

        applicationContext.startService(commService)

        LocalBroadcastManager.getInstance(applicationContext!!)
            .registerReceiver(mMessageReceiver,
                IntentFilter("Connectivity-Status"));


        Log.i(TAG,"App resumed.....")
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent

            tv_connection_status.text = intent.getStringExtra("status")
        }
    }

    override fun onPause() {

        unbindService(this)
        LocalBroadcastManager.getInstance(applicationContext!!)
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
        Toast.makeText(this, "Service disconnected", Toast.LENGTH_SHORT)
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
        Toast.makeText(this, "Service connected.", Toast.LENGTH_SHORT)
    }
}
