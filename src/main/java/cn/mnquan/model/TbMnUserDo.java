package cn.mnquan.model;

import java.util.Date;

public class TbMnUserDo {
    private String account;

    private String id;

    private String pwd;

    private String state;

    private String agencyId;

    private String errorTimes;

    private Date createdAt;

    private Date updatedAt;

    private String userName;

    private String isAgency;

    private String bindAccount;

    private String ownRate;

    private String teamRate;

    public String getOwnRate() {
        return ownRate;
    }

    public void setOwnRate(String ownRate) {
        this.ownRate = ownRate;
    }

    public String getTeamRate() {
        return teamRate;
    }

    public void setTeamRate(String teamRate) {
        this.teamRate = teamRate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(String errorTimes) {
        this.errorTimes = errorTimes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIsAgency() {
        return isAgency;
    }

    public void setIsAgency(String isAgency) {
        this.isAgency = isAgency;
    }

    public String getBindAccount() {
        return bindAccount;
    }

    public void setBindAccount(String bindAccount) {
        this.bindAccount = bindAccount;
    }
}