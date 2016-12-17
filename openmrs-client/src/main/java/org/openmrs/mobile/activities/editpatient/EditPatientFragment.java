package org.openmrs.mobile.activities.editpatient;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.openmrs.mobile.R;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.watcher.PatientBirthdateValidatorWatcher;
import org.openmrs.mobile.models.retrofit.Patient;
import org.openmrs.mobile.models.retrofit.Person;
import org.openmrs.mobile.models.retrofit.PersonAddress;
import org.openmrs.mobile.models.retrofit.PersonName;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;
import org.openmrs.mobile.utilities.ViewUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ykarim on 12/16/16.
 */

public class EditPatientFragment extends Fragment implements EditPatientContract.View{

    EditPatientContract.Presenter mPresenter;

    LocalDate birthdate;
    DateTime bdt;

    EditText edfname;
    EditText edmname;
    EditText edlname;
    EditText eddob;
    EditText edyr;
    EditText edmonth;
    EditText edaddr1;
    EditText edaddr2;
    EditText edcity;
    EditText edstate;
    EditText edcountry;
    EditText edpostal;

    RadioGroup gen;
    ProgressBar progressBar;

    TextView fnameerror;
    TextView lnameerror;
    TextView doberror;
    TextView gendererror;
    TextView addrerror;

    Button registerConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_patient_edit, container, false);
        resolveViews(root);
        addListeners();
        return root;
    }

    @Override
    public void setPresenter(EditPatientContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void finishEditActivity() {
        getActivity().finish();
    }

    @Override
    public void scrollToTop() {
        ScrollView scrollView=(ScrollView)this.getActivity().findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getPaddingTop());
    }

    @Override
    public void setErrorsVisibility(boolean givenNameError,
                                    boolean familyNameError,
                                    boolean dayOfBirthError,
                                    boolean addressError,
                                    boolean genderError) {
        if (givenNameError) {
            fnameerror.setVisibility(View.VISIBLE);
        }
        else {
            fnameerror.setVisibility(View.INVISIBLE);
        }

        if (familyNameError) {
            lnameerror.setVisibility(View.VISIBLE);
        }
        else {
            lnameerror.setVisibility(View.INVISIBLE);
        }

        if (dayOfBirthError) {
            doberror.setVisibility(View.VISIBLE);
        }
        else {
            doberror.setVisibility(View.GONE);
        }

        if (addressError) {
            addrerror.setVisibility(View.VISIBLE);
        }
        else {
            addrerror.setVisibility(View.GONE);
        }

        if (genderError) {
            gendererror.setVisibility(View.VISIBLE);
        }
        else {
            gendererror.setVisibility(View.GONE);
        }
    }

    /**
     * This creates a new patient. Make a new method that given ID will edit patient
     * @return
     */
    private Patient updatePatient(String id) {
        Person person = new Person();

        // Add address
        PersonAddress address = new PersonAddress();
        address.setAddress1(ViewUtils.getInput(edaddr1));
        address.setAddress2(ViewUtils.getInput(edaddr2));
        address.setCityVillage(ViewUtils.getInput(edcity));
        address.setPostalCode(ViewUtils.getInput(edpostal));
        address.setCountry(ViewUtils.getInput(edcountry));
        address.setStateProvince(ViewUtils.getInput(edstate));
        address.setPreferred(true);

        List<PersonAddress> addresses = new ArrayList<>();
        addresses.add(address);
        person.setAddresses(addresses);

        // Add names
        PersonName name = new PersonName();
        name.setFamilyName(ViewUtils.getInput(edlname));
        name.setGivenName(ViewUtils.getInput(edfname));
        name.setMiddleName(ViewUtils.getInput(edmname));

        List<PersonName> names = new ArrayList<>();
        names.add(name);
        person.setNames(names);

        // Add gender
        String[] genderChoices = {"M","F"};
        int index = gen.indexOfChild(getActivity().findViewById(gen.getCheckedRadioButtonId()));
        if (index != -1) {
            person.setGender(genderChoices[index]);
        }
        else {
            person.setGender(null);
        }

        // Add birthdate
        String birthdate = null;
        if(ViewUtils.isEmpty(eddob)) {
            if (!StringUtils.isBlank(ViewUtils.getInput(edyr)) || !StringUtils.isBlank(ViewUtils.getInput(edmonth))) {
                int yeardiff = ViewUtils.isEmpty(edyr)? 0 : Integer.parseInt(edyr.getText().toString());
                int mondiff = ViewUtils.isEmpty(edmonth)? 0 : Integer.parseInt(edmonth.getText().toString());
                LocalDate now = new LocalDate();
                bdt = now.toDateTimeAtStartOfDay().toDateTime();
                bdt = bdt.minusYears(yeardiff);
                bdt = bdt.minusMonths(mondiff);
                person.setBirthdateEstimated(true);
                birthdate = bdt.toString();
            }
        }
        else {
            birthdate = bdt.toString();
        }

        person.setBirthdate(birthdate);

        Patient currentPatient = new PatientDAO().findPatientByID(id);
        currentPatient.setPerson(person);
        return currentPatient;
    }

    @Override
    public void hideSoftKeys(){
        View view = this.getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(this.getActivity());
        }
        InputMethodManager inputMethodManager = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void setProgressBarVisibility(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showUpgradeRegistrationModuleInfo() {
        ToastUtil.notifyLong(getResources().getString(R.string.registration_core_info));
    }

    public static EditPatientFragment newInstance() {
        return new EditPatientFragment();
    }

    private void resolveViews(View v) {
        edfname = (EditText) v.findViewById(R.id.firstname);
        edmname = (EditText) v.findViewById(R.id.middlename);
        edlname = (EditText) v.findViewById(R.id.surname);
        eddob=(EditText)v.findViewById(R.id.dob);
        edyr=(EditText)v.findViewById(R.id.estyr);
        edmonth=(EditText)v.findViewById(R.id.estmonth);
        edaddr1=(EditText)v.findViewById(R.id.addr1);
        edaddr2=(EditText)v.findViewById(R.id.addr2);
        edcity=(EditText)v.findViewById(R.id.city);
        edstate=(EditText)v.findViewById(R.id.state);
        edcountry=(EditText)v.findViewById(R.id.country);
        edpostal=(EditText)v.findViewById(R.id.postal);

        gen=(RadioGroup)v.findViewById(R.id.gender);
        progressBar = (ProgressBar)v.findViewById(R.id.progress_bar);

        fnameerror=(TextView)v.findViewById(R.id.fnameerror);
        lnameerror=(TextView)v.findViewById(R.id.lnameerror);
        doberror=(TextView)v.findViewById(R.id.doberror);
        gendererror=(TextView)v.findViewById(R.id.gendererror);
        addrerror=(TextView)v.findViewById(R.id.addrerror);

        registerConfirm= (Button) v.findViewById(R.id.registerConfirm);

        setFormValues();
    }

    private void setFormValues() {
        Person person = new PatientDAO().findPatientByID(mPresenter.getPatientId()).getPerson();
        TextView.BufferType defaultBuffer = TextView.BufferType.EDITABLE;

        edfname.setText(person.getName().getGivenName(), defaultBuffer);
        edmname.setText(person.getName().getMiddleName(), defaultBuffer);
        edlname.setText(person.getName().getFamilyName(), defaultBuffer);

        DateFormat desiredDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String patientDOB = DateUtils.getDOBFromPatient(mPresenter.getPatientId(), desiredDateFormat);
        eddob.setText(patientDOB);

        edaddr1.setText(person.getAddress().getAddress1());
        edaddr2.setText(person.getAddress().getAddress2());
        edcity.setText(person.getAddress().getCityVillage());
        edstate.setText(person.getAddress().getStateProvince());
        edcountry.setText(person.getAddress().getCountry());
        edpostal.setText(person.getAddress().getPostalCode());

        if (person.getGender().compareToIgnoreCase("M") == 0) {
            gen.check(R.id.male);
        } else if (person.getGender().compareToIgnoreCase("F") == 0) {
            gen.check(R.id.female);
        }
    }

    private void addListeners() {
        gen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId)
            {
                gendererror.setVisibility(View.GONE);
            }
        });

        if (eddob != null) {
            eddob.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Person person = new PatientDAO().findPatientByID(mPresenter.getPatientId()).getPerson();

                    int cYear = DateUtils.getPatientDateData(person.getBirthdate(), DateUtils.YEAR_VALUE);
                    int cMonth = DateUtils.getPatientDateData(person.getBirthdate(), DateUtils.MONTH_VALUE);
                    int cDay = DateUtils.getPatientDateData(person.getBirthdate(), DateUtils.DAY_VALUE);

                    edmonth.getText().clear();
                    edyr.getText().clear();

                    DatePickerDialog mDatePicker=new DatePickerDialog(EditPatientFragment.this.getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            eddob.setText(selectedday+"/"+selectedmonth+"/"+selectedyear);
                            birthdate = new LocalDate(selectedyear, selectedmonth, selectedday);
                            bdt=birthdate.toDateTimeAtStartOfDay().toDateTime();
                        }
                    },cYear, cMonth, cDay);
                    mDatePicker.setTitle("Select Date");
                    mDatePicker.show();  }
            });
        }
        registerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.confirm(updatePatient(mPresenter.getPatientId()));
            }
        });

        TextWatcher textWatcher = new PatientBirthdateValidatorWatcher(eddob, edmonth, edyr);
        edmonth.addTextChangedListener(textWatcher);
        edyr.addTextChangedListener(textWatcher);
    }
}
