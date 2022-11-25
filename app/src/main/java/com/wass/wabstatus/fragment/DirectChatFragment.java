package com.wass.wabstatus.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wass.wabstatus.R;
import com.wass.wabstatus.util.SharedPrefs;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class DirectChatFragment extends Fragment {

    CountryCodePicker ccp;
    EditText edtPhoneNumber, msg_edt;
    LinearLayout wapp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return inflater.inflate(R.layout.fragment_direct_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
        edtPhoneNumber = view.findViewById(R.id.phone_number_edt);
//        ccp.registerPhoneNumberTextView(edtPhoneNumber);
        ccp.setDefaultCountryUsingNameCode(SharedPrefs.getCountryNameCode(requireActivity()));
        wapp = view.findViewById(R.id.wapp);
        wapp.requestFocus();
//        msg_edt = view.findViewById(R.id.msg_edt);
        wapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect();
                SharedPrefs.setCountryNameCode(requireActivity(), ccp.getSelectedCountryNameCode());
            }
        });
    }

    void redirect() {
        if (TextUtils.isEmpty(edtPhoneNumber.getText().toString())) {
            Toast.makeText(requireContext(), R.string.select_country, Toast.LENGTH_SHORT).show();
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="
                        + ccp.getSelectedCountryCode()
                        + edtPhoneNumber.getText().toString()));
//                        + "&text=" + msg_edt.getText().toString()));

                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Install WhatsApp First...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
