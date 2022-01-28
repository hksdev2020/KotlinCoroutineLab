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
import androidx.navigation.Navigation
import hks.dev.kotlincoroutinelab.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
        val vCompareButton = view.findViewById<TextView>(R.id.vCompareButton)


        /**
         * observe flow for ui
         */
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
            }
        }
        // collect flow must be ran in coroutine
        // Collects from the flow when the View is at least STARTED and
        // SUSPENDS the collection when the lifecycle is STOPPED.
        // Collecting the flow cancels when the View is DESTROYED.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {}


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //collect flow
                viewModel.countDownFlow
                    .collect {
                        vCollect.text = "cold flow collect: $it"
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //collect latest flow
                viewModel.countDownFlow
                    .collectLatest {
                        //when new value arrive, current action block will be cancelled
                        //e.g. 10
                        delay(1500) //load 1.5s, but next value arrive, then it wont continue
                        vCollectLatest.text = "cold flow collect latest: $it"
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //collect flow with flow operator
                viewModel.countDownFlow
                    .filter { time ->
                        time % 2 == 0
                    }
                    .map { time ->
                        time * time
                    }
                    .onEach { time ->
                        println("cold flow onEach operator: $time")
                    }
                    .collect {
                        vCollectWithFlowOperator.text = "collect with cold flow operator onEach: $it"
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
                        println("onEach cold flow terminal operator: $time")
                    }
                    .count {
                        it % 2 == 0
                    }
                vCollectWithFlowTerminalOperator.text =
                    "cold flow with flow terminal operator count: $count"
            }
        }



        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //cold flow - flow builder can be:
                //val flow1 = flowOf(1..10)
                //val flow1 = (1..5).asFlow()
                val flow1 = flow {
                    emit(1)
                    delay(500L)
                    emit(2)
                    delay(500L)
                    emit(3)
                }
                // flattening flow operator
                flow1.flatMapConcat { value ->  //outer flow
                    flow { // inner flow
                        emit("$value A") //1+A, 2+A, 3+A
                        delay(500L)
                        emit("$value B") //1+B, 2+B, 3+B
                    }
                }.collect { //output sequential result
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
//                .conflate() //shortcut of buffer, skipped task 2, directly go to latest emit value
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
            }
        }

        //state flow
        vIncrement.text = "Increment Button init"
        vIncrement.setOnClickListener {
            //viewModel.stateFlow.value = 1 //never do this!
            viewModel.incrementCounter()
        }
        collectLatestLifecycleFlow(viewModel.stateFlow) {
            vIncrement.text = "Increment: $it"
        }


        //shared flow
        lifecycleScope.launchWhenStarted {
            //won't work for here
            //no collectors, then event will be lost
            viewModel.squareNumber(3)
        }
        collectLatestLifecycleFlow(viewModel.sharedFlow) {
            delay(1000L)
            println("1st Flow: received number is $it")
        }
        collectLatestLifecycleFlow(viewModel.sharedFlow) {
            delay(3000L)
            println("2nd Flow: received number is $it")
        }
        lifecycleScope.launchWhenStarted {
            //works here
            //since all collectors are setup already
            viewModel.squareNumber(4)
        }



        vCompareButton.setOnClickListener {
            Navigation
                .findNavController(it)
                .navigate(R.id.action_stateFlowAndSharedFlowFragment_to_compareFragment)
        }
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
