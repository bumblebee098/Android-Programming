package si.uni_lj.fri.pbd.miniapp2

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.coroutines.*
import si.uni_lj.fri.pbd.miniapp2.MediaPlayerService.Companion.ACTION_START
import si.uni_lj.fri.pbd.miniapp2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mediaPlayerService: MediaPlayerService? = null
    private var isServiceBound = false

    private val progressBarScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var mainBinding: ActivityMainBinding

    companion object {
        internal val TAG = MainActivity::class.simpleName
        const val ACTION_EXIT = "stop_service"
        private const val MSG_UPDATE_TIME = 1
        private const val UPDATE_RATE_MS = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        connectToService()

        // pressed EXIT button
        mainBinding.btnExit.setOnClickListener {
            Log.d(TAG, "pressed EXIT button")
            if(mediaPlayerService?.isMusicPlaying == true) {
                mediaPlayerService?.isMusicPlaying = false
            }
            mediaPlayerService?.exitApp()
            finishAndRemoveTask()
        }

        // pressed PLAY button
        mainBinding.btnPlay.setOnClickListener {
            Log.d(TAG, "pressed PLAY button")
            mediaPlayerService?.playMusic()

            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            updateUI()

            if(mediaPlayerService?.isSongFinished == true) {
                mainBinding.progressBar.progress = 0
            }

            progressBarScope.launch {
                Log.d(TAG, "progress bar launch")
                updateProgressBar()
            }
        }

        // pressed PAUSE button
        mainBinding.btnPause.setOnClickListener {
            Log.d(TAG, "pressed PAUSE button")
            mediaPlayerService?.pauseMusic()
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            updateUI()
        }

        // pressed STOP button
        mainBinding.btnStop.setOnClickListener {
            Log.d(TAG, "pressed STOP button")
            mediaPlayerService?.stopMusic()
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            updateUI()
            mainBinding.progressBar.progress = 0 // do i need this
        }

        // pressed GESTURES ON button
        mainBinding.btnGesturesOn.setOnClickListener {
            Log.d(TAG, "pressed GESTURES ON button")
            mediaPlayerService?.bindAcceleratorService()
        }

        // pressed GESTURES OFF button
        mainBinding.btnGesturesOff.setOnClickListener {
            Log.d(TAG, "pressed GESTURES OFF button")
            mediaPlayerService?.unbindAcceleratorService()
        }
    }

    // function to connect to the service
    private fun connectToService() {
        Log.d(TAG, "Starting and binding service");
        val i = Intent(this, MediaPlayerService::class.java)
        startService(i)
        i.action = ACTION_START
        bindService(i, serviceConnection, 0);
    }

    // service connection
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "Service bound")
            val binder = iBinder as MediaPlayerService.RunServiceBinder
            mediaPlayerService = binder.service
            isServiceBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "Service disconnect")
            isServiceBound = false
        }
    }

    // stoping and unbinding the services
    override fun onDestroy() {
        super.onDestroy()
        val i = Intent(this, MediaPlayerService::class.java)
        i.action = ACTION_EXIT
        stopService(i)
        unbindService(serviceConnection)
    }

    override fun onResume() {
        Log.d(TAG, "Resuming")
        super.onResume()

        if(mediaPlayerService?.isSongLoaded == true) {
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            mainBinding.trackTitle.text = mediaPlayerService?.currentSongTitle ?: ""
            mainBinding.duration.text = mediaPlayerService?.getSongProgress()
        }
    }

    // handler updates the UI when playing music
    private val updateTimeHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(message: Message) {
            if (MSG_UPDATE_TIME == message.what) {
                updateUITimer()
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS)
            }
        }
    }

    // function updates UI timer
    private fun updateUITimer() {
        if(isServiceBound) {
            if(mediaPlayerService?.isSongLoaded == true) {
                mainBinding.duration.text = mediaPlayerService?.getSongProgress()
            } else {
                mainBinding.duration.text = ""
            }
            // do sth
        }
    }

    // function that updates the UI
    private fun updateUI() {
        mainBinding.trackTitle.text = mediaPlayerService?.currentSongTitle ?: ""
        if(mediaPlayerService?.isSongLoaded == true) {
            mainBinding.duration.text = mediaPlayerService?.getSongProgress()
        } else {
            mainBinding.duration.text = "0:00:00/0:00:00"
        }
        if(mediaPlayerService?.isSongFinished == true) {
            mainBinding.progressBar.progress = 0
        }
    }

    //private var job: Job? = null
    private lateinit var job: Job

    // function for updating the progess bar
    private fun updateProgressBar() {
        if (this::job.isInitialized && job.isActive) {
            job.cancel()
        }

        Log.d(TAG, "updateprogressbar: " + mediaPlayerService?.isMusicPlaying.toString())

        if(mediaPlayerService?.isMusicPlaying == true) {
            job = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    try {
                        mediaPlayerService?.let {
                        val currentDuration = it.getCurrentSongDuration()
                        val maxDuration = mediaPlayerService?.getSongDuration()
                        val progress = if(maxDuration != 0) currentDuration * 100 / maxDuration!! else 0
                        mainBinding.progressBar.progress = progress.toInt()
                        }
                        delay(500)
                    } catch (e: InterruptedException) {
                        continue
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }
}