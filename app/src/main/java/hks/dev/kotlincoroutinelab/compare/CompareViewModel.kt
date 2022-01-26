package hks.dev.kotlincoroutinelab.compare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompareViewModel : ViewModel() {

    //livedata is optional to have value
//    private val _liveData = MutableLiveData<String>("Hello I am live data")
    private val _liveData = MutableLiveData<String>()
    val liveData: LiveData<String> = _liveData

    private val _stateFlow = MutableStateFlow("Hello I am state flow")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()

    private val _sharedFlow = MutableSharedFlow<String>()
    val sharedFlow: SharedFlow<String> = _sharedFlow.asSharedFlow()


    fun triggerLiveData() {
        _liveData.value = "LiveData!!!"
    }

    fun triggerFlow(): Flow<String> { //cold flow
        return flow {
            repeat(5) {
                emit("Item $it")
                delay(1000L)
            }
            emit("Item 5 & Flow!!!")
        }
    }

    fun triggerStateFlow() {
        _stateFlow.value = "StateFlow!!!"
    }

    fun triggerSharedFlow() {
        viewModelScope.launch {
            _sharedFlow.emit("SharedFlow!!!")
        }
    }

}