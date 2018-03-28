package com.example.hp.warmweather;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.warmweather.db.City;
import com.example.hp.warmweather.db.County;
import com.example.hp.warmweather.db.Province;
import com.example.hp.warmweather.util.HttpUtil;
import com.example.hp.warmweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    public static final int LEVE_PROVINCE=0;
    public static final int LEVE_CITY=1;
    public static final int LEVE_COUNTY=2;
    private ProgressDialog progressDialog;
    private ListView listView;
    private TextView title;
    private Button back;
    private ArrayAdapter<String> arrayAdapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLeve;
    private List<String> dataList=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        listView=view.findViewById(R.id.list_view);
        title=view.findViewById(R.id.title_text);
        back=view.findViewById(R.id.back_button);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1
        ,dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLeve==LEVE_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLeve==LEVE_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLeve==LEVE_CITY){
                    queryProvinces();
                }else if (currentLeve==LEVE_COUNTY){
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        title.setText("中国");
        back.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeve=LEVE_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromeServer(address,"province");
        }
    }
    private void queryCities(){
        title.setText(selectedProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?"
                ,String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeve=LEVE_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromeServer(address,"city");
        }
    }
    private void queryCounties(){
        title.setText(selectedCity.getCityName());
        back.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?"
                ,String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLeve=LEVE_COUNTY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"
                    +provinceCode+"/"+cityCode;
            queryFromeServer(address,"county");
        }
    }
    private void queryFromeServer(String address, final String type){
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgress();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if(type=="province"){
                   result= Utility.handleProvinceResponse(responseText);
                }else if (type=="city"){
                    result= Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if (type=="county"){
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(type=="province"){
                                queryProvinces();
                            }else if (type=="city"){
                                queryCities();
                            }else if (type=="county"){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
    private void showProgress(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgress(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
