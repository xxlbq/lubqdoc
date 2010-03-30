// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2008-6-20 14:32:26
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ExecWriter.java

package cn.bestwiz.jhf.trader.trader.business;

import cn.bestwiz.jhf.core.bo.bean.*;
import cn.bestwiz.jhf.core.bo.contructor.OrderBindInfoFactory;
import cn.bestwiz.jhf.core.bo.contructor.PositionInfoFactory;
import cn.bestwiz.jhf.core.bo.enums.*;
import cn.bestwiz.jhf.core.bo.exceptions.ServiceException;
import cn.bestwiz.jhf.core.custtrade.*;
import cn.bestwiz.jhf.core.dao.*;
import cn.bestwiz.jhf.core.dao.bean.main.*;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.jms.bean.*;
import cn.bestwiz.jhf.core.mail.BizMailService;
import cn.bestwiz.jhf.core.mail.SysMailService;
import cn.bestwiz.jhf.core.service.*;
import cn.bestwiz.jhf.core.util.*;
import cn.bestwiz.jhf.trader.trader.exception.ExecWriterException;
import java.math.BigDecimal;
import java.util.*;
import org.apache.commons.logging.Log;

// Referenced classes of package cn.bestwiz.jhf.trader.trader.business:
//            TraderProcessThreadPool

public class ExecWriter
{

    public ExecWriter()
    {
        m_props = null;
        m_coreService = null;
        m_productService = null;
        tradeService = TradeServiceFactory.DEFAULT;
        try
        {
            m_coreService = ServiceFactory.getCoreService();
            m_productService = ServiceFactory.getProductService();
            m_props = PropertiesLoader.getProperties("trader_thread_pool.properties");
        }
        catch(Exception e)
        {
            m_log.error("When ExecWriter init Errors: ", e);
        }
    }

    public void doProcess(OrderBindInfo orderBindInfo, boolean bFlag, String sCpExecutionId)
        throws ExecWriterException
    {
        try
        {
            String sOrdrBindId = orderBindInfo.getOrderBindId();
            if(sOrdrBindId != null && sOrdrBindId.length() > 0)
            {
                fillOrder(orderBindInfo, bFlag, sCpExecutionId);
            } else
            {
                m_log.error("orderbindid Is Null!!");
                throw new ExecWriterException("orderbindid Is Null!!");
            }
        }
        catch(Exception e)
        {
            m_log.error((new StringBuilder()).append("ExecWriter doProcess Exception : TransactionCounte = ").append(DbSessionFactory.getMainTransactionCounter()).toString());
            throw new ExecWriterException(e);
        }
    }

    public void doMultiThreadProcess(OrderBindInfo orderBindInfo, boolean bFlag, String sCpExecutionId)
        throws ExecWriterException
    {
        try
        {
            String sOrdrBindId = orderBindInfo.getOrderBindId();
            if(sOrdrBindId != null && sOrdrBindId.length() > 0)
            {
                List ordInfoList = getOrderList(orderBindInfo.getOrderBindId());
                if(ordInfoList != null && ordInfoList.size() > 0)
                {
                    Map cstOrdGroupMap = groupOrderInfoByCustomer(ordInfoList);
                    List ordInfoListFinal;
                    OrderBindInfo ordBindInfoFinal;
                    String cpExeId;
                    boolean isManual;
                    for(Iterator i$ = cstOrdGroupMap.entrySet().iterator(); i$.hasNext(); TraderProcessThreadPool.getInstance().executeOrder(new  Object(ordBindInfoFinal, ordInfoListFinal, cpExeId, isManual)     /* anonymous class not found */
    class _anm1 {}

))
                    {
                        java.util.Map.Entry entry = (java.util.Map.Entry)i$.next();
                        ordInfoListFinal = (List)entry.getValue();
                        ordBindInfoFinal = orderBindInfo;
                        cpExeId = sCpExecutionId;
                        isManual = bFlag;
                    }

                } else
                {
                    m_log.error((new StringBuilder()).append("getOrderList(orderBindId) return EMPTY ! ,").append(orderBindInfo.toString()).toString());
                }
            } else
            {
                m_log.error("orderbindid Is Null!!");
                throw new ExecWriterException("orderbindid Is Null!!");
            }
        }
        catch(Exception e)
        {
            throw new ExecWriterException(e);
        }
    }

    private void fillOrderByCustomer(OrderBindInfo ordBindInfo, List ordInfoList, String cpExeId, boolean isManual)
        throws Exception
    {
        OrderInfo orderInfo;
        for(Iterator i$ = ordInfoList.iterator(); i$.hasNext(); fillOrder(ordBindInfo, orderInfo, cpExeId, isManual))
            orderInfo = (OrderInfo)i$.next();

    }

    private void fillOrder(OrderBindInfo orderBindInfo, boolean bFlag, String sCpExecutionId)
        throws Exception
    {
        List lstOrder = getOrderList(orderBindInfo.getOrderBindId());
        if(lstOrder != null && lstOrder.size() > 0)
        {
            int i = 0;
            for(int size = lstOrder.size(); i < size; i++)
                try
                {
                    fillOrder(orderBindInfo, (OrderInfo)lstOrder.get(i), sCpExecutionId, bFlag);
                }
                catch(Exception e)
                {
                    throw new Exception((new StringBuilder()).append(" ERROR FIRE ,order:").append(lstOrder.get(i)).toString());
                }

        } else
        {
            throw new Exception("Get JHF_ORDER_BIND Is Null!!");
        }
    }

