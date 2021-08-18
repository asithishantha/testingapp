package com.example.testingapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.example.testingapp.Interface.EditImageFragmentListener;


public class EditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {



    private EditImageFragmentListener listener;
    SeekBar seekbar_brightness,seekbar_constrant, seekbar_saturation;


    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public EditImageFragment() {
        // Required empty public constructor
    }

//    // TODO: Rename and change types and number of parameters
//    public static EditImageFragment newInstance(String param1, String param2) {
//        EditImageFragment fragment = new EditImageFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView= inflater.inflate(R.layout.fragment_edit_image, container, false);

        seekbar_brightness= (SeekBar)itemView.findViewById(R.id.seekbar_brightness);
        seekbar_constrant= (SeekBar)itemView.findViewById(R.id.seekbar_constraint);
        seekbar_saturation= (SeekBar)itemView.findViewById(R.id.seekbar_saturation);


        seekbar_brightness.setMax(200);
        seekbar_brightness.setProgress(100);

        seekbar_constrant.setMax(20);
        seekbar_constrant.setProgress(0);

        seekbar_saturation.setMax(30);
        seekbar_saturation.setProgress(10);

        seekbar_saturation.setOnSeekBarChangeListener(this);
        seekbar_constrant.setOnSeekBarChangeListener( this);
        seekbar_brightness.setOnSeekBarChangeListener(this);

        return itemView;
      }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(listener !=null){
            if(seekBar.getId() == R.id.seekbar_brightness)
            {
                listener.onBrightnessChanged(progress-100);

            }
            else if(seekBar.getId() == R.id.seekbar_constraint){
                progress+=10;
                float value= .10f*progress;
                listener.onConstrantChanged(value);

            }
            else if(seekBar.getId() == R.id.seekbar_constraint){

                float value = .10f*progress;
                listener.onSaturationChanged(value);
            }

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener !=null)
           listener.onEditStarted();

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener !=null)
            listener.onEditCompleted();
    }

    public void restControls()
      {
          seekbar_brightness.setProgress(100);
          seekbar_constrant.setProgress(0);
          seekbar_saturation.setProgress(10);
      }


}