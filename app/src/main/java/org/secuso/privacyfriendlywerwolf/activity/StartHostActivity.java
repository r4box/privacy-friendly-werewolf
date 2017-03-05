package org.secuso.privacyfriendlywerwolf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlywerwolf.R;
import org.secuso.privacyfriendlywerwolf.context.GameContext;
import org.secuso.privacyfriendlywerwolf.helpers.PermissionHelper;
import org.secuso.privacyfriendlywerwolf.model.Player;
import org.secuso.privacyfriendlywerwolf.server.ServerGameController;
import org.secuso.privacyfriendlywerwolf.util.Constants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * StartHostActivity is the default page to start a game host
 * It waits for other clients to connect by creating a new Thread
 *
 * @author Florian Staubach <florian.staubach@stud.tu-darmstadt.de>
 * @author Tobias Kowalski <tobias.kowalski@stud.tu-darmstadt.de>
 */
public class StartHostActivity extends BaseActivity {

    TextView infoip;
    Toolbar toolbar;
    Button buttonStart;
    Button buttonAbort;
    FloatingActionButton nextButton;
    ServerGameController serverGameController;
    private static final String TAG = "StartHostActivity";


    //TODO: use custom Player Adapter !!!!
   // private List<Player> players;
    private ArrayList<String> stringPlayers;
    private ArrayAdapter<String> playerAdapter;
    // private ArrayAdapter<Player> playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverGameController = serverGameController.getInstance();
        serverGameController.setStartHostActivity(this);
        //reset everything
        serverGameController.destroy();

        setContentView(R.layout.activity_start_host);
        infoip = (TextView) findViewById(R.id.infoip);

        infoip.setText(getIpAddress());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.startgame_subtitle);

        PermissionHelper.showWifiAlert(this);



        // start the server
        serverGameController.startServer();


        buttonStart = (Button) findViewById(R.id.btn_start);
        buttonAbort = (Button) findViewById(R.id.btn_cancel);
        nextButton = (FloatingActionButton) findViewById(R.id.next_fab);


        // user clicks the button to start the game
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
                nextButton.setVisibility(View.VISIBLE);
            }
        });

        // TODO: remove: click for next phase
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNextPhase();

            }
        });


        ListView list = (ListView) findViewById(R.id.host_player_list);


        stringPlayers = new ArrayList<>();
        fillStringPlayers();

        playerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringPlayers);
        list.setAdapter(playerAdapter);
        Intent intent = getIntent();
        serverGameController.prepareServerPlayer(intent.getStringExtra(Constants.PLAYERNAME_PUTEXTRA));
    }

    @Override
    protected void onDestroy() {
        //TODO: destroy, only on back button
        // serverGameController.destroy();
        super.onDestroy();

    }

    private void startNextPhase() {
        GameContext.Phase nextRound = serverGameController.startNextPhase();
        //Toast.makeText(StartHostActivity.this, "The following round will start soon: " + nextRound, Toast.LENGTH_SHORT).show();
    }

    //TODO: remove this
    private void fillStringPlayers() {
        stringPlayers.clear();
        List<Player> players = GameContext.getInstance().getPlayersList();
        for (Player player : players) {
            stringPlayers.add(player.getPlayerName());
        }
    }

    public void renderUI() {
        fillStringPlayers();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerAdapter.notifyDataSetChanged();
            }
        });

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += getResources().getString(R.string.startgame_use_this_ip) + " "
                                + inetAddress.getHostAddress();
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public void startGame() {
        serverGameController.initiateGame();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("Host", true);
        startActivity(intent);


    }
}