    private void fillOrder(OrderBindInfo orderBindInfo, OrderInfo order, String sCpExecutionId, boolean bFlag)
        throws Exception
    {
        long lOrderBegin = Calendar.getInstance().getTimeInMillis();
        List lstTradeId = new ArrayList();
        BigDecimal bdCustomerprice = null;
        TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
        String sPriceId = orderBindInfo.getPriceId();
        Map limitOrderMap = orderBindInfo.getLimitOrderMap();
        String sCurrentOrderId = order.getOrderId();
        order.setPriceId(sPriceId);
        OrderInfo mailOrder = order;
        if(order.getSide() == SideEnum.SIDE_BUY.getValue())
            bdCustomerprice = orderBindInfo.getTradeAskPrice();
        else
            bdCustomerprice = orderBindInfo.getTradeBidPrice();
        if(orderBindInfo.getType() == ExecutionTypeEnum.EXEC_OTHER_ENUM.getValue())
        {
            m_log.info((new StringBuilder()).append("opm order|").append(order.getExecutionType()).append("|").append(!orderBindInfo.isMarketOpenPrice()).append("|").append(bdCustomerprice).toString());
            if(order.getExecutionType() == ExecutionTypeEnum.EXEC_LIMIT_ENUM.getValue() && !orderBindInfo.isMarketOpenPrice())
                if(null == limitOrderMap || null == limitOrderMap.get(sCurrentOrderId))
                {
                    m_log.info((new StringBuilder()).append("limit order get order price is null..").append(sCurrentOrderId).append("|").append(orderBindInfo.getOrderBindId()).append("|").append(order.getOrderPrice()).toString());
                    bdCustomerprice = order.getOrderPrice();
                } else
                {
                    bdCustomerprice = (BigDecimal)limitOrderMap.get(sCurrentOrderId);
                    m_log.info((new StringBuilder()).append("limit order usd order price").append(sCurrentOrderId).append("|").append(orderBindInfo.getOrderBindId()).append("|").append(bdCustomerprice).toString());
                }
        }
        PositionInfo position = PositionInfoFactory.getInstance().createPositionInfo(order, m_coreService.getFrontDate(), bdCustomerprice, bFlag, sCpExecutionId, orderBindInfo.getMode());
        try
        {
            tradeService.getFillOrderService().fillOrder(order, position);
            if(!lstTradeId.contains(order.getTradeID()))
            {
                BigDecimal bdprice = bdCustomerprice;
                TraderProcessThreadPool.getInstance().executeSendMail(new  Object(mailOrder, bFlag, bdprice)     /* anonymous class not found */
    class _anm2 {}

);
                lstTradeId.add(order.getTradeID());
            }
        }
        catch(Exception e)
        {
            m_log.error("ExecWriter fillOrder Exception", e);
            if(order.getExecutionType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue() || order.getExecutionType() == ExecutionTypeEnum.EMERGENCY_SETTLE_ENUM.getValue())
                throw new Exception((new StringBuilder()).append("order Error: ").append(order.getOrderId()).toString());
        }
        long lOrderEnd = Calendar.getInstance().getTimeInMillis();
        m_log.info((new StringBuilder()).append(" fillOrder() end .=== Total Times : ").append(lOrderEnd - lOrderBegin).append(" (ms),orderId:").append(order.getOrderId()).append(" ,orderBindId:").append(orderBindInfo.getOrderBindId()).toString());
    }

    private List getOrderList(String sOrderBindId)
    {
        List lstOrder = null;
        List lstOrderBind = null;
        try
        {
            lstOrderBind = getOrderBind(sOrderBindId);
            TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
            if(lstOrderBind != null && lstOrderBind.size() > 0)
            {
                lstOrder = new ArrayList();
                int i = 0;
                for(int size = lstOrderBind.size(); i < size; i++)
                {
                    OrderBindInfo bindInfo = (OrderBindInfo)lstOrderBind.get(i);
                    OrderInfo orderInfo = tradeService.getReadOrderService().obtainOrder(bindInfo.getOrderId());
                    m_log.debug((new StringBuilder()).append(" orderInfo is ").append(orderInfo).toString());
                    lstOrder.add(orderInfo);
                }

            } else
            {
                int retryNum = Integer.parseInt(m_props.getProperty("ORDERBIND_RETRY_NUM"));
                int frequency = Integer.parseInt(m_props.getProperty("ORDERBIND_RETRY_FREQUENCY"));
                m_log.warn((new StringBuilder()).append("getOrderList() is emtpy by orderBindId=").append(sOrderBindId).append(", retrying ... ,retryNum:").append(retryNum).append(",frequency:").append(frequency).toString());
                int retrying = 0;
                do
                {
                    if(retrying >= retryNum)
                        break;
                    m_log.warn((new StringBuilder()).append("orderBindId:").append(sOrderBindId).append(",retrying number:").append(retrying).toString());
                    lstOrderBind = getOrderBind(sOrderBindId);
                    if(lstOrderBind != null && lstOrderBind.size() > 0)
                    {
                        m_log.warn((new StringBuilder()).append("getOrderList() is not empty by retry ").append(retrying).append(" times.").toString());
                        lstOrder = new ArrayList();
                        int i = 0;
                        for(int size = lstOrderBind.size(); i < size; i++)
                        {
                            OrderBindInfo bindInfo = (OrderBindInfo)lstOrderBind.get(i);
                            OrderInfo orderInfo = tradeService.getReadOrderService().obtainOrder(bindInfo.getOrderId());
                            lstOrder.add(orderInfo);
                        }

                        break;
                    }
                    Thread.sleep(frequency);
                    retrying++;
                } while(true);
                if(lstOrder == null || lstOrder.size() == 0)
                    m_log.error((new StringBuilder()).append(" After retry ! Get ORDER_BIND Is Null!! OrderBindId = ").append(sOrderBindId).toString());
                else
                    m_log.info((new StringBuilder()).append(" getOrderList() retry end .  endPoint: List<OrderInfo> size is ").append(lstOrder.size()).toString());
            }
        }
        catch(Exception e)
        {
            m_log.error(" Get OrderList Errors ", e);
            return null;
        }
        return lstOrder;
    }

