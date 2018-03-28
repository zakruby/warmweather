package com.example.hp.warmweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by HP on 2018/3/28.
 */

public class Province extends DataSupport{
    private String provinceName;
    private int id;
    private int provinceCode;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
