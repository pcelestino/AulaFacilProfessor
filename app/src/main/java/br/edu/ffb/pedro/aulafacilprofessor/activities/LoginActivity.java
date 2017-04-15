package br.edu.ffb.pedro.aulafacilprofessor.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.ffb.pedrosilveira.easyp2p.EasyP2p;
import com.ffb.pedrosilveira.easyp2p.EasyP2pDataReceiver;
import com.ffb.pedrosilveira.easyp2p.EasyP2pDevice;
import com.ffb.pedrosilveira.easyp2p.EasyP2pServiceData;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pCallback;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pDataCallback;
import com.ffb.pedrosilveira.easyp2p.callbacks.EasyP2pDeviceCallback;
import com.ffb.pedrosilveira.easyp2p.payloads.Payload;
import com.ffb.pedrosilveira.easyp2p.payloads.bully.BullyElection;
import com.ffb.pedrosilveira.easyp2p.payloads.device.DeviceInfo;

import java.io.IOException;

import br.edu.ffb.pedro.aulafacilprofessor.R;
import br.edu.ffb.pedro.aulafacilprofessor.adapters.ProfessorsListAdapter;
import br.edu.ffb.pedro.aulafacilprofessor.payload.Quiz;

public class LoginActivity extends AppCompatActivity implements EasyP2pDataCallback {

    protected static final String TAG = "AulaFacilProfessor";
    private String professorInputName;

    private EasyP2pDataReceiver easyP2pDataReceiver;
    private EasyP2pServiceData easyP2pServiceData;
    private EasyP2p network;

    private RecyclerView professorsList;
    private ProfessorsListAdapter professorsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showProfessorInputNameDialog();
    }

    @SuppressLint("InflateParams")
    private void showProfessorInputNameDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(LoginActivity.this);
        View mView = layoutInflaterAndroid.inflate(R.layout.professor_input_name_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(LoginActivity.this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText studentInputNameDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(R.string.send, null);

        final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();

        alertDialogAndroid.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        professorInputName = studentInputNameDialogEditText.getText().toString();
                        if (professorInputName.isEmpty()) {
                            Toast.makeText(LoginActivity.this, R.string.please_insert_your_name,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            setupEasyP2P(professorInputName);
                            alertDialogAndroid.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogAndroid.show();
    }

    private void setupEasyP2P(String professorInputName) {

        easyP2pDataReceiver = new EasyP2pDataReceiver(LoginActivity.this, LoginActivity.this);
        easyP2pServiceData = new EasyP2pServiceData("AulaFacilProfessor", 50489, professorInputName);

        network = new EasyP2p(easyP2pDataReceiver, easyP2pServiceData, new EasyP2pCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Desculpe, esse dispositivo não suporta WiFi Direct.");
            }
        });

        network.startNetworkService(
                new EasyP2pDeviceCallback() {
                    @Override
                    public void call(EasyP2pDevice device) {
                        Log.d(TAG, "NOVO DISPOSITIVO CONECTADO!" +
                                "\nREADABLE NAME: " + device.readableName +
                                "\nINSTANCE NAME: " + device.instanceName +
                                "\nSERVICE NAME: " + device.serviceName +
                                "\nDEVICE NAME: " + device.deviceName);

                        professorsListAdapter.notifyDataSetChanged();
                    }
                },
                new EasyP2pDeviceCallback() {
                    @Override
                    public void call(EasyP2pDevice device) {
                        Log.d(TAG, "DISPOSITIVO DESCONECTADO!" +
                                "\nREADABLE NAME: " + device.readableName +
                                "\nINSTANCE NAME: " + device.instanceName +
                                "\nSERVICE NAME: " + device.serviceName +
                                "\nDEVICE NAME: " + device.deviceName);

                        professorsListAdapter.notifyDataSetChanged();
                    }
                });

        setupProfessorsList();
    }

    private void setupProfessorsList() {
        professorsList = (RecyclerView) findViewById(R.id.studentsList);
        professorsListAdapter = new ProfessorsListAdapter(network.registeredClients, LoginActivity.this);
        professorsList.setAdapter(professorsListAdapter);

        LinearLayoutManager studentsListLayoutManager = new LinearLayoutManager(LoginActivity.this,
                LinearLayoutManager.VERTICAL, false);

        professorsList.setLayoutManager(studentsListLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                network.startElection(
                        new EasyP2pDeviceCallback() {
                            @Override
                            public void call(EasyP2pDevice device) {
                                Log.d(TAG, "Pedido de eleição enviado com sucesso para: " + device.readableName);
                            }
                        }, new EasyP2pDeviceCallback() {
                            @Override
                            public void call(EasyP2pDevice device) {
                                Log.d(TAG, "Falha ao enviar o pedido de eleição para: " + device.readableName);
                            }
                        }
                );
                break;
            case R.id.menu_logout:
                AlertDialog alerta = new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Deseja realmente sair?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                network.stopNetworkService(false, new EasyP2pCallback() {
                                    @Override
                                    public void call() {
                                        finish();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Não", null)
                        .create();
                alerta.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataReceived(Object data) {
        Log.d(TAG, "Received network data.");

        try {
            Payload payload = LoganSquare.parse((String) data, Payload.class);

            switch (payload.type) {

                // O Líder irá enviar mensagens do tipo QUIZ
                case Quiz.TYPE:
                    final Quiz newQuiz;
                    newQuiz = LoganSquare.parse((String) data, Quiz.class);

                    Log.d(TAG, "MENSAGEM GERAL: " + String.valueOf(newQuiz.isLeader));  //See you on the other side!
                    break;

                // BULLY ELECTION
                case BullyElection.TYPE:
                    BullyElection bullyElection = LoganSquare.parse((String) data, BullyElection.class);

                    switch (bullyElection.message) {
                        case BullyElection.RESPOND_OK:
                            Log.d(TAG, "O Dispositivo " + bullyElection.device.readableName + " retornou OK");
                            break;
                        case BullyElection.INFORM_LEADER:
                            final EasyP2pDevice leader = bullyElection.device;
                            network.updateLeaderReference(leader, new EasyP2pCallback() {
                                @Override
                                public void call() {
                                    network.registeredLeader = leader;
                                    professorsListAdapter.notifyDataSetChanged();
                                }
                            });
                            break;
                    }
                    break;

                case DeviceInfo.TYPE:
                    DeviceInfo deviceInfo = LoganSquare.parse((String) data, DeviceInfo.class);

                    switch (deviceInfo.message) {
                        case DeviceInfo.REMOVE_DEVICE:
                            network.removeDeviceReference(deviceInfo.device, null);
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        network.stopNetworkService(false, null);
    }
}
