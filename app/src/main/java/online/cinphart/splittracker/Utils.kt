package online.cinphart.splittracker

import android.content.res.Resources
import android.os.Bundle
import java.io.Serializable

/**
 * Created by cinph on 11/24/2017.
 */

fun formatTime(resources : Resources, timeSeconds: Double) : String = if (timeSeconds >= 0.0){
    val minutes = Math.floor(timeSeconds / 60.0).toInt()
    val seconds = Math.floor(timeSeconds - minutes * 60.0).toInt()
    val hundreths = ((timeSeconds - Math.floor(timeSeconds)) * 100.0).toInt()
    resources.getString(R.string.time_display, minutes, seconds, hundreths)
} else {
    resources.getString(R.string.no_time_display)
}

fun formatTime(resources : Resources, timeMilliseconds : Long) : String = formatTime(resources, timeMilliseconds/1000.0)

inline fun <reified T : Serializable> retrieveModel(bundle : Bundle?, key: String, default : T) : T {
    val v = bundle?.getSerializable(key)
    return if (v != null && v is T) v else default
}

inline fun seconds(timeAsMs : Long) = timeAsMs / 1000.0
