package br.edu.ffb.pedro.aulafacilprofessor.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.edu.ffb.pedro.aulafacilprofessor.R;
import br.edu.ffb.pedro.aulafacilprofessor.activities.MainActivity;
import br.edu.ffb.pedro.aulafacilprofessor.adapters.StudentsListAdapter;
import br.edu.ffb.pedro.aulafacilprofessor.events.MessageEvent;

public class StudentsFragment extends Fragment {

    private RecyclerView studentsList;
    private View mEmptyView;
    private StudentsListAdapter studentsListAdapter;
    private MainActivity mainActivity;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.students_list);
        setupStudentsList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.students_fragment, container, false);
        studentsList = (RecyclerView) view.findViewById(R.id.studentsList);
        mEmptyView = view.findViewById(R.id.emptyView);
        mainActivity = (MainActivity) getActivity();
        return view;
    }

    private void checkAdapterIsEmpty() {
        if (studentsListAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            studentsList.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            studentsList.setVisibility(View.VISIBLE);
        }
    }

    private void setupStudentsList() {
        studentsListAdapter = new StudentsListAdapter(mainActivity.network.registeredClients, mainActivity);

        studentsListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        LinearLayoutManager studentsListLayoutManager = new LinearLayoutManager(mainActivity,
                LinearLayoutManager.VERTICAL, false);

        studentsList.setLayoutManager(studentsListLayoutManager);
        studentsList.setHasFixedSize(true);
        studentsList.setAdapter(studentsListAdapter);
        checkAdapterIsEmpty();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals(MessageEvent.UPDATE_STUDENTS_LIST)) {
            studentsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
