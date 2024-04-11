package com.backend.admin_server.access_requests.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.backend.admin_server.access_requests.enums.RequestStateEnums;

@DynamoDBTable(tableName = "requests")
public class AccessRequestModel {

    private String requestId;
    private Integer userId;
    private String date;
    private String approvalStatus;
    private String base64Image;
    @DynamoDBTypeConvertedEnum
    private RequestStateEnums state;

    public AccessRequestModel () {
        this.state = RequestStateEnums.AUTOMATED;
    }


    @DynamoDBAutoGeneratedKey
    @DynamoDBRangeKey(attributeName = "request_id")
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @DynamoDBAttribute(attributeName = "user_id")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @DynamoDBHashKey(attributeName = "date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public RequestStateEnums getState() {
        return state;
    }

    public void setState(RequestStateEnums state) {
        this.state = state;
    }
}
