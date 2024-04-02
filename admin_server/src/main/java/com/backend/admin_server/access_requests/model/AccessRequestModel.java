package com.backend.admin_server.access_requests.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Date;

@DynamoDBTable(tableName = "admin_db")
public class AccessRequestModel {

    private String requestId;
    private Integer userId;
    private Date date;
    private String approvalStatus;
    private String base64Image;

    @DynamoDBHashKey(attributeName = "request_Id")
    @DynamoDBAutoGeneratedKey
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @DynamoDBAttribute(attributeName = "user_Id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "date")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = "approval_status")
    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    @DynamoDBAttribute(attributeName = "request_image")
    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

}
