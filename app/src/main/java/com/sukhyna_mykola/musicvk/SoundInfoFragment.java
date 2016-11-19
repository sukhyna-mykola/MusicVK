package com.sukhyna_mykola.musicvk;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by mikola on 25.10.2016.
 */

public class SoundInfoFragment extends Fragment {

    int id;
    Sound sound;
    String size;
    public static final String ARG_ID_SOUND = "com.sukhyna_mykola.vkmusic.ARG_ID_SOUND";
    private TextView sizeView;
    public static final String Tag = "SoundInfoFragment";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments().getInt(ARG_ID_SOUND);
    }

    static SoundInfoFragment newInstance(int id) {
        SoundInfoFragment pageFragment = new SoundInfoFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_ID_SOUND, id);
        pageFragment.setArguments(arguments);
        return pageFragment;

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.sound_info_fragment, container, false);
        sound = SoundLab.get().getSound(id);
        ((TextView) v.findViewById(R.id.title_sound_info)).setText(sound.getTitle());
        ((TextView) v.findViewById(R.id.artist_sound_info)).setText(sound.getArtist());
        ((TextView) v.findViewById(R.id.time_sound_info)).setText(Constants.getTimeString(sound.getDuration())+" min");
        ((TextView) v.findViewById(R.id.genre_sound_info)).setText(sound.getGenre());
        sizeView = ((TextView) v.findViewById(R.id.size_sound_info));

        new getText().execute(v);



        return v;
    }


    private class getText extends AsyncTask<View, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(final View... params) {
            if (sound.getId_lyriks() != 0) {
                Log.d(Tag,"sound.getId_lyriks() != 0");
                VKRequest request = VKApi.audio().getLyrics(VKParameters.from("lyrics_id", sound.getId_lyriks()));
                Log.d(Tag,"VKRequest request");
                request.executeWithListener(new VKRequest.VKRequestListener() {

                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Log.d(Tag,"onComplete");
                        String text = (String) response.responseString;
                        Log.d(Tag,"text < = "+text);

                        try {
                            JSONObject reader = new JSONObject(text);
                            JSONObject sys = reader.getJSONObject("response");
                            text = sys.getString("text");
                            Log.d(Tag,"text > = "+text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ((TextView) params[0].findViewById(R.id.text_sound_info)).setText(text);
                    }
                });
            }else
            Log.d(Tag," Constants.SizeFile pre");
            size = Constants.SizeFile(sound.getUrl());
            Log.d(Tag," Constants.SizeFile after");
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(Tag, "pre");
                            sizeView.setText(size + " mb");
                            Log.d(Tag, "after");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Log.d(Tag, "error");
                        }
                    }
                });

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

    }


}
