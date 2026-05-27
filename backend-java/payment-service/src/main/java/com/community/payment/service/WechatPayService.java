package com.community.payment.service;

import com.community.payment.entity.PaymentTransaction;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.http.*;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务类 - 使用官方SDK
 */
@Service
public class WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayService.class);

    @Value("${wechat.pay.appid}")
    private String appid;

    @Value("${wechat.pay.mchid}")
    private String mchid;

    @Value("${wechat.pay.api-key}")
    private String apiKey;

    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;

    @Value("${wechat.pay.cert-path}")
    private String certPath;

    @Value("${wechat.pay.key-path}")
    private String keyPath;

    private final PaymentService paymentService;
    private JsapiService jsapiService;
    private RefundService refundService;

    public WechatPayService(PaymentService paymentService) {
        this.paymentService = paymentService;
        initWechatPayService();
    }

    /**
     * 初始化微信支付服务
     */
    private void initWechatPayService() {
        try {
            // 读取私钥
            String privateKeyContent = new String(Files.readAllBytes(Paths.get(keyPath)));
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 创建配置
            Config config = new RSAConfig.Builder()
                    .merchantId(mchid)
                    .privateKey(privateKey)
                    .merchantSerialNumber("") // 商户API证书序列号，需要从微信支付后台获取
                    .apiV3Key(apiKey)
                    .build();

            // 初始化服务
            jsapiService = new JsapiService.Builder().config(config).build();
            refundService = new RefundService.Builder().config(config).build();

            log.info("微信支付SDK初始化成功");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("微信支付SDK初始化失败", e);
            throw new RuntimeException("微信支付SDK初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统一下单接口
     * @param orderId 订单ID
     * @param amount 金额（元）
     * @param description 商品描述
     * @param openid 用户openid
     * @return 支付参数
     */
    public Map<String, String> createOrder(Long orderId, BigDecimal amount, String description, String openid) {
        try {
            // 1. 创建支付交易记录
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod(1) // 微信支付
                    .status(0) // 待支付
                    .build();
            transaction = paymentService.createTransaction(transaction);

            // 2. 构造下单请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(appid);
            request.setMchid(mchid);
            request.setDescription(description);
            request.setOutTradeNo(transaction.getTransactionNo());
            request.setNotifyUrl(notifyUrl);

            // 金额
            com.wechat.pay.java.service.payments.jsapi.model.Amount amountObj = new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue()); // 转换为分
            amountObj.setCurrency("CNY");
            request.setAmount(amountObj);

            // 支付者
            Payer payer = new Payer();
            payer.setOpenid(openid);
            request.setPayer(payer);

            // 3. 调用微信支付API
            PrepayResponse response = jsapiService.prepay(request);

            // 4. 生成前端支付参数
            return generatePayParams(response.getPrepayId());

        } catch (ServiceException e) {
            log.error("微信支付统一下单失败: {}", e.getErrorMessage(), e);
            throw new RuntimeException("微信支付统一下单失败: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new RuntimeException("创建微信支付订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单状态
     * @param transactionNo 商户订单号
     * @return 订单状态
     */
    public Map<String, Object> queryOrder(String transactionNo) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(mchid);
            request.setOutTradeNo(transactionNo);

            com.wechat.pay.java.service.payments.jsapi.model.Transaction transaction = jsapiService.queryOrderByOutTradeNo(request);

            Map<String, Object> result = new HashMap<>();
            result.put("transactionId", transaction.getTransactionId());
            result.put("outTradeNo", transaction.getOutTradeNo());
            result.put("tradeState", transaction.getTradeState());
            result.put("tradeStateDesc", transaction.getTradeStateDesc());
            result.put("successTime", transaction.getSuccessTime());

            return result;
        } catch (ServiceException e) {
            log.error("查询微信支付订单失败: {}", e.getErrorMessage(), e);
            throw new RuntimeException("查询微信支付订单失败: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            log.error("查询微信支付订单失败", e);
            throw new RuntimeException("查询微信支付订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭订单
     * @param transactionNo 商户订单号
     */
    public void closeOrder(String transactionNo) {
        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(mchid);
            request.setOutTradeNo(transactionNo);

            jsapiService.closeOrder(request);
            log.info("微信支付订单已关闭: transactionNo={}", transactionNo);
        } catch (ServiceException e) {
            log.error("关闭微信支付订单失败: {}", e.getErrorMessage(), e);
            throw new RuntimeException("关闭微信支付订单失败: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            log.error("关闭微信支付订单失败", e);
            throw new RuntimeException("关闭微信支付订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 申请退款
     * @param transactionNo 商户订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款单号
     */
    public String refund(String transactionNo, BigDecimal refundAmount, String reason) {
        try {
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(transactionNo);
            request.setOutRefundNo(generateRefundNo());
            request.setReason(reason);
            request.setNotifyUrl(notifyUrl + "/refund"); // 退款回调地址

            // 退款金额
            com.wechat.pay.java.service.refund.model.Amount amountObj = new com.wechat.pay.java.service.refund.model.Amount();
            amountObj.setRefund(refundAmount.multiply(new BigDecimal("100")).intValue());
            amountObj.setTotal(refundAmount.multiply(new BigDecimal("100")).intValue());
            amountObj.setCurrency("CNY");
            request.setAmount(amountObj);

            // 调用退款API
            Refund refund = refundService.create(request);

            log.info("微信支付退款申请成功: refundId={}", refund.getRefundId());
            return refund.getRefundId();
        } catch (ServiceException e) {
            log.error("申请微信支付退款失败: {}", e.getErrorMessage(), e);
            throw new RuntimeException("申请微信支付退款失败: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            log.error("申请微信支付退款失败", e);
            throw new RuntimeException("申请微信支付退款失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理微信支付回调
     * @param callbackData 回调数据
     */
    public void handlePaymentCallback(Map<String, Object> callbackData) {
        try {
            // 使用SDK验证签名已在过滤器中完成
            // 这里直接解析回调数据
            String transactionNo = (String) callbackData.get("out_trade_no");
            String channelTransactionNo = (String) callbackData.get("transaction_id");
            String tradeState = (String) callbackData.get("trade_state");

            // 更新交易状态
            boolean success = "SUCCESS".equals(tradeState);
            paymentService.processPaymentCallback(transactionNo, channelTransactionNo, success);

            log.info("微信支付回调处理成功: transactionNo={}, success={}", transactionNo, success);
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            throw new RuntimeException("处理微信支付回调失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理退款回调
     * @param callbackData 回调数据
     */
    public void handleRefundCallback(Map<String, Object> callbackData) {
        try {
            String refundNo = (String) callbackData.get("out_refund_no");
            String refundStatus = (String) callbackData.get("refund_status");

            log.info("退款回调处理: refundNo={}, status={}", refundNo, refundStatus);
            // TODO: 更新退款状态
        } catch (Exception e) {
            log.error("处理退款回调失败", e);
            throw new RuntimeException("处理退款回调失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成支付参数（供前端调用wx.requestPayment）
     */
    private Map<String, String> generatePayParams(String prepayId) {
        Map<String, String> params = new HashMap<>();
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = generateNonceStr();

        params.put("appId", appid);
        params.put("timeStamp", timeStamp);
        params.put("nonceStr", nonceStr);
        params.put("package", "prepay_id=" + prepayId);
        params.put("signType", "RSA");

        // 生成签名
        String signMessage = appid + "\n" + timeStamp + "\n" + nonceStr + "\n" + "prepay_id=" + prepayId + "\n";
        String paySign = generateSignature(signMessage);

        params.put("paySign", paySign);

        return params;
    }

    /**
     * 生成签名
     */
    private String generateSignature(String message) {
        try {
            // 使用私钥对消息进行签名
            java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
            String privateKeyContent = new String(Files.readAllBytes(Paths.get(keyPath)));
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            signer.initSign(privateKey);
            signer.update(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] signatureBytes = signer.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "REFUND" + timestamp + uuid;
    }
}
