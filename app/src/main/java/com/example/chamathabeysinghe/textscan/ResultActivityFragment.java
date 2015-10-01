package com.example.chamathabeysinghe.textscan;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ResultActivityFragment extends Fragment {

    public ResultActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_result, container, false);
        TextView textView=(TextView)rootView.findViewById(R.id.text_result);
        textView.setText(getActivity().getIntent().getStringExtra(MainActivityFragment.RESULT_TEXT));
        return rootView;
    }
}
