package com.example.blink22.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;

/**
 * Created by blink22 on 5/29/18.
 */
public class CrimeFragment extends Fragment{
    private static final int REQUEST_DATE = 0 ;
    private static final int REQUEST_TIME = 1 ;
    private static final int REQUEST_CONTACT = 2;
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_PERMISSION_CONTACTS = 3 ;
    private static final int REQUEST_PERMISSION_CALL = 4;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String TAG = "CrimeFragment";

    public static CrimeFragment newInstance(UUID crimeId){

        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();

        mTimeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstacne(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = ShareCompat.IntentBuilder.from(getActivity()).
                        setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .getIntent();
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},
                            REQUEST_PERMISSION_CONTACTS);
                }else{
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        });

        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mCallButton = (Button) v.findViewById(R.id.call_suspect);
        mCallButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mCrime.getSuspect() == null){
                    Toast.makeText(getActivity(), R.string.crime_report_no_suspect,Toast.LENGTH_SHORT)
                            .show();
                }
                else if(mCrime.getPhone() == null) {
                    Toast.makeText(getActivity(), R.string.no_suspect_phone,Toast.LENGTH_SHORT)
                            .show();
                }else if(ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {Manifest.permission.CALL_PHONE},
                            REQUEST_PERMISSION_CALL);
                }else{
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+mCrime.getPhone()));
                    startActivity(i);
                }
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
        if(requestCode == REQUEST_TIME){
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        }
        if(requestCode == REQUEST_CONTACT && data != null){

            Uri contactUri = data.getData();
            String[] queryFields = new String[]
                    {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
            ContentResolver cr = getActivity().getContentResolver();

            Cursor c = cr
                    .query(contactUri, queryFields, null, null, null);

            try{
                if(c.getCount()==0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phones = cr.query(
                        CommonDataKinds.Phone.CONTENT_URI, null,
                        CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null
                );
                phones.moveToFirst();
                String suspectPhone = phones.getString(phones.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                if(suspectPhone != null) {
                    mCrime.setPhone(suspectPhone);
                }
                phones.close();
            }finally {
                c.close();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void updateDate() {
        mDateButton.setText(formatDate(mCrime.getDate()));
    }

    private void updateTime(){
        mTimeButton.setText(formatTime(mCrime.getDate()));
    }

    private CharSequence formatDate(Date date){
        return DateFormat.format("EEEE, MMMM dd, yyyy",date);
    }

    private CharSequence formatTime(Date date){
        return DateFormat.format("HH : mm", date);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_delete_crime:
                CrimeLab crimeLab = CrimeLab.get(getActivity());
                crimeLab.deleteCrime(mCrime.getId());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    private String getCrimeReport(){
        String solvedString = mCrime.isSolved() ?
                getString(R.string.crime_report_solved)
                :getString(R.string.crime_report_unsolved);

        String dateString = formatDate(mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect() == null ?
                getString(R.string.crime_report_no_suspect)
                :getString(R.string.crime_report_suspect, mCrime.getSuspect());


        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CONTACTS:
                if(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }else{
                    Toast.makeText(getActivity(), R.string.permissions_required, Toast.LENGTH_SHORT).show();
                }
                return;
            case REQUEST_PERMISSION_CALL:
                if(grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+mCrime.getPhone()));
                    startActivity(i);
                }else{
                    Toast.makeText(getActivity(), R.string.permissions_required, Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
