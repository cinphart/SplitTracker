package online.cinphart.splittracker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener, View.OnClickListener {

    companion object {
        val pool_sizes = PoolSize.values().toList()
        val DEFAULTS = RaceModel(PoolSize.SCY, 500.0, 334460)
    }

    lateinit var model : RaceModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        model = retrieveModel(savedInstanceState, "race", DEFAULTS)
        loadPoolSizes()
        loadRaceLengths()
        loadGoalTime()
        start.setOnClickListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putSerializable("race", model)
    }

    fun loadPoolSizes() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pool_sizes.map {resources.getString(it.resourceId)})
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pool_size.adapter = adapter
        pool_size.onItemSelectedListener = this
        pool_size.setSelection(pool_sizes.indexOf(model.poolSize))
    }

    fun loadRaceLengths() {
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item,
                model.poolSize.distances.map {
                    resources.getString(R.string.distance, it, resources.getString(model.poolSize.unit))
                })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        race_length.adapter = adapter
        race_length.onItemSelectedListener = this
        race_length.setSelection(model.poolSize.distances.indexOf(model.distance))
    }

    fun loadGoalTime() {
        val twoDigitFormatter = NumberPicker.Formatter { i -> String.format("%02d", i) }
        goal_time_minutes.minValue = 0
        goal_time_minutes.maxValue = 39
        goal_time_minutes.value = Math.floor(model.targetTime/60000.0).toInt()
        goal_time_minutes.setFormatter(twoDigitFormatter)
        goal_time_seconds.minValue = 0
        goal_time_seconds.maxValue = 59
        goal_time_seconds.value = (model.targetTime/1000.0 % 60).toInt()
        goal_time_seconds.setFormatter(twoDigitFormatter)
        goal_time_hundredths.minValue = 0
        goal_time_hundredths.maxValue = 99
        goal_time_hundredths.value = ((model.targetTime / 10) % 100).toInt()
        goal_time_hundredths.setFormatter(twoDigitFormatter)
        goal_time_minutes.setOnValueChangedListener(this)
        goal_time_seconds.setOnValueChangedListener(this)
        goal_time_hundredths.setOnValueChangedListener(this)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == pool_size) {
            model.poolSize = pool_sizes[position]
            model.distance = model.poolSize.distances[0]
            loadRaceLengths()
        } else if (parent == race_length) {
            model.distance = model.poolSize.distances[position]
        }
        showSplits()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    fun unit() : String = resources.getString(model.poolSize.unit)

    fun showSplits() {
        val splitTimeSeconds = model.targetTime / model.splits()
        start.isEnabled  = splitTimeSeconds > 20.0
        split_time.text = formatTime(resources, splitTimeSeconds)
    }


    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        model.targetTime =
                goal_time_minutes.value.toLong() * 60000L +
                goal_time_seconds.value.toLong() * 1000L +
                goal_time_hundredths.value.toLong() * 10L
        showSplits()
    }

    override fun onClick(v: View?) {
        val startedAt = SystemClock.elapsedRealtime()
        val intent = Intent(this, TrackActivity::class.java)
        intent.putExtra(TrackActivity.RACE_MODEL, model.copy())
        intent.putExtra(TrackActivity.STARTED_AT, startedAt)
        startActivity(intent)
    }
}
