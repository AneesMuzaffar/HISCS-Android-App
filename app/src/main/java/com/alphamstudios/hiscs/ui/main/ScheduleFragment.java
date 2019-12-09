package com.alphamstudios.hiscs.ui.main;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alphamstudios.hiscs.HISCSData;
import com.alphamstudios.hiscs.HISCSInterface;
import com.alphamstudios.hiscs.MainActivity;
import com.alphamstudios.hiscs.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class ScheduleFragment extends Fragment {

    Activity main;
    HISCSData data;

    TextView tv_schedules;
    Switch action;

    char load = ' ';
    int seconds = 0;
    int h = 0;
    int m = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        data = new HISCSData();

        root.findViewById(R.id.rb_l_A).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLoad(v);
            }
        });
        root.findViewById(R.id.rb_l_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLoad(v);
            }
        });
        root.findViewById(R.id.rb_l_C).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLoad(v);
            }
        });
        root.findViewById(R.id.rb_l_D).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLoad(v);
            }
        });

        action = root.findViewById(R.id.s_action);
        tv_schedules = root.findViewById(R.id.tv_schedules);
        root.findViewById(R.id.btn_set_s_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                final int hour = c.get(Calendar.HOUR_OF_DAY);
                final int minute = c.get(Calendar.MINUTE);

                //Create and return a new instance of TimePickerDialog
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        int currentSeconds = (hour * 60 + minute) * 60;
                        int setSeconds = (hourOfDay * 60 + minuteOfHour) * 60;

                        h = hourOfDay;
                        m = minuteOfHour;

                        seconds = setSeconds - currentSeconds;

                        if (load == ' ') {
                            Toast.makeText(getActivity(), "Please select a load.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String act = action.isChecked() ? "1" : "0";
                        String s = String.format("%05d", seconds);
                        ((HISCSInterface) main).addSchedule(load + act + s);

                        String on_off = action.isChecked() ? "ON " : "OFF";
                        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                        try {
                            ((MainActivity) main).scheduleStrings[load - 'A'] = "Load " + load + ": Switch " + on_off + " at " +
                                    new SimpleDateFormat("hh:mm a").format(sdf.parse(h + ":" + m));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Failed to parse time.", Toast.LENGTH_LONG).show();
                        }

                        data.scheduleState[load - 'A'] = true;
                        update();

                        Toast.makeText(getActivity(), "Second Difference: " + seconds, Toast.LENGTH_LONG).show();
                    }
                }, hour, minute, DateFormat.is24HourFormat(getActivity())).show();
            }
        });

        root.findViewById(R.id.btn_cancel_all_schedules).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HISCSInterface) main).cancelAllSchedules();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((HISCSInterface) main).refreshData();
    }

    public void selectLoad(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.rb_l_A:
                if (checked)
                    load = 'A';
                    break;
            case R.id.rb_l_B:
                if (checked)
                    load = 'B';
                break;
            case R.id.rb_l_C:
                if (checked)
                    load = 'C';
                break;
            case R.id.rb_l_D:
                if (checked)
                    load = 'D';
                break;
        }
    }

    public void setHISCSData(HISCSData d)
    {
        data = d;
        update();
    }

    public void update()
    {
        if (tv_schedules == null) return;

        String scheduleStr = "";
        for (int i = 0; i < 4; i++)
        {
            if (!data.scheduleState[i]) continue;
            scheduleStr += ((MainActivity) main).scheduleStrings[i] + "\n";
        }
        tv_schedules.setText(scheduleStr);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            main = (Activity) context;
        }
    }
}