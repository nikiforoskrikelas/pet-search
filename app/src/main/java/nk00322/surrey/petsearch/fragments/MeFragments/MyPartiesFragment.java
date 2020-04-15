package nk00322.surrey.petsearch.fragments.MeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petsearch.R;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPartiesFragment extends Fragment {
    private View view;

    public MyPartiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_parties, container, false);
        initViews();
        setListeners();
        return view;

    }


    private void initViews() {
    }


    private void setListeners() {

    }
}
