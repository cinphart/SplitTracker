package online.cinphart.splittracker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_track.*

class TrackActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        val RACE_MODEL = "race_model"
        val STARTED_AT = "started_at"
    }

    lateinit var event: EventModel

    private val handler = Handler()
    private val callback = this::refreshView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey("event")==true) {
            event = savedInstanceState.getSerializable("event") as EventModel
        } else {
            val raceModel = intent.getSerializableExtra(RACE_MODEL) as RaceModel
            val splitsAt = Array(raceModel.splits() + 1) { 0L }
            splitsAt[0] = intent.getLongExtra(STARTED_AT, 0L)
            event = EventModel(raceModel, splitsAt)
        }
        setContentView(R.layout.activity_track)
        race_time_goal.text = formatTime(resources, event.raceModel.targetTime)
        split_time_goal.text = formatTime(resources, event.raceModel.splitTarget())
        distance_goal.text = resources.getString(R.string.distance, event.raceModel.distance,
                resources.getString(event.raceModel.poolSize.unit))
        split.setOnClickListener(this)
        oops_minus.setOnClickListener(this)
        oops_minus.isEnabled = false
        oops_plus.setOnClickListener(this)
        refreshView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable("event", event)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState?.containsKey("event")==true) {
            event = savedInstanceState.getSerializable("event") as EventModel
        }
    }

    fun scheduleUpdates() {
        if (split.isEnabled) {
            handler.postDelayed(callback, 16) // ~60 Hz, which is faster than most screens refresh
        } else {
            handler.removeCallbacks(callback)
        }
    }

    fun refreshView() {
        updateView()
        scheduleUpdates()
    }

    fun updateView() {
        val now = SystemClock.elapsedRealtime()
        val state = event.stateAt(now)
        val currentSplit = event.currentSplit
        race_time_elapsed.text = formatTime(resources, state.elapsed_race_time)
        split_time_elapsed.text = formatTime(resources, state.elapsed_split_time)
        distance_elapsed.text = resources.getString(
                R.string.distance,
                state.estimated_distance_swam,
                resources.getString(event.raceModel.poolSize.unit))
        oops_minus.isEnabled = state.card_number != 1
        oops_plus.isEnabled = !state.last_lap && !state.race_complete
        split.isEnabled = !state.race_complete

        if (currentSplit > 0) {
            split_time_expected.text = formatTime(resources, state.expected_split_time)
            split_time_expected.setTextColor(
                    goalColor(state.expected_split_time,
                            seconds(event.raceModel.splitTarget()),
                            0.02))
            race_time_expected.text = formatTime(resources, state.expected_finish_time)
            race_time_expected.setTextColor(
                    goalColor(state.expected_finish_time,
                            seconds(event.raceModel.targetTime),
                            0.01))
        } else {
            split_time_expected.text = ""
            split_time_expected.setTextColor(
                    ContextCompat.getColor(applicationContext, android.R.color.secondary_text_light))
            race_time_expected.text = ""
            race_time_expected.setTextColor(
                    ContextCompat.getColor(applicationContext, android.R.color.secondary_text_light))
        }

        updateCard(state)

        when {
            state.last_lap -> split.text = resources.getText(R.string.last_lap_button_label)
            state.race_complete -> {
                split.text = resources.getText(R.string.finished_button_label)
                split.isEnabled = false
            }
            else -> split.text = resources.getText(R.string.split_button_label)
        }
    }

    override fun onClick(v: View?) {
        val now = SystemClock.elapsedRealtime()
        if (v == split) {
            splitNow(now)
        } else if (v == oops_minus) {
            oopsMinus(now)
        } else if (v == oops_plus) {
            oopsPlus(now)
        }
    }

    private fun oopsPlus(now: Long) {
        event.oopsPlus(now)
        updateView()
    }

    private fun oopsMinus(now: Long) {
        event.oopsMinus(now)
        updateView()
        // If this is the last lap, previously race was over, so refresh was ended,
        // so we need to restart it.
        if (event.stateAt(now).last_lap) {
            scheduleUpdates()
        }
    }

    private fun splitNow(now: Long) {
        // This is mostly to protect the volume button handlers from trying to create
        // a split once the race has finished - this causes an attempt to write past the
        // end of the array.
        if (!event.stateAt(now).race_complete) {
            event.splitAt(now)
        }
        updateView()
    }

    private fun goalColor(expected: Double, goal: Double, window: Double) =
            when {
                expected < goal * (1.0 - window) -> ContextCompat.getColor(applicationContext, R.color.exceedingGoal)
                expected > goal * (1.0 + window) -> ContextCompat.getColor(applicationContext, R.color.trailingGoal)
                else -> ContextCompat.getColor(applicationContext, android.R.color.secondary_text_light)
            }

    private fun updateCard(state : InstantaneousSnapshot) {
        if (!state.last_lap && !state.race_complete) {
            card.text = state.card_number.toString()
            card.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.primary_text_light))
        } else {
            card.text = resources.getText(R.string.final_card)
            card.setTextColor(ContextCompat.getColor(applicationContext, R.color.finalCard))
        }
    }

    // This uses the volume keys to trigger a split to be seen.
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            splitNow(now)
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            splitNow(now)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
