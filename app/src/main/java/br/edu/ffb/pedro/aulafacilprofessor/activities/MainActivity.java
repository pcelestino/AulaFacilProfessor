package br.edu.ffb.pedro.aulafacilprofessor.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import br.edu.ffb.pedro.aulafacilprofessor.R;
import br.edu.ffb.pedro.aulafacilprofessor.events.MessageEvent;
import br.edu.ffb.pedro.aulafacilprofessor.fragments.StudentsFragment;
import br.edu.ffb.pedro.aulafacilprofessor.payload.Quiz;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EasyP2pDataCallback {

    protected static final String TAG = "AulaFacilProfessor";
    private LinearLayout contentMain;
    private Toolbar toolbar;
    private String professorInputName = "";
    private TextView drawerTitle;

    public EasyP2p network;
    private EasyP2pDataReceiver easyP2pDataReceiver;
    private EasyP2pServiceData easyP2pServiceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        showProfessorInputNameDialog();
    }

    private void setupLayout() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
        contentMain = (LinearLayout) findViewById(R.id.content_main);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_students_list);
        navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                navigationView.removeOnLayoutChangeListener(this);
                drawerTitle = (TextView) navigationView.findViewById(R.id.drawerTitle);
            }
        });
    }

    @SuppressLint("InflateParams")
    private void showProfessorInputNameDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
        View mView = layoutInflaterAndroid.inflate(R.layout.professor_input_name_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
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
                            Toast.makeText(MainActivity.this, R.string.please_insert_your_name,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            toolbar.setVisibility(View.VISIBLE);
                            drawerTitle.setText(professorInputName);
                            displaySelectedScreen(R.id.nav_students_list);
                            contentMain.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
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

        easyP2pDataReceiver = new EasyP2pDataReceiver(MainActivity.this, MainActivity.this);
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

                        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_STUDENTS_LIST));
                        //studentsListAdapter.notifyDataSetChanged();
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

                        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_STUDENTS_LIST));
                        //studentsListAdapter.notifyDataSetChanged();
                    }
                });

        //setupProfessorsList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displaySelectedScreen(int id) {

        Fragment fragment = null;

        switch (id) {
            case R.id.nav_students_list:
                fragment = new StudentsFragment();
                break;
            case R.id.nav_statistics:
                break;
            case R.id.nav_manage_quiz:
                break;
            case R.id.nav_tools:
                break;
            case R.id.nav_logout:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
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
                alertDialog.show();
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
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
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(contentMain.getId(), fragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
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
                                    EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_STUDENTS_LIST));
                                    //studentsListAdapter.notifyDataSetChanged();
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
