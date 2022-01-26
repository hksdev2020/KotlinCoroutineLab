package hks.dev.kotlincoroutinelab.compare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import hks.dev.kotlincoroutinelab.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CompareFragment : Fragment() {

    companion object {
        fun newInstance() = CompareFragment()
    }

    private val viewModel by viewModels<CompareViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.compare_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //bindview
        val vLiveDataResult = view.findViewById<TextView>(R.id.vLiveDataResult)
        val vStateFlowResult = view.findViewById<TextView>(R.id.vStateFlowResult)
        val vFlowResult = view.findViewById<TextView>(R.id.vFlowResult)
        val vSharedFlowResult = view.findViewById<TextView>(R.id.vSharedFlowResult)


        //click action
        view.findViewById<TextView>(R.id.vLiveDataButton).setOnClickListener {
            viewModel.triggerLiveData()
        }
        view.findViewById<TextView>(R.id.vStateFlowButton).setOnClickListener {
            viewModel.triggerStateFlow()
        }
        view.findViewById<TextView>(R.id.vFlowButton).setOnClickListener {
            val flow = viewModel.triggerFlow()
            lifecycleScope.launch { //can use launch, not a stateFlow
                flow.collectLatest {
                    vFlowResult.text = "$it"
                    vFlowResult.turnRed()
                }
            }
        }
        view.findViewById<TextView>(R.id.vSharedFlowButton).setOnClickListener {
            viewModel.triggerSharedFlow()
        }


        //livedata observe
        viewModel.liveData.observe(viewLifecycleOwner, {
            vLiveDataResult.text = "$it"
            vLiveDataResult.turnRed()
        })
        //stateFlow subscribe
        lifecycleScope.launchWhenStarted { //must be in coroutine
            viewModel.stateFlow.collectLatest {
                vStateFlowResult.text = "$it"
                vStateFlowResult.turnRed()
                Snackbar.make(view, "StateFlowTriggered", Snackbar.LENGTH_SHORT).show()
            }
        }
        //sharedFlow subscribe
        lifecycleScope.launchWhenStarted { //must be in coroutine
            viewModel.sharedFlow.collectLatest {
                vSharedFlowResult.text = "$it"
                vSharedFlowResult.turnRed()
            }
        }

        //redirect
        view.findViewById<TextView>(R.id.vStateAndSharedFlowButton)
            .setOnClickListener {
                Navigation
                    .findNavController(view)
                    .navigate(R.id.action_compareFragment_to_stateFlowAndSharedFlowFragment)
            }
    }

}

fun TextView.turnRed(){
    val red = ContextCompat.getColor(context, R.color.red)
    this.setTextColor(red)
}