    private void sendFillMail(OrderInfo order)
        throws Exception
    {
        m_log.debug(" sendFillMail() Start: ");
        HashMap mapParamters = buildContents(order);
        BizMailService bizMailService = new BizMailService();
        bizMailService.sendMail(order.getCustomerId(), MailActionIdEnum.FILL_ORDER_ENUM.getName(), mapParamters);
        m_log.debug("sendFillMail() End: ");
    }

    private void sendSysMail(OrderInfo order, BigDecimal bdCustomerprice)
        throws Exception
    {
        m_log.debug(" sendSysMail() Start: ");
        HashMap mapParamters = new HashMap();
        mapParamters.put("currencypair", order.getCurrencyPair());
        mapParamters.put("side", Integer.valueOf(order.getSide()));
        mapParamters.put("amount", order.getOrderAmount());
        mapParamters.put("price", bdCustomerprice);
        mapParamters.put("executiontime", DateHelper.getSystemTimestamp());
        SysMailService sysMailService = new SysMailService();
        sysMailService.sendMail(SysMailActionIdEnum.MANUAL_TRADER_ENUM.getName(), mapParamters);
        m_log.debug("sendSysMail() End: ");
    }

    private HashMap buildContents(OrderInfo order)
        throws Exception
    {
        HashMap mapParamters = new HashMap();
        String sCustomerOrderNo = null;
        String name = null;
        try
        {
            DbSessionFactory.beginTransaction(100);
            sCustomerOrderNo = order.getCustomerOrderNumber();
            DbSessionFactory.commitTransaction(100);
        }
        catch(Exception e)
        {
            m_log.error(" build Mail Contents getCustomerOrderNo Errors ", e);
            DbSessionFactory.rollbackTransaction(100);
            throw e;
        }
        try
        {
            name = m_coreService.obtainCustomerName(order.getCustomerId());
        }
        catch(Exception e)
        {
            m_log.error(" build Mail Contents getCustomerName Errors ", e);
            throw e;
        }
        mapParamters.put("name", name);
        mapParamters.put("customer_order_no", sCustomerOrderNo);
        return mapParamters;
    }

    protected final BigDecimal getUnitFromOrderAmount(String sProductId, BigDecimal bdAmount)
        throws Exception
    {
        m_log.debug("getUnitFromOrderAmount(String productId, double amount)  function start");
        m_log.debug((new StringBuilder()).append("parameter1[productId] = ").append(sProductId).toString());
        m_log.debug((new StringBuilder()).append("parameter2[amount] = ").append(bdAmount).toString());
        BigDecimal bdReturn = null;
        try
        {
            ProductInfo product = m_productService.obtainProduct(sProductId);
            bdReturn = bdAmount.divide(product.getUnit(), 7);
            m_log.debug("getUnitFromOrderAmount(String productId, double amount)  function end");
        }
        catch(Exception e)
        {
            m_log.error("getUnitFromOrderAmount Errors ", e);
            throw e;
        }
        return bdReturn;
    }

    private List getOrderBind(String sOrderBindId)
        throws Exception
    {
        m_log.debug("getOrderBind() Start: ");
        m_log.debug((new StringBuilder()).append("getOrderBind.parameter[sOrderBindId]) ").append(sOrderBindId).toString());
        List lstInfo = null;
        OrderBindInfo orderBindInfo = new OrderBindInfo();
        List lstBean = null;
        try
        {
            DbSessionFactory.beginTransaction(100);
            lstBean = DAOFactory.getOrderDao().getOrderBind(sOrderBindId);
            DbSessionFactory.commitTransaction(100);
        }
        catch(Exception e)
        {
            DbSessionFactory.rollbackTransaction(100);
            m_log.error("getOrderBind() Exception : ", e);
        }
        try
        {
            if(lstBean != null && lstBean.size() > 0)
            {
                lstInfo = new ArrayList();
                int i = 0;
                for(int size = lstBean.size(); i < size; i++)
                {
                    JhfOrderBind bean = (JhfOrderBind)lstBean.get(i);
                    orderBindInfo = OrderBindInfoFactory.getInstance().createInfo(bean);
                    lstInfo.add(orderBindInfo);
                }

            }
        }
        catch(Exception e)
        {
            m_log.error("create orderbindinfo Error : ", e);
        }
        m_log.debug("getOrderBind() End: ");
        return lstInfo;
    }

    public OrderResponseListInfo doBtProcess(OrderBindInfo orderBindInfo, boolean bFlag, String sCpExecutionId)
    {
        OrderResponseListInfo retListInfo = fillBtOrder(orderBindInfo, bFlag, sCpExecutionId);
        return retListInfo;
    }

