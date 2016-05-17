package com.samset.facebooklogin.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;

import com.samset.facebooklogin.MainActivity;
import com.samset.facebooklogin.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener,
        SocialNetworkManager.OnInitializationCompleteListener, OnLoginCompleteListener {


    private View view;
    public static SocialNetworkManager networkManager;
    private Button btnfb;

    //Give permissions
    ArrayList<String> fbpermission = new ArrayList<String>();


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        initView(view);

        fbpermission.addAll(Arrays.asList("public_profile, email, user_friends"));
        networkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(MainActivity.SOCIAL_NETWORK_TAG);

        return view;
    }

    private void initView(View view) {
        btnfb = (Button) view.findViewById(R.id.facebook);
        btnfb.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (networkManager == null) {
            networkManager = new SocialNetworkManager();
            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbpermission);
            networkManager.addSocialNetwork(fbNetwork);
            getFragmentManager().beginTransaction().add(networkManager, MainActivity.SOCIAL_NETWORK_TAG).commit();
            networkManager.setOnInitializationCompleteListener(this);

        } else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if (!networkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = networkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    initSocialNetwork(socialNetwork);
                }
            }
        }

    }

    private void initSocialNetwork(SocialNetwork socialNetwork) {
        if (socialNetwork.isConnected()) {
            switch (socialNetwork.getID()) {
                case FacebookSocialNetwork.ID:
                    btnfb.setText("Show Facebook profile");
                    break;

            }
        }
    }

    @Override
    public void onClick(View v) {
        int networkId = FacebookSocialNetwork.ID;

        SocialNetwork socialNetwork = networkManager.getSocialNetwork(networkId);
        if (!socialNetwork.isConnected()) {
            if (networkId != 0) {
                socialNetwork.requestLogin();
                MainActivity.showProgress("Loading social person");
            } else {
                Toast.makeText(getActivity(), "Wrong networkId", Toast.LENGTH_LONG).show();
            }
        } else {
            startProfile(socialNetwork.getID());
        }

    }

    private void startProfile(int networkId) {
        Toast.makeText(getActivity(), "You Login", Toast.LENGTH_LONG).show();

       ProfileFragment profile = ProfileFragment.newInstannce(networkId);
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack("profile")
                .replace(R.id.container, profile)
                .commit();
    }

    @Override
    public void onSocialNetworkManagerInitialized() {

        //when init SocialNetworks - get and setup login only for initialized SocialNetworks
        for (SocialNetwork socialNetwork : networkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
            initSocialNetwork(socialNetwork);
        }

    }

    @Override
    public void onLoginSuccess(int socialNetworkID) {
        MainActivity.hideProgress();
        startProfile(socialNetworkID);
    }

    @Override
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {
        MainActivity.hideProgress();
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }
}
