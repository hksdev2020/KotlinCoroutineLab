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


    //hot flow: no subscribers = event will be lost
    //state flow survive configuration change
    //screen rotate: value will be triggered again, notify all observers. mainly used in ui
    //default replay = 1, replay = replay how many items already in flow
    private val _stateFlow = MutableStateFlow<Int>(0)
    val stateFlow = _stateFlow.asStateFlow()
    fun incrementCounter() {
        _stateFlow.value += 1
    }


    //hot flow: no subscribers = event will be lost
    //screen rotate: value will NOT be triggered again, for functions like snachbar / navigation
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