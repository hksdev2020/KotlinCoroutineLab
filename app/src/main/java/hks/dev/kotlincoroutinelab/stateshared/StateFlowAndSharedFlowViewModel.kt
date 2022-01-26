package hks.dev.kotlincoroutinelab.stateshared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StateFlowAndSharedFlowViewModel : ViewModel() {


    //as known as cold flow
    val countDownFlow = flow<Int> {
        val startValue = 10
        var currentValue = startValue

        emit(startValue)    //emit 10

        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            //emit every second
            emit(currentValue)
        }
    }

    init {
        collectFlowInViewModel()
    }

    private fun collectFlowInViewModel() {
        viewModelScope.launch {
            countDownFlow.collect {
                println("collectFlowInViewModel: $it")
            }
        }
    }


    //hot flow
    //state flow survive configuration change
    //screen rotated, value will be triggered again, notify all observers
    //for single live event for snackbar / navigation, use share flow
    private val _stateFlow = MutableStateFlow<Int>(0)
    val stateFlow = _stateFlow.asStateFlow()
    fun incrementCounter() {
        _stateFlow.value += 1
    }


    //both hot flow: no subscribers = event will be lost
    //state flow: trigger again after rotate screen
    //shared flow: not trigger again after rotate screen
    private val _sharedFlow = MutableSharedFlow<Int>(
        //replay = 2 //cache how many event, will replay n values when new subscribers subscribe
    ) //no default value
    val sharedFlow = _sharedFlow.asSharedFlow()

    fun squareNumber(number: Int) {
        viewModelScope.launch { //hav to be in scope
            _sharedFlow.emit(number * number)
        }
    }

}