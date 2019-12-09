package com.alphamstudios.hiscs.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.widget.Toast;

import com.alphamstudios.hiscs.HISCSData;
import com.alphamstudios.hiscs.HISCSInterface;
import com.alphamstudios.hiscs.MainActivity;
import com.alphamstudios.hiscs.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PriorityFragment extends Fragment {

    Activity main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    TextView p1, p2, p3, p4;

    HISCSData data;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_priority, container, false);

        p1 = root.findViewById(R.id.p1);
        p2 = root.findViewById(R.id.p2);
        p3 = root.findViewById(R.id.p3);
        p4 = root.findViewById(R.id.p4);

        root.findViewById(R.id.btn_reset_p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyPriority("1234");
                refreshPriorityDisplay();
            }
        });

        root.findViewById(R.id.btn_set_p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPriority();
            }
        });
        return root;
    }

    void askPriority() {
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle("Set Priority");

        final EditText input = new EditText(main);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pS = input.getText().toString().trim();

                if (validPriority(pS)) {
                    applyPriority(pS);
                } else {
                    Toast.makeText(main, "Invalid priority. Example: 1234", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        ((HISCSInterface) main).refreshData();
    }

    void refreshPriorityDisplay()
    {
        if (p1 == null) return;

        p1.setText("Load " + (char)(((MainActivity) main).priority[0] + 'A'));
        p2.setText("Load " + (char)(((MainActivity) main).priority[1] + 'A'));
        p3.setText("Load " + (char)(((MainActivity) main).priority[2] + 'A'));
        p4.setText("Load " + (char)(((MainActivity) main).priority[3] + 'A'));
    }

    public void setHISCSData(HISCSData d)
    {
        data = d;
        refreshPriorityDisplay();
    }

    boolean validPriority(String pS)
    {
        return pS.length() == 4
                && pS.contains("1")
                && pS.contains("2")
                && pS.contains("3")
                && pS.contains("4");
    }

    void applyPriority(String pS)
    {
        String appliedPS = "";
        for (int i = 0; i < 4; i++) {
            ((MainActivity) main).priority[i] = pS.charAt(i) - '0' - 1;
            appliedPS += String.valueOf(((MainActivity) main).priority[i]);
        }

        ((HISCSInterface) main).setPriority(appliedPS);
        refreshPriorityDisplay();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            main = (Activity) context;
        }
    }
}