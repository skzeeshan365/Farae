package com.reiserx.farae.Models;

public class deviceInfo {
    private String model, id, manufacturer, versionCode, appVersion;
    int sdk;

    public deviceInfo(String model, String id, String manufacturer, int sdk, String versionCode, String appVersion) {
        this.model = model;
        this.id = id;
        this.manufacturer = manufacturer;
        this.sdk = sdk;
        this.versionCode = versionCode;
        this.appVersion = appVersion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getSdk() {
        return sdk;
    }

    public void setSdk(int sdk) {
        this.sdk = sdk;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
