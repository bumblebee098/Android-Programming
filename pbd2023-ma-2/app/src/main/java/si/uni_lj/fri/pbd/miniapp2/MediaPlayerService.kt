package si.uni_lj.fri.pbd.miniapp2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.IOException
import kotlin.system.exitProcess


class MediaPlayerService: Service() {
    private var accelerationService: AccelerationService? = null

    private var serviceBinder = RunServiceBinder()
    private var player: MediaPlayer = MediaPlayer()

    var isMusicPlaying = false
    var isSongLoaded = false
    var isSongFinished = false

    private var isServiceBound = false

    var currentSongTitle = ""
    var currentSongDuration = ""

    companion object {
        val TAG: String? = MediaPlayerService::class.simpleName
        const val ACTION_START = "start_service"
        const val ACTION_PLAY = "play_serivce"
        const val ACTION_PAUSE = "pause_service"
        const val ACTION_STOP = "stop_service"
        const val ACTION_EXIT = "exit_service"

        const val NOTIFICATION_ID = 1
        private const val channelID = "background_media_player_notification"

        private const val MSG_UPDATE_TIME = 1
        private const val UPDATE_RATE_MS = 1000L
    }

    // service connection
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(MainActivity.TAG, "Service bound")
            val binder = iBinder as AccelerationService.RunServiceBinder
            accelerationService = binder.service
            isServiceBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(MainActivity.TAG, "Service disconnect")
            isServiceBound = false
        }
    }

    inner class RunServiceBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Binding service")

        registerReceiver(PauseMessageReciever, IntentFilter("pause"))
        registerReceiver(PlayMessageReciever, IntentFilter("play"))

        return serviceBinder
    }

    // pause message reciever
    private var PauseMessageReciever: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            pauseMusic()
        }
    }

    // play message reciever
    private var PlayMessageReciever: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            playMusic()
        }
    }

    override fun onCreate() {
        Log.d(TAG, "Creating service")

        super.onCreate()

        createNotificationChannel()
        updateNotification()
    }

    // defining variables and coresponding actions
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null) {
            Log.d(TAG, "Recieved intent: " + intent.action)

            if(intent.action == ACTION_PLAY) {
                playMusic()
                createNotification()
            }

            if(intent.action == ACTION_PAUSE) {
                pauseMusic()
                createNotification()
            }

            if(intent.action == ACTION_STOP) {
                stopMusic()
            }

            if(intent.action == ACTION_EXIT) {
                exitApp()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        } else {
            val channel = NotificationChannel(
                channelID,
                "Foreground channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "notification channel for mediaPlayer"
            val managerCompat = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            managerCompat.createNotificationChannel(channel)
        }
    }


    // update notification in the foreground
    private fun updateNotification() {
        startForeground(NOTIFICATION_ID, createNotification())

    }

    //updater that calls a function every second
    private val updateTimeHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(message: Message) {
            if (MSG_UPDATE_TIME == message.what) {
                updateNotification()
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS)
            }
        }
    }

    // function for creating the notification
    private fun createNotification(): Notification {
        // notification setup, adds song title, duration and progress bar, as well as 4 buttons: play, stop, pause, exit
        val playIntent = Intent(this, MediaPlayerService::class.java)
        playIntent.action = ACTION_PLAY
        var playPendingIntent: PendingIntent? = null

        val pauseIntent = Intent(this, MediaPlayerService::class.java)
        pauseIntent.action = ACTION_PAUSE
        //val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, MediaPlayerService::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(this, MediaPlayerService::class.java)
        exitIntent.action = ACTION_EXIT
        val exitPendingIntent = PendingIntent.getService(this, 3, exitIntent, PendingIntent.FLAG_IMMUTABLE)

        var playpause = ""
        if(isMusicPlaying) {
            playpause = "Pause"
            playPendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            playpause = "Play"
            playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(this, channelID)
            .setContentTitle(currentSongTitle)
            .setContentText(getSongProgress())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setChannelId(channelID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_play, playpause, playPendingIntent)
            //.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.btn_default, "Stop", stopPendingIntent)
            .addAction(android.R.drawable.btn_default, "Exit", exitPendingIntent)

        // rabim progress bar

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()

        if(isServiceBound) {
            stopService(Intent(this, AccelerationService::class.java))
        }

        unregisterReceiver(PauseMessageReciever)
        unregisterReceiver(PlayMessageReciever)

        Log.d(TAG, "Destroying service")
    }

    // function for playing music
    fun playMusic() {
        Log.d(TAG, "Playing music")

        if(isMusicPlaying) {
            Log.d(TAG, "Music is already playing")
            return
        }

        // if song is already loaded
        if(isSongLoaded) {
            Log.d(TAG, "Song is already loaded")
            player.start()
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            isMusicPlaying = true
        } else {
            // load a new random song
            Log.d(TAG, "Loading a new song")
            val song = getRandomSong()
            if(song != null) {
                try {
                    val descriptor = assets.openFd("music/$song")
                    player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    player.prepare()
                    //currentSongDuration = player.duration // / 1000
                    currentSongDuration = createDuration(player.duration)
                    player.start()

                    isMusicPlaying = true
                    isSongLoaded = true
                    // notification
                    updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
                } catch (e: IOException) {
                    Log.d(TAG, e.toString())
                }
            }
        }

        isSongFinished = false
    }

    // function for pausing music
    fun pauseMusic() {
        Log.d(TAG, "Pausing music")
        if(isMusicPlaying) {
            player.pause()
            // update notification
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            isMusicPlaying = false
        }
    }

    // function for stoping music
    fun stopMusic() {
        Log.d(TAG, "Stoping music")
        if(isSongLoaded) {
            if (player.isPlaying) {
                player.stop()
            }
            if (player.isPlaying || player.currentPosition > 0) {
                player.reset()
            }

            isSongLoaded = false
            isMusicPlaying = false
            isSongFinished = true

            // notification
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)

            currentSongDuration = ""

            updateNotification()
        }
        Log.d(TAG, "smo v funkciji stop music " + isMusicPlaying.toString() + isSongFinished.toString())
    }

    // function for exiting app
    fun exitApp() {
        Log.d(TAG, "Exsiting app")
        stopForeground(true)
        stopSelf()
        // gestures
        exitProcess(0)
    }

    // function Starts AccelerationService and enables gesture-based commands
    fun bindAcceleratorService() {
        Log.d(TAG, "binding Acceleration Service")

        val i = Intent(this, AccelerationService::class.java)
        startService(i)
        bindService(i, serviceConnection, 0)

        // update ui
        Toast.makeText(applicationContext, "Gestures activated", Toast.LENGTH_SHORT).show()
    }

    // function Stops AccelerationService and disables gesture-based commands
    fun unbindAcceleratorService() {
        if(isServiceBound) {
            stopService(Intent(this, AccelerationService::class.java))
        }
        Toast.makeText(applicationContext, "Gestures deactivated", Toast.LENGTH_SHORT).show()
    }

    // function that returns a random song
    private fun getRandomSong(): String? {
        val songList = assets.list("music")
        if (songList.isNullOrEmpty()) {
            return null
        }
        Log.d(TAG, "Chosing a random song")
        val randomIndex = (0 until songList.size).random()

        currentSongTitle = songList[randomIndex].toString()
        return songList[randomIndex]
    }

    // function returns current song position
    fun getCurrentSongDuration(): Int {
        return player.currentPosition
    }

    // function returns duration of the song
    fun getSongDuration(): Int {
        player?.let {
            return it.duration
        }
        return 0
    }

    // function returns song progress and formats it
    fun getSongProgress(): String {
        val fullDuration = currentSongDuration
        val currentDuration = createDuration(player.currentPosition)

        if(player.duration == player.currentPosition) {
            isSongFinished = true
        }
        return String.format("%s/%s", currentDuration, fullDuration)
    }

    // function formats duration and current progress: includes hours
    fun createDuration(duration: Int): String {
        var time = ""
        val hr = duration / 1000 / 60 / 60
        val min = duration / 1000 / 60 % 60
        val sec = duration / 1000 % 60
        if (hr > 0) {
            time = "$time$hr:"
            if (min < 10) {
                time += "0"
            }
        }
        time = "$time$min:"
        if (sec < 10) {
            time += "0"
        }
        time += sec
        return time
    }

}