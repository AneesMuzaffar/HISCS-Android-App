package com.alphamstudios.hiscs.ui.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alphamstudios.hiscs.HISCSData;
import com.alphamstudios.hiscs.HISCSInterface;
import com.alphamstudios.hiscs.MainActivity;
import com.alphamstudios.hiscs.R;

import java.text.DecimalFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class SwitchFragment extends Fragment {

    Activity main;
    HISCSData data;

    Switch switch_a, switch_b, switch_c, switch_d;

    EditText max_current;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_switch, container, false);

        final Switch switch_inverter = root.findViewById(R.id.switch_inverter);
        switch_a = root.findViewById(R.id.switch_a);
        switch_b = root.findViewById(R.id.switch_b);
        switch_c = root.findViewById(R.id.switch_c);
        switch_d = root.findViewById(R.id.switch_d);

        final EditText max_current = root.findViewById(R.id.et_max_current);
        Button set_max_current = root.findViewById(R.id.btn_set_max_current);
        Button reinit = root.findViewById(R.id.btn_reinit);

        switch_inverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_inverter.isChecked()) {
                    ((HISCSInterface) main).setSwitch("I1");
                } else {
                    ((HISCSInterface) main).setSwitch("I0");
                }
            }
        });

        switch_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_a.isChecked()) {
                    ((HISCSInterface) main).setSwitch("A1");
                } else {
                    ((HISCSInterface) main).setSwitch("A0");
                }
            }
        });

        switch_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_b.isChecked()) {
                    ((HISCSInterface) main).setSwitch("B1");
                } else {
                    ((HISCSInterface) main).setSwitch("B0");
                }
            }
        });

        switch_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_c.isChecked()) {
                    ((HISCSInterface) main).setSwitch("C1");
                } else {
                    ((HISCSInterface) main).setSwitch("C0");
                }
            }
        });

        switch_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_d.isChecked()) {
                    ((HISCSInterface) main).setSwitch("D1");
                } else {
                    ((HISCSInterface) main).setSwitch("D0");
                }
            }
        });

        set_max_current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double current = 0.0;
                try {
                    current = Double.parseDouble(max_current.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(main, "Failed to cast to double: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                max_current.setText("");

                if (switch_inverter.isChecked()) {
                    if (current > 4 || current < 0) {
                        Toast.makeText(main, "Current must be between 0 and 4 amp in Inverter Mode", Toast.LENGTH_LONG).show();
                    } else {
                        setInverterCurrent(current);
                    }
                } else {
                    if (current > 40 || current < 0) {
                        Toast.makeText(main, "Current must be between 0 and 40 amp in Normal Mode", Toast.LENGTH_LONG).show();
                    } else {
                        setInverterCurrent(current);
                    }
                }
            }
        });

        reinit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HISCSInterface) main).setSwitch("R");
            }
        });

        return root;
    }


    private static DecimalFormat df = new DecimalFormat("00.00");

    @Override
    public void onResume() {
        super.onResume();

        ((HISCSInterface) main).refreshData();
    }

    String formatCurrent(double c)
    {
        return df.format(c);
    }

    public void setInverterCurrent(double c)
    {
        ((HISCSInterface) main).setSwitch("M" + formatCurrent(c).replace(".", ""));
        ((MainActivity) main).maxAllowedCurrent = c;
    }

    public void setHISCSData(HISCSData d)
    {
        data = d;
        update();
    }

    public void update()
    {
        if (switch_a == null) return;

        switch_a.setChecked(data.state[0]);
        switch_b.setChecked(data.state[1]);
        switch_c.setChecked(data.state[2]);
        switch_d.setChecked(data.state[3]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            main = (Activity) context;
        }
    }
}