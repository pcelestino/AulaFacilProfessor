package br.edu.ffb.pedro.aulafacilprofessor.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ffb.pedrosilveira.easyp2p.EasyP2pDevice;

import java.util.ArrayList;

import br.edu.ffb.pedro.aulafacilprofessor.R;
import br.edu.ffb.pedro.aulafacilprofessor.adapters.holders.StudentsListViewHolder;

public class StudentsListAdapter extends RecyclerView.Adapter {

    private ArrayList<EasyP2pDevice> studentDevices;
    private Context context;

    public StudentsListAdapter(ArrayList<EasyP2pDevice> studentDevices, Context context) {
        this.studentDevices = studentDevices;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new StudentsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StudentsListViewHolder studentsListViewHolder = (StudentsListViewHolder) holder;
        EasyP2pDevice easyP2pDevice = studentDevices.get(position);
        studentsListViewHolder.studentDeviceReadableName.setText(easyP2pDevice.readableName);

        if (easyP2pDevice.isLeader) {
            studentsListViewHolder.studentDeviceLeaderIcon.setImageResource(R.drawable.ic_turned_in);
        } else {
            studentsListViewHolder.studentDeviceLeaderIcon.setImageResource(R.drawable.ic_turned_in_not);
        }
    }

    @Override
    public int getItemCount() {
        return studentDevices.size();
    }
}
