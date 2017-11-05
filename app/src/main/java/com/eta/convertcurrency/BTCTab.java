package com.eta.convertcurrency;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;

import org.json.JSONObject;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mopahshewuri on 11/3/17.
 */

public class BTCTab extends Fragment
{
    TextView singleBtcDollarValue, singleEtcDollarValue, conversionOutput;
    EditText editText;
    private ProgressDialog progressDialog;
    CardView cardView;
    Spinner spinner;
    SharedPreferences sendNairaValue;
    Double editBtc, singleBTCDollarEquiv;

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        View v = inflater.inflate(R.layout.btc_activity, container, false);


        if(!isConnected(getActivity()))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle("Internet Connectivity Issues");
            builder.setMessage("Device is Offline. Please Connect To Network");
            builder.setPositiveButton("OK!!!", new DialogInterface.OnClickListener()
            { @Override  public void onClick(DialogInterface dialog, int id) { } });
            builder.create().show();
        }
        else
        {
            //Device Has Internet Access Make Request.
            String[] userAction = {"NAIRA - Nigeria", "GBP - British Pound", "ING - Indian Ruppe", "AUD - Austrialian Dollar", "CAD - Canadian Dollar", "SGD - Singapore Dollar", "ARS - Argentine Peso", "MYR - Malaysian Riggit", "JPY - Japanese Yen",
                    "CNY - Chinese Yuan Renminbi", "NZD - New Zealand Dollar", "ZAR - South Africa Rand", "BRL - Brazilian Real",
                    "SAR - Saudi Arabian Riyal", "KES - Kenyan Shilling", "KRW - South Korean Won", "GHS - Ghanaian Cedi",
                    "AOA - Angolan Kwanza", "RUB - Russian Ruble", "EGP - Egyptian POUNDS"};

            cardView = (CardView) v.findViewById(R.id.btc_card);
            singleBtcDollarValue = (TextView) v.findViewById(R.id.single_btc_dollar_value);

            TextView Btc = (TextView) v.findViewById(R.id.BTC);
            conversionOutput = (TextView) v.findViewById(R.id.conversion_output);

            editText = (EditText) v.findViewById(R.id.get_edittext);
            spinner = (Spinner) v.findViewById(R.id.btc_spinner);
            ArrayAdapter aa = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, userAction);
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(aa);
            spinner.setSelection(0, false);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("...Getting Conversion Rates...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);

            //Make API Call
            new HandleAPIRequest().execute("https://min-api.cryptocompare.com/data/pricemulti");

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
                {
                    Double coinAmount;
                    if (editText.getText().toString().equals("")) { coinAmount = 1.0; } else { coinAmount = Double.valueOf(editText.getText().toString()); }
                    conversionOutput.setText(String.valueOf(calculateCoinValue(coinAmount, getSingleBTCDollarEquiv(), position)));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            editText.addTextChangedListener(new TextWatcher()
            {
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void afterTextChanged(Editable s)
                {
                    String g = s.toString();
                    if (!g.equals("")) { conversionOutput.setText(String.valueOf(calculateCoinValue(Double.valueOf(g), getSingleBTCDollarEquiv(), spinner.getSelectedItemPosition()))); }
                }
            });




            //Toast.makeText(getActivity(), "Device Connected", Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    private class HandleAPIRequest extends AsyncTask<String, String, String>
    {

        @Override
        protected void onPreExecute() { progressDialog.show(); }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                final String url = params[0]+"?fsyms=BTC,ETH&tsyms=USD,EUR&e=Coinbase&extraParams=your_app_name";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                return restTemplate.getForObject(url, String.class);
            }
            catch (Exception e) { Log.e("MainActivity", e.getMessage(), e); }

            return null;
        }

        @Override
        protected void onPostExecute(String response)
        {
            progressDialog.dismiss();

            JSONObject response_obj = makeJSONObjectFromString(response);

            String btc_dollar_value = getResponseValue(makeJSONObjectFromString(getResponseValue(response_obj,"BTC")),"USD");

            singleBtcDollarValue.setText( "$"+ btc_dollar_value);

            setSingleBTCDollarEquiv(Double.valueOf(btc_dollar_value));
        }

    }

    public JSONObject makeJSONObjectFromString(String jsonString)
    {
        JSONObject jsonObj = null;
        try { jsonObj = new JSONObject(jsonString); }  catch (Exception e) { e.printStackTrace(); }
        return jsonObj;
    }

    public String getResponseValue(JSONObject response, String value)
    {
        String response_value = null;
        try { response_value = response.getString(value); } catch (Exception e) { e.printStackTrace(); }
        return response_value;
    }

    public void setSingleBTCDollarEquiv(Double value) { this.singleBTCDollarEquiv=value; }
    public Double getSingleBTCDollarEquiv() { return this.singleBTCDollarEquiv; }

    public String calculateCoinValue(Double coinAmount, Double coinValue, int spinnerPos)
    {
        Double result=0.0; String currency_symbol=new String();

        DecimalFormat dec = new DecimalFormat("#,##0.00");
        if(spinnerPos==0){ result =  (coinValue/0.00281)*coinAmount; currency_symbol="₦"; }
        else if(spinnerPos==1){ result =  (coinValue/1.30765)*coinAmount; currency_symbol="£"; }
        else if(spinnerPos==2){ result =  (coinValue/0.01549)*coinAmount; currency_symbol="₹"; }
        else if(spinnerPos==3){ result =  (coinValue/0.76542)*coinAmount; currency_symbol="A$"; }
        else if(spinnerPos==4){ result =  (coinValue/0.78384)*coinAmount; currency_symbol="$"; }
        else if(spinnerPos==5){ result =  (coinValue/0.73268)*coinAmount; currency_symbol="$"; }
        else if(spinnerPos==6){ result =  (coinValue/0.05673)*coinAmount; currency_symbol="$"; }
        else if(spinnerPos==7){ result =  (coinValue/0.23609)*coinAmount; currency_symbol="RM"; }
        else if(spinnerPos==8){ result =  (coinValue/0.00877)*coinAmount; currency_symbol="¥"; }
        else if(spinnerPos==9){ result =  (coinValue/0.15070)*coinAmount; currency_symbol="¥"; }
        else if(spinnerPos==10){ result =  (coinValue/0.69071)*coinAmount; currency_symbol="$"; }
        else if(spinnerPos==11){ result =  (coinValue/0.07025)*coinAmount; currency_symbol="R"; }
        else if(spinnerPos==12){ result =  (coinValue/0.30179)*coinAmount; currency_symbol="R$"; }
        else if(spinnerPos==13){ result =  (coinValue/0.26599)*coinAmount; currency_symbol="﷼"; }
        else if(spinnerPos==14){ result =  (coinValue/0.00964)*coinAmount; currency_symbol="KSh";}
        else if(spinnerPos==15){ result =  (coinValue/0.00090)*coinAmount; currency_symbol="₩"; }
        else if(spinnerPos==16){ result =  (coinValue/0.22465)*coinAmount; currency_symbol="GH¢"; }
        else if(spinnerPos==17){ result =  (coinValue/0.00603)*coinAmount; currency_symbol="Kz"; }
        else if(spinnerPos==18){ result =  (coinValue/0.01691)*coinAmount; currency_symbol="руб"; }
        else if(spinnerPos==19){ result =  (coinValue/0.05666)*coinAmount; currency_symbol="£"; }
        return (currency_symbol+""+dec.format(result));
    }


    //Network call
    public Boolean isConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
