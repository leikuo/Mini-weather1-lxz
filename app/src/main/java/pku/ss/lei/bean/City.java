package pku.ss.lei.bean;

/**
 * Created by Administrator on 2016/11/20.
 */
public class City {
    private String province;
    private String city;
    private String number;
    private String firstpy;
    private String allpy;
    private String allfirstpy;

    public City(String province, String city, String number, String firstpy, String allpy, String
                allfirstpy){
        this.province = province;
        this.city = city;
        this.number = number;
        this.firstpy = firstpy;
        this.allpy = allpy;
        this.allfirstpy = allfirstpy;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFirstpy() {
        return firstpy;
    }

    public void setFirstpy(String firstpy) {
        this.firstpy = firstpy;
    }

    public String getAllpy() {
        return allpy;
    }

    public void setAllpy(String allpy) {
        this.allpy = allpy;
    }

    public String getAllfirstpy() {
        return allfirstpy;
    }

    public void setAllfirstpy(String allfirstpy) {
        this.allfirstpy = allfirstpy;
    }
}
