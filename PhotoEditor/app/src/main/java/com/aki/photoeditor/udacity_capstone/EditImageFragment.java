package com.aki.photoeditor.udacity_capstone;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditImageFragment extends Fragment {

    public EditImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_image, container, false);

        Button brightness = (Button) rootView.findViewById(R.id.brightnessButton);

        brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CallBack) getActivity()).showSeekBar(200,100);
            }
        });

        return rootView;
    }
    public interface CallBack{
        void showSeekBar(int max, int initial);
    }
}
