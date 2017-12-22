package online.cinphart.splittracker

import java.io.Serializable

/**
 * Created by cinph on 11/26/2017.
 */

enum class PoolSize(val resourceId : Int, val unit : Int, val size : Int, val distances : Array<Double>) {
    SCY(R.string.pool_size_scy, R.string.unit_yards, 25, arrayOf(500.0,1000.0,1650.0) ),
    LCM(R.string.pool_size_lcm, R.string.unit_meters, 50, arrayOf(400.0, 800.0, 1500.0))
}

fun PoolSize.splits(distance : Double) : Int = Math.floor(distance/(size*2)).toInt()

data class RaceModel(var poolSize: PoolSize = PoolSize.SCY,
                     var distance : Double = 500.0,
                     var targetTime : Long) : Serializable {

    fun splits() : Int = (distance/(poolSize.size*2.0)).toInt()

    fun splitTarget() = targetTime / splits()
}

data class EventModel(val raceModel : RaceModel,
                      val splits : Array<Long>,
                      var currentSplit : Int = 0) : Serializable {

    fun splitAt(now : Long) {
        splits[++currentSplit] = now
    }

    fun oopsMinus(now : Long) {
        if (currentSplit > 0) {
            currentSplit--
        }
    }

    fun oopsPlus(now : Long) : Unit = TODO()

    fun stateAt(now : Long) : InstantaneousSnapshot =
            InstantaneousSnapshot(
                    elapsed_race_time = seconds(now - startedAt()),
                    elapsed_split_time = seconds(now - lastSplitAt()),
                    estimated_distance_swam = estimateDistance(now),
                    splits_completed = currentSplit,
                    race_complete = currentSplit == raceModel.splits(),
                    last_lap = currentSplit == raceModel.splits()-1,
                    card_number = (currentSplit * 2) + 1,
                    expected_finish_time = seconds(estimatedTimeToComplete()),
                    expected_split_time = seconds(splitAverage()))

    fun lastSplitAt() : Long = splits[currentSplit]

    fun startedAt() : Long = splits[0]

    fun splitAverage() : Long = if (currentSplit != 0) (lastSplitAt()-startedAt())/currentSplit else raceModel.splitTarget()

    fun estimatedTimeToComplete() : Long = splitAverage() * raceModel.splits()

    fun estimateDistance(now : Long) : Double {
        val maximumDistanceTraveled = raceModel.poolSize.size * currentSplit+1
        val estimatedDistance = (now - startedAt())*raceModel.distance/estimatedTimeToComplete()
        return Math.min(estimatedDistance, maximumDistanceTraveled.toDouble())
    }
}

data class InstantaneousSnapshot(
        val elapsed_race_time : Double,
        val elapsed_split_time : Double,
        val estimated_distance_swam : Double,
        val splits_completed : Int,
        val race_complete : Boolean,
        val last_lap : Boolean,
        val card_number : Int,
        val expected_finish_time : Double,
        val expected_split_time : Double)