    private OrderResponseListInfo fillBtOrder(OrderBindInfo orderBindInfo, boolean bFlag, String sCpExecutionId)
    {
        m_log.debug(" #fillBtOrder() start");
        List lstTradeId = new ArrayList();
        OrderResponseListInfo ResponseListInfo = new OrderResponseListInfo();
        OrderResponseInfo ResponseInfo = new OrderResponseInfo();
        ArrayList lstResponseInfo = new ArrayList();
        TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
        List lstOrder = null;
        String sOrderBindId = orderBindInfo.getOrderBindId();
        ResponseListInfo.setOrderBindId(sOrderBindId);
        lstOrder = getOrderList(sOrderBindId);
        String sPriceId = orderBindInfo.getPriceId();
        ResponseInfo.setPriceId(sPriceId);
        BigDecimal bdCustomerprice = null;
        int iOrderInfoSize = lstOrder.size();
        if(lstOrder != null && iOrderInfoSize > 0)
        {
            int i = 0;
            for(int size = lstOrder.size(); i < size; i++)
            {
                OrderInfo order = (OrderInfo)lstOrder.get(i);
                order.setPriceId(sPriceId);
                OrderInfo mailOrder = (OrderInfo)BeanCopy.copy(order);
                if(order.getSide() == SideEnum.SIDE_BUY.getValue())
                    bdCustomerprice = orderBindInfo.getTradeAskPrice();
                else
                    bdCustomerprice = orderBindInfo.getTradeBidPrice();
                try
                {
                    PositionInfo position = PositionInfoFactory.getInstance().createPositionInfo(order, m_coreService.getFrontDate(), bdCustomerprice, bFlag, sCpExecutionId, orderBindInfo.getMode());
                    tradeService.getFillOrderService().fillOrder(order, position);
                }
                catch(Exception e)
                {
                    m_log.error("fillOrder error", e);
                    ResponseInfo.setSuccessFlag(false);
                    ResponseInfo.setErrorMsg("FILL ORDER FAILURE!");
                    ResponseInfo.setErrorCode(4013);
                    lstResponseInfo.add(ResponseInfo);
                }
                if(!lstTradeId.contains(order.getTradeID()))
                {
                    BigDecimal bdprice = bdCustomerprice;
                    TraderProcessThreadPool.getInstance().executeSendMail(new  Object(mailOrder, bFlag, bdprice)     /* anonymous class not found */
    class _anm3 {}

);
                    lstTradeId.add(order.getTradeID());
                }
            }

            int iResponseInfoSize = lstResponseInfo.size();
            if(lstResponseInfo.size() < 1)
            {
                ResponseListInfo.setErrorCode(SuccessFlagEnum.ALL_SUCCESS_ENUM.getValue());
                ResponseListInfo.setResponseList(lstResponseInfo);
            } else
            if(iResponseInfoSize == iOrderInfoSize)
            {
                ResponseListInfo.setErrorCode(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
                ResponseListInfo.setErrorMsg("FILL ORDER FAILURE!");
                ResponseListInfo.setResponseList(lstResponseInfo);
            } else
            {
                ResponseListInfo.setErrorCode(SuccessFlagEnum.SUCCESS_FAILURE_ENUM.getValue());
                ResponseListInfo.setResponseList(lstResponseInfo);
            }
        } else
        {
            ResponseListInfo.setErrorCode(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
            ResponseListInfo.setErrorMsg("FILL ORDER FAILURE!");
            ResponseListInfo.setResponseList(lstResponseInfo);
        }
        return ResponseListInfo;
    }

    public void reverseFillOrder(String orderBindId)
        throws Exception
    {
        try
        {
            List lstOrder = getOrderList(orderBindId);
            TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
            if(lstOrder != null && lstOrder.size() > 0)
            {
                int i = 0;
                for(int size = lstOrder.size(); i < size; i++)
                {
                    OrderInfo order = (OrderInfo)lstOrder.get(i);
                    try
                    {
                        tradeService.getFillOrderService().reverseFillOrder(order);
                    }
                    catch(Exception e)
                    {
                        m_log.error((new StringBuilder()).append("reverseFillOrder Exception").append(orderBindId).append("|").append(order).toString(), e);
                    }
                }

            }
        }
        catch(Exception e)
        {
            m_log.error((new StringBuilder()).append("ExecWriter reverseFillOrder error : ").append(orderBindId).toString(), e);
            throw e;
        }
    }

    public void reverseFillOrder(List lstOrder)
        throws Exception
    {
        try
        {
            TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
            if(lstOrder != null && lstOrder.size() > 0)
            {
                int i = 0;
                for(int size = lstOrder.size(); i < size; i++)
                    try
                    {
                        OrderInfo orderInfo = tradeService.getReadOrderService().obtainOrder((String)lstOrder.get(i));
                        tradeService.getFillOrderService().reverseFillOrder(orderInfo);
                    }
                    catch(Exception e)
                    {
                        m_log.error((new StringBuilder()).append("reverseFillOrder Exception").append((String)lstOrder.get(i)).toString(), e);
                    }

            }
        }
        catch(Exception e)
        {
            m_log.error((new StringBuilder()).append("ExecWriter reverseFillOrder error : ").append(lstOrder).toString(), e);
            throw e;
        }
    }

    public void failFillOrder(String orderBindId)
        throws Exception
    {
        try
        {
            List lstOrder = getOrderList(orderBindId);
            TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
            if(lstOrder != null && lstOrder.size() > 0)
            {
                int i = 0;
                for(int size = lstOrder.size(); i < size; i++)
                {
                    OrderInfo order = (OrderInfo)lstOrder.get(i);
                    try
                    {
                        tradeService.getFillOrderService().failFillOrder(order);
                    }
                    catch(Exception e)
                    {
                        m_log.error((new StringBuilder()).append("failFillOrder Exception").append(orderBindId).append("|").append(order).toString(), e);
                    }
                }

            }
        }
        catch(Exception e)
        {
            m_log.error("ExecWriter failFillOrder error : ", e);
            throw e;
        }
    }

    private BigDecimal addSpread(String sCurrencyPair, BigDecimal basePrice, int side)
        throws Exception
    {
        BigDecimal newPrice = new BigDecimal("0");
        JhfSpotRateConfig jhfSpotRateConfig = getSpotRateConfig(sCurrencyPair);
        if(jhfSpotRateConfig == null)
        {
            m_log.error((new StringBuilder()).append("JhfSpotRateConfig is null, use cpexecutionprice").append(sCurrencyPair).toString());
            return basePrice;
        }
        if(side == SideEnum.SIDE_BUY.getValue())
            newPrice = basePrice.add(jhfSpotRateConfig.getBaseAskAdjustment());
        else
            newPrice = basePrice.add(jhfSpotRateConfig.getBaseBidAdjustment());
        return newPrice;
    }

    private JhfSpotRateConfig getSpotRateConfig(String sCurrencyPair)
        throws Exception
    {
        m_log.debug("====== getSpotRateConfig. Start: ");
        m_log.debug((new StringBuilder()).append("====== getSpotRateConfig[currencyPair]").append(sCurrencyPair).toString());
        JhfSpotRateConfig bean = null;
        try
        {
            DbSessionFactory.beginTransaction(100);
            bean = (JhfSpotRateConfig)DAOFactory.getConfigDao().get(cn/bestwiz/jhf/core/dao/bean/main/JhfSpotRateConfig, sCurrencyPair);
            DbSessionFactory.commitTransaction(100);
        }
        catch(Exception e)
        {
            m_log.error("=== [RateGenerator] === getSpotRateConfig() Error: ", e);
            DbSessionFactory.rollbackTransaction(100);
            return null;
        }
        m_log.debug("====== getSpotRateConfig() End: ");
        if(bean.getActiveFlag().intValue() == BoolEnum.BOOL_NO_ENUM.getValue())
            return null;
        else
            return bean;
    }

    public void doProcess(TradeResultInfo tradeRstInfo, String sCpExecutionId, boolean bFlag)
        throws ExecWriterException
    {
        try
        {
            if(tradeRstInfo.getOrderIdList() != null && tradeRstInfo.getOrderIdList().size() > 0)
            {
                fillOrder(tradeRstInfo, sCpExecutionId, bFlag);
            } else
            {
                m_log.error("Get OrderIdList from TradeResultInfo is null!!");
                throw new ExecWriterException("orderbindid Is Null!!");
            }
        }
        catch(Exception e)
        {
            m_log.error((new StringBuilder()).append("Trader doProcess Exception : TransactionCounte = ").append(DbSessionFactory.getMainTransactionCounter()).toString());
            throw new ExecWriterException(e);
        }
    }

    public void doMultiThreadProcess(TradeResultInfo tradeRstInfo, String sCpExecutionId, boolean bFlag)
        throws ExecWriterException
    {
        try
        {
            if(tradeRstInfo.getOrderIdList() != null && tradeRstInfo.getOrderIdList().size() > 0)
            {
                List lstOrder = tradeRstInfo.getOrderIdList();
                if(lstOrder != null && lstOrder.size() > 0)
                {
                    List orderInfoList = getOrderInfoList(lstOrder);
                    Map cstOrdGroupMap = groupOrderInfoByCustomer(orderInfoList);
                    TradeResultInfo tradeRstInfoFinal;
                    List ordInfoListFinal;
                    String cpExeId;
                    boolean isManual;
                    for(Iterator i$ = cstOrdGroupMap.entrySet().iterator(); i$.hasNext(); TraderProcessThreadPool.getInstance().executeOrder(new  Object(tradeRstInfoFinal, ordInfoListFinal, cpExeId, isManual)     /* anonymous class not found */
    class _anm4 {}

))
                    {
                        java.util.Map.Entry entry = (java.util.Map.Entry)i$.next();
                        tradeRstInfoFinal = tradeRstInfo;
                        ordInfoListFinal = (List)entry.getValue();
                        cpExeId = sCpExecutionId;
                        isManual = bFlag;
                    }

                } else
                {
                    m_log.error((new StringBuilder()).append("TradeResultInfo.getOrderIdList() return EMPTY ! ,").append(tradeRstInfo.toString()).toString());
                }
            } else
            {
                m_log.error("Get OrderIdList from TradeResultInfo is null!!");
                throw new ExecWriterException("orderbindid Is Null!!");
            }
        }
        catch(Exception e)
        {
            throw new ExecWriterException(e);
        }
    }

    private void fillOrderByCustomer(TradeResultInfo tradeRstInfo, List ordInfoList, String cpExeId, boolean isManual)
        throws Exception
    {
        OrderInfo orderInfo;
        for(Iterator i$ = ordInfoList.iterator(); i$.hasNext(); fillOrder(tradeRstInfo, orderInfo.getOrderId(), cpExeId, isManual))
            orderInfo = (OrderInfo)i$.next();

    }

    private List getOrderInfoList(List lstOrder)
        throws ServiceException
    {
        List orderInfoList = new ArrayList();
        OrderInfo orderInfo;
        for(Iterator i$ = lstOrder.iterator(); i$.hasNext(); orderInfoList.add(orderInfo))
        {
            String orderId = (String)i$.next();
            orderInfo = tradeService.getReadOrderService().obtainOrder(orderId);
        }

        return orderInfoList;
    }

    private void fillOrder(TradeResultInfo tradeRstInfo, String sCpExecutionId, boolean bFlag)
        throws Exception
    {
        List lstOrder = tradeRstInfo.getOrderIdList();
        if(lstOrder != null && lstOrder.size() > 0)
        {
            int i = 0;
            for(int size = lstOrder.size(); i < size; i++)
                fillOrder(tradeRstInfo, (String)lstOrder.get(i), sCpExecutionId, bFlag);

        }
    }

    private void fillOrder(TradeResultInfo tradeRstInfo, String sCurrentOrderId, String sCpExecutionId, boolean bFlag)
        throws Exception
    {
        long lOrderBegin = Calendar.getInstance().getTimeInMillis();
        List lstTradeId = new ArrayList();
        BigDecimal bdCustomerprice = null;
        TradeServiceFactory tradeService = TradeServiceFactory.DEFAULT;
        String sPriceId = tradeRstInfo.getPriceId();
        Map limitOrderMap = tradeRstInfo.getLimitOrderMap();
        OrderInfo orderInfo = tradeService.getReadOrderService().obtainOrder(sCurrentOrderId);
        orderInfo.setPriceId(sPriceId);
        if(!isCustTrader(orderInfo.getOrderId()))
            return;
        OrderInfo mailOrder = orderInfo;
        if(orderInfo.getExecutionType() == ExecutionTypeEnum.EXEC_LOSS_CUT_ENUM.getValue())
            bdCustomerprice = calcFinalCustomerPrice(tradeRstInfo, orderInfo);
        else
        if(orderInfo.getSide() == SideEnum.SIDE_BUY.getValue())
            bdCustomerprice = tradeRstInfo.getTradeAskPrice();
        else
            bdCustomerprice = tradeRstInfo.getTradeBidPrice();
        if(tradeRstInfo.getExectionType() == ExecutionTypeEnum.EXEC_OTHER_ENUM.getValue())
            if(orderInfo.getExecutionType() == ExecutionTypeEnum.EXEC_LIMIT_ENUM.getValue() && !tradeRstInfo.isMarketOpenPrice())
            {
                if(null == limitOrderMap || null == limitOrderMap.get(sCurrentOrderId))
                {
                    m_log.info((new StringBuilder()).append("limit order get order price is null..").append(sCurrentOrderId).append("|").append(tradeRstInfo.getOrderBindId()).append("|").append(orderInfo.getOrderPrice()).toString());
                    bdCustomerprice = orderInfo.getOrderPrice();
                } else
                {
                    bdCustomerprice = (BigDecimal)limitOrderMap.get(sCurrentOrderId);
                    m_log.info((new StringBuilder()).append("limit order usd order price").append(sCurrentOrderId).append("|").append(tradeRstInfo.getOrderBindId()).append("|").append(bdCustomerprice).toString());
                }
            } else
            if(orderInfo.getExecutionType() == ExecutionTypeEnum.EXEC_STOP_ENUM.getValue())
                bdCustomerprice = calcFinalCustomerPrice(tradeRstInfo, orderInfo);
        PositionInfo position = PositionInfoFactory.getInstance().createPositionInfo(orderInfo, m_coreService.getFrontDate(), bdCustomerprice, bFlag, sCpExecutionId, tradeRstInfo.getMode());
        try
        {
            tradeService.getFillOrderService().fillOrder(orderInfo, position);
            long lSendMailBegin = Calendar.getInstance().getTimeInMillis();
            if(!lstTradeId.contains(orderInfo.getTradeID()))
            {
                BigDecimal bdprice = bdCustomerprice;
                TraderProcessThreadPool.getInstance().executeSendMail(new  Object(mailOrder, bFlag, bdprice)     /* anonymous class not found */
    class _anm5 {}

);
                lstTradeId.add(orderInfo.getTradeID());
            }
            long lSendMailEnd = Calendar.getInstance().getTimeInMillis();
            m_log.info((new StringBuilder()).append("===Trader SendMail Times===").append(lSendMailEnd - lSendMailBegin).append(" (millseconds)").append(sCurrentOrderId).append("|").append(tradeRstInfo.getOrderBindId()).toString());
        }
        catch(Exception e)
        {
            m_log.error("Trader fillOrder Exception", e);
        }
        long lOrderEnd = Calendar.getInstance().getTimeInMillis();
        m_log.info((new StringBuilder()).append(" fillOrder() end .=== Total Times : ").append(lOrderEnd - lOrderBegin).append(" (ms),orderId:").append(sCurrentOrderId).append(" ,orderBindId:").append(tradeRstInfo.getOrderBindId()).toString());
    }

    public void setEmergencyCustomerPrice(TradeResultInfo tradeRstInfo, OrderBindInfo orderBindInfo)
    {
        m_log.info((new StringBuilder()).append("before calcFinalCustomerPrice() param : orderBindId:").append(orderBindInfo.getOrderBindId()).append(",currencyPair:").append(tradeRstInfo.getCurrencyPair()).append(",tradeRstSide:").append(tradeRstInfo.getSide()).append(",orderBindInfoSide:").append(orderBindInfo.getSide()).append(",resultTradeAsk:").append(tradeRstInfo.getTradeAskPrice()).append(",resultTradeBid:").append(tradeRstInfo.getTradeBidPrice()).append(",cpAsk:").append(tradeRstInfo.getCpExecutionAskPrice()).append(",cpBid:").append(tradeRstInfo.getCpExecutionBidPrice()).append(",orderBindPriceId:").append(orderBindInfo.getPriceId()).append(",orderBindTradeAsk:").append(orderBindInfo.getTradeAskPrice()).append(",orderBindTradeBid:").append(orderBindInfo.getTradeBidPrice()).toString());
        int side = tradeRstInfo.getSide();
        if(side == SideEnum.SIDE_BUY.getValue())
        {
            BigDecimal buyFinalCustomerPrice = calcFinalCustomerPriceByBind(tradeRstInfo.getCurrencyPair(), side, tradeRstInfo.getTradeAskPrice(), tradeRstInfo.getCpExecutionAskPrice(), orderBindInfo);
            orderBindInfo.setTradeAskPrice(buyFinalCustomerPrice);
        } else
        if(side == SideEnum.SIDE_SELL.getValue())
        {
            BigDecimal sellFinalCustomerPrice = calcFinalCustomerPriceByBind(tradeRstInfo.getCurrencyPair(), side, tradeRstInfo.getTradeBidPrice(), tradeRstInfo.getCpExecutionBidPrice(), orderBindInfo);
            orderBindInfo.setTradeBidPrice(sellFinalCustomerPrice);
        } else
        {
            m_log.error((new StringBuilder()).append("shouldn't be here ! ").append(tradeRstInfo.getOrderId()).toString());
        }
        m_log.info((new StringBuilder()).append("after calcFinalCustomerPrice() param : orderBindId:").append(orderBindInfo.getOrderBindId()).append(",currencyPair:").append(tradeRstInfo.getCurrencyPair()).append(",tradeRstSide:").append(tradeRstInfo.getSide()).append(",orderBindInfoSide:").append(orderBindInfo.getSide()).append(",resultTradeAsk:").append(tradeRstInfo.getTradeAskPrice()).append(",resultTradeBid:").append(tradeRstInfo.getTradeBidPrice()).append(",cpAsk:").append(tradeRstInfo.getCpExecutionAskPrice()).append(",cpBid:").append(tradeRstInfo.getCpExecutionBidPrice()).append(",orderBindPriceId:").append(orderBindInfo.getPriceId()).append(",orderBindTradeAsk:").append(orderBindInfo.getTradeAskPrice()).append(",orderBindTradeBid:").append(orderBindInfo.getTradeBidPrice()).toString());
    }

    public BigDecimal calcFinalCustomerPriceByBind(String currencyPair, int side, BigDecimal custTraderPrice, BigDecimal cpExecutionPrice, OrderBindInfo orderBindInfo)
    {
        BigDecimal customerPrice = null;
        BigDecimal cpExePriceWithSpread = null;
        try
        {
            cpExePriceWithSpread = addSpread(currencyPair, cpExecutionPrice, side);
        }
        catch(Exception e)
        {
            m_log.error("ExecWriter.addSpread() error ! ", e);
        }
        m_log.info((new StringBuilder()).append("after add spread ,side:").append(side).append(" ,custTraderPrice:").append(custTraderPrice).append(" ,pair:").append(currencyPair).append(" ,cpExePriceWithSpread:").append(cpExePriceWithSpread).toString());
        if(side == SideEnum.SIDE_BUY.getValue())
        {
            if(custTraderPrice.compareTo(cpExePriceWithSpread) < 0)
            {
                customerPrice = cpExePriceWithSpread;
                orderBindInfo.setPriceId(null);
            } else
            {
                customerPrice = custTraderPrice;
            }
        } else
        if(side == SideEnum.SIDE_SELL.getValue())
            if(custTraderPrice.compareTo(cpExePriceWithSpread) > 0)
            {
                customerPrice = cpExePriceWithSpread;
                orderBindInfo.setPriceId(null);
            } else
            {
                customerPrice = custTraderPrice;
            }
        m_log.info((new StringBuilder()).append("return customerPrice=").append(customerPrice).toString());
        return customerPrice;
    }

    public BigDecimal calcFinalCustomerPrice(TradeResultInfo tradeRstInfo, OrderInfo orderInfo)
    {
        BigDecimal finalCustomerPrice = null;
        m_log.info((new StringBuilder()).append("before calcFinalCustomerPrice() param : orderId:").append(orderInfo.getOrderId()).append(",currencyPair:").append(tradeRstInfo.getCurrencyPair()).append(",tradeRstInfoSide:").append(tradeRstInfo.getSide()).append(",orderInfoSide:").append(orderInfo.getSide()).append(",resultTradeAsk:").append(tradeRstInfo.getTradeAskPrice()).append(",resultTradeBid:").append(tradeRstInfo.getTradeBidPrice()).append(",cpAsk:").append(tradeRstInfo.getCpExecutionAskPrice()).append(",cpBid:").append(tradeRstInfo.getCpExecutionBidPrice()).append(",orderInfoPriceId:").append(orderInfo.getPriceId()).toString());
        int side = orderInfo.getSide();
        if(side == SideEnum.SIDE_BUY.getValue())
            finalCustomerPrice = calcFinalCustomerPriceByInfo(tradeRstInfo.getCurrencyPair(), orderInfo.getSide(), tradeRstInfo.getTradeAskPrice(), tradeRstInfo.getCpExecutionAskPrice(), orderInfo);
        else
        if(side == SideEnum.SIDE_SELL.getValue())
            finalCustomerPrice = calcFinalCustomerPriceByInfo(tradeRstInfo.getCurrencyPair(), orderInfo.getSide(), tradeRstInfo.getTradeBidPrice(), tradeRstInfo.getCpExecutionBidPrice(), orderInfo);
        else
            m_log.error("shouldn't be here ! ");
        m_log.info((new StringBuilder()).append("after calcFinalCustomerPrice() param : orderId:").append(orderInfo.getOrderId()).append(",currencyPair:").append(tradeRstInfo.getCurrencyPair()).append(",tradeRstInfoSide:").append(tradeRstInfo.getSide()).append(",orderInfoSide:").append(orderInfo.getSide()).append(",resultTradeAsk:").append(tradeRstInfo.getTradeAskPrice()).append(",resultTradeBid:").append(tradeRstInfo.getTradeBidPrice()).append(",cpAsk:").append(tradeRstInfo.getCpExecutionAskPrice()).append(",cpBid:").append(tradeRstInfo.getCpExecutionBidPrice()).append(",orderBindPriceId:").append(orderInfo.getPriceId()).append(",finalCustomerPrice:").append(finalCustomerPrice).toString());
        return finalCustomerPrice;
    }

    public BigDecimal calcFinalCustomerPriceByInfo(String currencyPair, int side, BigDecimal custTraderPrice, BigDecimal cpExecutionPrice, OrderInfo orderInfo)
    {
        BigDecimal customerPrice = null;
        BigDecimal cpExePriceWithSpread = null;
        try
        {
            cpExePriceWithSpread = addSpread(currencyPair, cpExecutionPrice, side);
        }
        catch(Exception e)
        {
            m_log.error("ExecWriter.addSpread() error ! ", e);
        }
        m_log.info((new StringBuilder()).append("after add spread ,side:").append(side).append(" ,custTraderPrice:").append(custTraderPrice).append(" ,pair:").append(currencyPair).append(" ,cpExePriceWithSpread:").append(cpExePriceWithSpread).toString());
        if(side == SideEnum.SIDE_BUY.getValue())
        {
            if(custTraderPrice.compareTo(cpExePriceWithSpread) < 0)
            {
                customerPrice = cpExePriceWithSpread;
                orderInfo.setPriceId(null);
            } else
            {
                customerPrice = custTraderPrice;
            }
        } else
        if(side == SideEnum.SIDE_SELL.getValue())
            if(custTraderPrice.compareTo(cpExePriceWithSpread) > 0)
            {
                customerPrice = cpExePriceWithSpread;
                orderInfo.setPriceId(null);
            } else
            {
                customerPrice = custTraderPrice;
            }
        m_log.info((new StringBuilder()).append("return customerPrice=").append(customerPrice).toString());
        return customerPrice;
    }

    private boolean isCustTrader(String orderId)
        throws Exception
    {
        boolean ret = false;
        JhfHedgeCusttrade bean = null;
        try
        {
            DbSessionFactory.beginTransaction(100);
            bean = DAOFactory.getTradeDao().getHedgeCusttrade(orderId);
            DbSessionFactory.commitTransaction(100);
        }
        catch(Exception e)
        {
            DbSessionFactory.rollbackTransaction(100);
            m_log.error("isCustTrader is Exception", e);
            throw new Exception("isCustTrader is Exception");
        }
        if(bean.getAmountNotHedged().compareTo(ZERO) == 0)
            ret = true;
        return ret;
    }

    public FxSpotRateInfoMap convert(FxSpotRateInfo fxSpotRateInfo)
        throws Exception
    {
        FxSpotRateInfoMap fxSpotRateInfoMap = new FxSpotRateInfoMap();
        m_log.info((new StringBuilder()).append("send LossCut Rate covert Start:").append(fxSpotRateInfo).toString());
        List lstRows = getSpread(fxSpotRateInfo.getCurrencyPair());
        if(lstRows == null || lstRows.size() == 0)
        {
            m_log.error("no getSpread");
            throw new Exception("no getSpread");
        }
        int i = 0;
        for(int size = lstRows.size(); i < size; i++)
            fxSpotRateInfoMap.put(((JhfSpread)lstRows.get(i)).getId().getSpreadId(), fxSpotRateInfo);

        m_log.info((new StringBuilder()).append("send LossCut Rate covert End:").append(fxSpotRateInfoMap).toString());
        return fxSpotRateInfoMap;
        Exception e;
        e;
        m_log.error("send LossCut Rate cover Exception :", e);
        throw e;
    }

    private List getSpread(String sCurrencyPair)
        throws Exception
    {
        m_log.info((new StringBuilder()).append("send LossCut Rate getSpread.convert() Start: ").append(sCurrencyPair).toString());
        List rows = null;
        try
        {
            DbSessionFactory.beginTransaction(100);
            rows = DAOFactory.getConfigDao().obtainJhfSpread(sCurrencyPair);
            DbSessionFactory.commitTransaction(100);
        }
        catch(Exception e)
        {
            m_log.error("send LossCut Rate getSpread.Exception: ", e);
            DbSessionFactory.rollbackTransaction(100);
            throw new Exception(e);
        }
        m_log.info((new StringBuilder()).append("send LossCut Rate getSpread.convert() End: ").append(rows).toString());
        return rows;
    }

    public TradeResultInfo disposeCpExecutionPrice(TradeResultInfo info)
        throws Exception
    {
        try
        {
            if(null != info.getCpExecutionAskPrice())
                info.setCpExecutionAskPrice(RateCalcHelpers.CalcAakRateUp(info.getCurrencyPair(), info.getCpExecutionAskPrice()));
            if(null != info.getCpExecutionBidPrice())
                info.setCpExecutionBidPrice(RateCalcHelpers.CalcBidRate(info.getCurrencyPair(), info.getCpExecutionBidPrice()));
            m_log.info((new StringBuilder()).append("after dispose CpExecutionPrice ").append(info).toString());
        }
        catch(Exception e)
        {
            m_log.error((new StringBuilder()).append("disposeCpExecutionPrice Exception:").append(info).toString(), e);
            throw e;
        }
        return info;
    }

    private Map groupOrderInfoByCustomer(List ordInfoList)
    {
        Map cstOrdGroupMap = new HashMap();
        int i = 0;
        for(int size = ordInfoList.size(); i < size; i++)
        {
            OrderInfo ordInfo = (OrderInfo)ordInfoList.get(i);
            if(cstOrdGroupMap.containsKey(ordInfo.getCustomerId()))
            {
                List cstOrdList = (List)cstOrdGroupMap.get(ordInfo.getCustomerId());
                cstOrdList.add(ordInfo);
            } else
            {
                List cstOrdList = new ArrayList();
                cstOrdList.add(ordInfo);
                cstOrdGroupMap.put(ordInfo.getCustomerId(), cstOrdList);
            }
        }

        return cstOrdGroupMap;
    }

    private Properties m_props;
    private static final String PROPS_FILE_NAME = "trader_thread_pool.properties";
    private static Log m_log = LogUtil.getLog(cn/bestwiz/jhf/trader/trader/business/ExecWriter);
    private CoreService m_coreService;
    private ProductService m_productService;
    private TradeServiceFactory tradeService;
    private static BigDecimal ZERO = BigDecimal.valueOf(0L);






}