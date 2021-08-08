package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //set up binding to the fragment_reminders view
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reminders, container, false)
        binding.viewModel = _viewModel

        //this sets up the options menu
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        //this calls the refreshLayout in the fragment_reminders xml to refresh data
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter { it -> //new code
            startActivity(
                Intent(requireContext(), ReminderDescriptionActivity::class.java).apply {
                    putExtra(Companion.EXTRA_ReminderDataItem, it)}
            )}

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    //this is for the logout on the LocationsReminders screen
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//                TO DO: add the logout implementation - Done
                //AuthUI.getInstance() below we simply use getInstance and import AuthUI
                AuthUI.getInstance()
                    .signOut(requireContext())
                    //  .addOnCompleteListener(this, OnCompleteListener<Void>() {
                    .addOnCompleteListener {// task ->   return to the login screen following
                        if (it.isSuccessful) {
                         //   val intent = Intent(context, AuthenticationActivity::class.java)
                            startActivity(Intent(activity, AuthenticationActivity::class.java))
                            activity?.finish()  //add activity.
                        } else {    /*let the use know if there is something*/
                            Snackbar.make(
                                requireView(), getString(R.string.logout_failed),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    companion object {      //for logging errors etc
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
    }
}
