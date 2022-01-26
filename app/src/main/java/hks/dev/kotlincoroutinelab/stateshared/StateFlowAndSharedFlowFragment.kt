package hks.dev.kotlincoroutinelab.stateshared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import hks.dev.kotlincoroutinelab.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StateFlowAndSharedFlowFragment : Fragment() {

    companion object {
        fun newInstance() = StateFlowAndSharedFlowFragment()
    }

    private val viewModel by viewModels<StateFlowAndSharedFlowViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.state_flow_and_shared_flow_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //bind view
        val vCollect = view.findViewById<TextView>(R.id.vCollect)
        val vCollectLatest = view.findViewById<TextView>(R.id.vCollectLatest)
        val vCollectWithFlowOperator = view.findViewById<TextView>(R.id.vCollectWithFlowOperator)
        val vCollectWithFlowTerminalOperator =
            view.findViewById<TextView>(R.id.vCollectWithFlowTerminalOperator)
        val vIncrement = view.findViewById<TextView>(R.id.vIncrement)
        val vFireSharedFlow = view.findViewById<TextView>(R.id.vFireSharedFlow)

/*
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

            }
        }

        //collect flow must be ran in coroutine
        lifecycleScope.launchWhenStarted {

            //collect flow
            viewModel.countDownFlow
                .collect {
                    vCollect.text = "collect: $it"
                }


            //collect latest flow
            viewModel.countDownFlow.collectLatest {
                //when new value arrive, current action block will be cancelled
                //e.g. 10
                delay(1500) //load 1.5s, but next value arrive, then it wont continue
                vCollectLatest.text = "collect latest: $it"
            }


            //collect flow with flow operator
            viewModel.countDownFlow
                .filter { time ->
                    time % 2 == 0
                }
                .map { time ->
                    time * time
                }
                .onEach { time ->
                    println("onEach flow operator: $time")
                }
                .collect {
                    vCollectWithFlowOperator.text = "collect with flow operator: $it"
                }


            //flow with flow terminal operator
            val count = viewModel.countDownFlow
                .filter { time ->
                    time % 2 == 0
                }
                .map { time ->
                    time * time
                }
                .onEach { time ->
                    println("onEach flow terminal operator: $time")
                }
                .count {
                    it % 2 == 0
                }
            vCollectWithFlowTerminalOperator.text = "flow with flow terminal operator count: $count"


            // [[1,2], [1,2,3]]
            // flattening flow operator
            // val flow1 = flowOf(1..10)
            val flow1 = flow {
                emit(1)
                delay(500L)
                emit(2)
            }
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1) //1+1=2, 2+1=3
                    delay(500L)
                    emit(value + 2) //1+2=3, 2+2=4
                }
            }.collect {
                println("flatMapConcat flow: $it")
            }


            val taskFlow = flow {
                delay(500L)
                emit("Task 1")
                delay(500L)
                emit("Task 2")
                delay(500L)
                emit("Task 3")
            }
            taskFlow
                .onEach {
                    println("Task received: $it")
                }
//                .buffer() //ensure collect block run in different coroutine with taskFlow
//                .conflate() //skipped task 2, directly go to latest emit value
//                .collectLatest {
//                    //when new value arrive, current action block will be cancelled
//                    //e.g. 10
//                    //delay(1500) //load 1.5s, but next value arrive, then it wont continue
                .collect {
                    println("Task begin: $it")
                    delay(1500L)
                    println("Task done: $it")
                    //collect block finish, then new value is emitted
                    //e.g. Task 1 done then Task 2 comes
                    //will affect taskflow block
                }
        }//launchWhenStarted


        //state flow
        vIncrement.text = "Increment Button init"
        vIncrement.setOnClickListener {
            //viewModel.stateFlow.value = 1 //never do this!
            viewModel.incrementCounter()
        }
        lifecycleScope.launchWhenStarted {
            viewModel.stateFlow.collect {
                vIncrement.text = "Increment: $it"
            }

        }
        //custom function
//        collectLatestLifecycleFlow(viewModel.stateFlow) {
//            vIncrement.text = "Increment: $it"
//        }
*/


        //shared flow
//        lifecycleScope.launchWhenStarted {
        viewModel.squareNumber(3) //no collectors, then event will be lost
//        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedFlow.collect {
                delay(2000L)
                println("1st Flow: received number is $it")
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedFlow.collect {
                delay(4000L)
                println("2nd Flow: received number is $it")
            }
        }
//        lifecycleScope.launchWhenStarted {
//            viewModel.squareNumber(3)
//        }
    }
}


fun <T> Fragment.collectLatestLifecycleFlow(
    flow: Flow<T>,
    collect: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest {
                collect.invoke(it)
            }
        }
    }
}
