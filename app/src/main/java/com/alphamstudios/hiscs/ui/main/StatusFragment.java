package com.alphamstudios.hiscs.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alphamstudios.hiscs.HISCSData;
import com.alphamstudios.hiscs.HISCSInterface;
import com.alphamstudios.hiscs.MainActivity;
import com.alphamstudios.hiscs.R;

import java.text.DecimalFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class StatusFragment extends Fragment {

    Activity main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    EditText invRatingET, batteryCapET;
    Button btnRecalc;
    TextView lA, lB, lC, lD;
    TextView tC, mC, bC, bT;

    HISCSData data;

    double totalCurrent, bCharge, bTime;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status, container, false);

        invRatingET = root.findViewById(R.id.et_inverter_rating);
        batteryCapET = root.findViewById(R.id.et_battery_capacity);
        btnRecalc = root.findViewById(R.id.btn_recalculate);

        lA = root.findViewById(R.id.tv_load_a);
        lB = root.findViewById(R.id.tv_load_b);
        lC = root.findViewById(R.id.tv_load_c);
        lD = root.findViewById(R.id.tv_load_d);

        tC = root.findViewById(R.id.tv_total_current);
        mC = root.findViewById(R.id.tv_max_current);
        bC = root.findViewById(R.id.tv_battery_charge);
        bT = root.findViewById(R.id.tv_battery_time);

        btnRecalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HISCSInterface) main).recalculate();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((HISCSInterface) main).refreshData();
    }

    public void setHISCSData(HISCSData d)
    {
        data = d;
        update();
    }

    private static DecimalFormat df = new DecimalFormat("00.00");
    private void update()
    {
        if (lA == null) return;

        totalCurrent = 0;
        for (int i = 0; i < 4; i++) {
            totalCurrent += data.loadCurrents[i];
        }

        bCharge = 0;
        bTime = 0;
        double invRating = 0;
        double bCap = 0;

        try {
            invRating = Double.parseDouble(invRatingET.getText().toString());
            bCap = Double.parseDouble(batteryCapET.getText().toString());
        } catch (Exception e) {
            //Toast.makeText(getContext(), "Inverter rating or battery capacity invalid.", Toast.LENGTH_LONG).show();
        }

        bCharge = bCap;
        bTime = invRating;

        lA.setText(df.format(data.loadCurrents[0]));
        lB.setText(df.format(data.loadCurrents[1]));
        lC.setText(df.format(data.loadCurrents[2]));
        lD.setText(df.format(data.loadCurrents[3]));

        tC.setText(df.format(totalCurrent));
        mC.setText(df.format(((MainActivity) main).maxAllowedCurrent));
        bC.setText(df.format(bCharge));
        bT.setText(df.format(bTime));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            main = (Activity) context;
        }
    }

}