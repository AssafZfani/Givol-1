package com.givol.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.givol.R
import com.givol.activities.MainActivity
import com.givol.adapters.BaseAdapter
import com.givol.adapters.ContestsAdapter
import com.givol.core.AbstractAction
import com.givol.core.Action
import com.givol.core.GivolFragment
import com.givol.core.SupportsOnBackPressed
import com.givol.model.FBContest
import com.givol.mvvm.State
import com.givol.navigation.arguments.TransferInfo
import com.givol.screens.ContestDetailsScreen
import com.givol.utils.Event
import com.givol.utils.FirebaseManager
import com.givol.utils.GridSpacingItemDecoration
import com.givol.widgets.GivolToolbar
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainFragment : GivolFragment(), GivolToolbar.ActionListener, SupportsOnBackPressed,
    BaseAdapter.AdapterListener<FBContest> {

    lateinit var transferInfo: TransferInfo
    //private val fbUtil: FirebaseUtils by inject()

    private val firebaseManager: FirebaseManager by inject()
    private val viewModel by viewModel<MainViewModel>()

    private val contestsAdapter = ContestsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transferInfo = castArguments(TransferInfo::class.java)

        registerViewModel()
        configureToolbar()
        recyclerView.apply {
            this.adapter = contestsAdapter
            addItemDecoration(GridSpacingItemDecoration(1, 30, true))
        }
    }

    private fun configureToolbar() {
        homeToolbar.setTitle(resources.getString(R.string.app_name))
        homeToolbar.addActions(arrayOf(Action.Drawer), this)
    }

    override fun onResume() {
        super.onResume()
        getContests()
    }

    private fun getContests() {
        showProgressView()
        viewModel.getContests().observe(this, Observer<List<FBContest>> {
            contestsAdapter.submitList(it)
            hideProgressView()
        })
    }

    private fun registerViewModel() {
        viewModel.dataStream.observe(
            viewLifecycleOwner,
            Observer { t ->
                when (t.state) {
                    State.INIT -> {
                    }
                    State.LOADING -> {
                        //    showProgressView()
                    }
                    State.NEXT -> {
                        hideProgressView()
                        handleNext(t.data)
                    }
                    State.ERROR -> {
                        t.throwable?.let { handleError(it) }
                    }
                    State.COMPLETE -> {
                        hideProgressView()
                    }
                }
            })
    }

    private fun handleNext(result: Event<MainDataState>?) {
//        result?.let { responseEvent ->
//            if (!responseEvent.consumed) {
//                responseEvent.consume()?.let { response ->
//                    when (response) {
//                        is GetContestsSuccess -> handleGetContestsSuccess(response)
//                        GetContestsFailure -> TODO()
//                    }
//                }
//            }
//        }
    }

    private fun handleError(throwable: Throwable) {
        hideProgressView()
        errorHandler.handleError(this, throwable)
    }

//    private fun handleGetContestsSuccess(response: GetContestsSuccess) {
//        contestsAdapter.submitList(response.contestsList)
//    }

    override fun onActionSelected(action: AbstractAction): Boolean {
        if (action == Action.Drawer) {
            Timber.i("onActionSelected - Drawer")
            (activity as MainActivity).openDrawer()
            return true
        }

        return false
    }

    private fun showProgressView() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressView() {
        progressBar.visibility = View.GONE
    }

    override fun onItemClick(data: FBContest) {
        val transferInfo = TransferInfo()
        transferInfo.contest = data
        navigator.replace(ContestDetailsScreen(transferInfo))
    }

    override val isDrawerEnabled: Boolean
        get() = true
}