package com.company.project.template.model.constant;

import com.company.project.common.messaging.ResponseQueueInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RabbitConstant {

    //--- QUEUE ---//
    public static final String Q_EXECUTE_PAYMENT_RESULT = "pn_execute_payment_result_q";
    public static final String DLQ_EXECUTE_PAYMENT_RESULT = "pn_execute_payment_result_dlq";


    //--- EXCHANGE ---//
    public static final String DLX_PN = "pn.dlx";
    public static final String X_PN_PAYMENT = "pn.payment.dx";


    //--- ROUTING KEY ---//
    public static final String K_EXECUTE_PAYMENT = "execute.payment";
    public static final String K_EXECUTE_PAYMENT_RESULT = "execute.payment.result";


    //--- RESPONSE QUEUE INFO ---//
    public static final ResponseQueueInfo RQI_EXECUTE_PAYMENT_RESULT
            = new ResponseQueueInfo(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT);

}
