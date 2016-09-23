package com.aki.photoeditor.udacity_capstone;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import static com.aki.photoeditor.udacity_capstone.EditImage.brightness;


/**
 * A simple {@link Fragment} subclass.
 */
public class StrengthFragment extends Fragment {

    private int PROCESS_TYPE;
    private final static int PROCESS_BRIGHTNESS = 1;
    private int strength;
    public StrengthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int max = getArguments().getInt("strength");
        int initial = getArguments().getInt("initial");

        PROCESS_TYPE = PROCESS_BRIGHTNESS;

        View root = inflater.inflate(R.layout.fragment_strength, container, false);
        Button saveButton = (Button) root.findViewById(R.id.applyButtonForAll);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        Button discardButton = (Button) root.findViewById(R.id.backButtonForAll);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((StrengthCallBacks) getActivity()).discardEffect();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        SeekBar seekBar = (SeekBar) root.findViewById(R.id.seekerForEverything);
        seekBar.setMax(max);
        if (brightness == -1)
            seekBar.setProgress(initial);
        else
            seekBar.setProgress(brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                switch (PROCESS_TYPE){
                    case PROCESS_BRIGHTNESS:
                        strength = progress - 100;
                        brightness = progress;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((StrengthCallBacks) getActivity()).processStrength(PROCESS_TYPE,strength);
            }
        });
        return root;
    }

    public interface StrengthCallBacks {
        void processStrength(int process_type,int value);
        void discardEffect();
    }

}
